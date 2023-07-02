package pt.up.fe.comp2023.ollir.optimization.constants.visitor;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.ollir.optimization.constants.OptimizationContext;
import pt.up.fe.comp2023.ollir.optimization.constants.value.BooleanValue;
import pt.up.fe.comp2023.ollir.optimization.constants.value.IntegerValue;
import pt.up.fe.comp2023.ollir.optimization.constants.value.TypedValue;

import java.util.Optional;

public class ConstantsExpressionOptimizer extends AJmmVisitor<OptimizationContext, Optional<? extends TypedValue<?>>> {

    @Override
    protected void buildVisitor() {
        addVisit("ParenthesisExpression", this::dealWithParenthesisExpression);
        addVisit("UnaryOp", this::dealWithUnaryOp);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("IntegerLiteral", this::dealWithIntegerLiteral);
        addVisit("BooleanLiteral", this::dealWithBooleanLiteral);
        addVisit("VariableLiteral", this::dealWithVariableLiteral);
        setDefaultVisit(this::dealWithOtherExpressions);
    }

    private Optional<? extends TypedValue<?>> dealWithParenthesisExpression(JmmNode node, OptimizationContext ctx) {
        return visit(node.getJmmChild(0), ctx);
    }

    private Optional<TypedValue<?>> dealWithUnaryOp(JmmNode node, OptimizationContext ctx) {
        var childValue = visit(node.getJmmChild(0), ctx);

        var op = node.get("op");
        return switch (op) {
            case "!" -> childValue
                .map(BooleanValue.class::cast)
                .map(value -> value.map(val -> !val));

            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }

    private Optional<TypedValue<?>> dealWithBinaryOp(JmmNode node, OptimizationContext ctx) {
        var leftChild = visit(node.getJmmChild(0), ctx);
        var rightChild = visit(node.getJmmChild(1), ctx);

        var op = node.get("op");
        return switch (op) {
            case "+", "-", "*", "/", "<" -> {
                var leftValue = leftChild
                        .map(IntegerValue.class::cast)
                        .map(IntegerValue::value)
                        .orElse(null);
                
                var rightValue = rightChild
                        .map(IntegerValue.class::cast)
                        .map(IntegerValue::value)
                        .orElse(null);
                
                if (leftValue == null || rightValue == null)
                    yield Optional.empty();
                
                TypedValue<?> result = switch (op) {
                    case "+" -> new IntegerValue(leftValue + rightValue);
                    case "-" -> new IntegerValue(leftValue - rightValue);
                    case "*" -> new IntegerValue(leftValue * rightValue);
                    case "/" -> new IntegerValue(leftValue / rightValue);
                    case "<" -> new BooleanValue(leftValue < rightValue);
                    default -> throw new IllegalStateException("Unexpected value: " + op);
                };
                
                yield Optional.of(result);
            }
            
            case "&&" -> {
                var leftValue = leftChild
                        .map(BooleanValue.class::cast)
                        .map(BooleanValue::value)
                        .orElse(null);

                var rightValue = rightChild
                        .map(BooleanValue.class::cast)
                        .map(BooleanValue::value)
                        .orElse(null);

                if (leftValue == null || rightValue == null)
                    yield Optional.empty();

                TypedValue<?> result = new BooleanValue(leftValue && rightValue);

                yield Optional.of(result);
            }
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }

    private Optional<? extends TypedValue<?>> dealWithIntegerLiteral(JmmNode node, OptimizationContext ctx) {
        return IntegerValue.fromJmmNode(node);
    }

    private Optional<? extends TypedValue<?>> dealWithBooleanLiteral(JmmNode node, OptimizationContext ctx) {
        return BooleanValue.fromJmmNode(node);
    }

    private Optional<? extends TypedValue<?>> dealWithVariableLiteral(JmmNode node, OptimizationContext ctx) {
        var name = node.get("name");

        System.out.println(node.getJmmParent().toTree());
        System.out.println("Variable: " + name);
        System.out.println(ctx.getOptimizedValue(name));
        System.out.println(".");
        System.out.println(".");
        return ctx.getOptimizedValue(name);
    }

    private Optional<TypedValue<?>> dealWithOtherExpressions(JmmNode node, OptimizationContext ctx) {
        return Optional.empty();
    }
}
