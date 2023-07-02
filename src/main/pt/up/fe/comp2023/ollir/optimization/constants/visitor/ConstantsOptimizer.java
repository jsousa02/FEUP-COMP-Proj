package pt.up.fe.comp2023.ollir.optimization.constants.visitor;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.ollir.optimization.constants.OptimizationContext;
import pt.up.fe.comp2023.ollir.optimization.constants.value.BooleanValue;
import pt.up.fe.comp2023.ollir.optimization.constants.value.IntegerValue;
import pt.up.fe.comp2023.ollir.optimization.constants.value.TypedValue;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.utils.Pair;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public class ConstantsOptimizer extends AJmmVisitor<OptimizationContext, OptimizationContext> {

    private final ConstantsExpressionOptimizer ceo = new ConstantsExpressionOptimizer();

    @Override
    protected void buildVisitor() {
        addVisit("VariableAssignmentStatement", this::dealWithVariableAssignmentStatement);
        addVisit("IfStatement", this::dealWithIfStatement);
        addVisit("WhileStatement", this::dealWithWhileStatement);

        addVisit("GenericMethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("MainMethodDeclaration", this::dealWithMethodDeclaration);

        var expressions = Arrays.asList(
                "ParenthesisExpression",
                "ArrayIndexExpression",
                "MethodCallExpression",
                "PropertyAccessExpression",
                "UnaryOp",
                "BinaryOp",
                "ArrayInitializationExpression",
                "ObjectInitializationExpression",
                "IntegerLiteral",
                "BooleanLiteral",
                "ThisLiteral",
                "VariableLiteral"
        );

        for (var s : expressions) {
            addVisit(s, this::dealWithExpression);
        }

        setDefaultVisit(this::visitAllChildren);
    }

    @Override
    protected OptimizationContext visitAllChildren(JmmNode node, OptimizationContext data) {
        for (JmmNode child : node.getChildren()) {
            data = visit(child, data);
        }

        return data;
    }

    private OptimizationContext dealWithMethodDeclaration(JmmNode node, OptimizationContext ctx) {
        return visitAllChildren(node, new OptimizationContext());
    }

    private OptimizationContext dealWithExpression(JmmNode node, OptimizationContext ctx) {
        visitAllChildren(node, ctx);

        var value = ceo.visit(node, ctx);
        value.ifPresent(val -> node.replace(val.toJmmNode()));

        return ctx;
    }

    private OptimizationContext dealWithVariableAssignmentStatement(JmmNode node, OptimizationContext ctx) {
        var varName = node.get("name");

        var valueNode = node.getJmmChild(0);
        visitAllChildren(valueNode, ctx);

        ctx.drop(varName);

        var value = ceo.visit(valueNode, ctx);
        value.ifPresent(val -> {
            ctx.assign(varName, val);
//            node.delete();
        });

        return ctx;
    }

    private OptimizationContext dealWithIfStatement(JmmNode node, OptimizationContext ctx) {
        var conditionNode = node.getJmmChild(0);
        var conditionValue = ceo.visit(conditionNode, ctx)
                .map(BooleanValue.class::cast)
                .map(BooleanValue::value);

        Supplier<Pair<JmmNode, OptimizationContext>> ifTrue = () -> {
            var ifTrueNode = node.getJmmChild(1);
            return Pair.of(ifTrueNode, visit(ifTrueNode, ctx.copy()));
        };

        Supplier<Pair<JmmNode, OptimizationContext>> ifFalse = () -> {
            var ifFalseNode = node.getNumChildren() > 2
                    ? Optional.of(node.getJmmChild(2))
                    : Optional.<JmmNode>empty();

            return ifFalseNode
                    .map(falseNode -> Pair.of(falseNode, visit(falseNode, ctx.copy())))
                    .orElseGet(() -> Pair.of(null, ctx.copy()));
        };

        var result = conditionValue
                .map(val -> val ? ifTrue : ifFalse)
                .map(Supplier::get)
                .orElseGet(() -> {
                    visit(conditionNode, ctx);

                    var trueBranchCtx = ifTrue.get().second();
                    var falseBranchCtx = ifFalse.get().second();

                    return Pair.of(node, trueBranchCtx.intersect(falseBranchCtx));
                });

        var nodeReplacement = result.first();
        if (nodeReplacement != null)
            node.replace(nodeReplacement);
        else
            node.delete();

        return result.second();
    }

    private OptimizationContext dealWithWhileStatement(JmmNode node, OptimizationContext ctx) {
        var whileBody = node.getJmmChild(1);

        var clonedBody = JmmNodeUtils.deepClone(whileBody);
        var afterWhileCtx = visit(clonedBody, ctx.copy());

        var resultingCtx = afterWhileCtx.intersect(ctx);
        return visitAllChildren(node, resultingCtx);
    }
}
