package pt.up.fe.comp2023.ollir.visitor;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.analysis.context.JmmSymbol;
import pt.up.fe.comp2023.analysis.type.primitives.JmmVoidType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmStaticReferenceType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmUnknownType;
import pt.up.fe.comp2023.ollir.OllirExpression;
import pt.up.fe.comp2023.ollir.OllirGenerator;

import java.util.ArrayList;
import java.util.List;

public class OllirExpressionVisitor extends AJmmVisitor<MethodContext, OllirExpression> {

    private final OllirGenerator ollir = new OllirGenerator();

    @Override
    protected void buildVisitor() {
        addVisit("ParenthesisExpression", this::dealWithParenthesisExpression);
        addVisit("ArrayIndexExpression", this::dealWithArrayIndexExpression);
        addVisit("MethodCallExpression", this::dealWithMethodCallExpression);
        addVisit("PropertyAccessExpression", this::dealWithPropertyAccessExpression);
        addVisit("UnaryOp", this::dealWithUnaryOp);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ArrayInitializationExpression", this::dealWithArrayInitializationExpression);
        addVisit("ObjectInitializationExpression", this::dealWithObjectInitializationExpression);
        addVisit("IntegerLiteral", this::dealWithIntegerLiterals);
        addVisit("BooleanLiteral", this::dealWithBooleanLiterals);
        addVisit("ThisLiteral", this::dealWithThisLiteral);
        addVisit("VariableLiteral", this::dealWithVariableLiteral);
    }

    private OllirExpression dealWithParenthesisExpression(JmmNode jmmNode, MethodContext ctx) {
        return visit(jmmNode.getJmmChild(0), ctx);
    }

    private OllirExpression dealWithArrayIndexExpression(JmmNode jmmNode, MethodContext ctx) {
        var arrayExpr = visit(jmmNode.getJmmChild(0), ctx);
        var indexExpr = visit(jmmNode.getJmmChild(1), ctx);

        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var code = new StringBuilder();
        arrayExpr.getCode().ifPresent(code::append);
        indexExpr.getCode().ifPresent(code::append);

        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.arrayIndex(arrayExpr.getIdentifier(), indexExpr.getReference(ollir), returnType);
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithMethodCallExpression(JmmNode jmmNode, MethodContext ctx) {
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();
        var calleeType = JmmNodeUtils.getNodeType(jmmNode.getJmmChild(0)).orElseThrow();

        boolean isStaticInvocation;

        if (returnType instanceof JmmUnknownType) {
            isStaticInvocation = calleeType instanceof JmmStaticReferenceType;
            returnType = JmmNodeUtils.getNodeType(jmmNode.getJmmParent()).orElse(JmmVoidType.getInstance());
        } else {
            var methodName = jmmNode.get("name");
            var method = ctx.method()
                    .getParentTable()
                    .getMethodByName(methodName)
                    .orElseThrow();

            isStaticInvocation = method.isStatic();
        }

        var invocationType = isStaticInvocation ? OllirGenerator.InvocationType.STATIC : OllirGenerator.InvocationType.VIRTUAL;
        var methodName = jmmNode.get("name");
        var calleeExpr = visit(jmmNode.getJmmChild(0), ctx);

        var code = new StringBuilder();
        calleeExpr.getCode().ifPresent(code::append);

        List<String> args = new ArrayList<>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            var argExpr = visit(jmmNode.getJmmChild(i), ctx);
            argExpr.getCode().ifPresent(code::append);

            args.add(argExpr.getReference(ollir));
        }

        var operationCode = ollir.invoke(invocationType, calleeExpr.getReference(ollir), methodName, args, returnType);

        if (returnType instanceof JmmVoidType) {
            code.append(operationCode).append(";\n");
            return new OllirExpression(null, null, code.toString());
        }

        var resultVar = ctx.variables().getFreeVariable();
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));
        return new OllirExpression(resultVar, returnType, code.toString());

    }

    private OllirExpression dealWithPropertyAccessExpression(JmmNode jmmNode, MethodContext ctx) {
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();
        var arrayExpr = visit(jmmNode.getJmmChild(0), ctx);

        var code = new StringBuilder();
        arrayExpr.getCode().ifPresent(code::append);

        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.arrayLength(arrayExpr.getReference(ollir));
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithUnaryOp(JmmNode jmmNode, MethodContext ctx) {
        var op = jmmNode.get("op");
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var childExpr = visit(jmmNode.getJmmChild(0), ctx);

        var code = new StringBuilder();
        childExpr.getCode().ifPresent(code::append);

        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.unaryOp(op, returnType, childExpr.getReference(ollir));
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithBinaryOp(JmmNode jmmNode, MethodContext ctx) {
        var op = jmmNode.get("op");
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var lhs = visit(jmmNode.getJmmChild(0), ctx);
        var rhs = visit(jmmNode.getJmmChild(1), ctx);

        var code = new StringBuilder();
        lhs.getCode().ifPresent(code::append);
        rhs.getCode().ifPresent(code::append);


        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.binaryOp(op, returnType, lhs.getReference(ollir), rhs.getReference(ollir));
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithArrayInitializationExpression(JmmNode jmmNode, MethodContext ctx) {
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var sizeExpr = visit(jmmNode.getJmmChild(0), ctx);

        var code = new StringBuilder();
        sizeExpr.getCode().ifPresent(code::append);

        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.newArray(sizeExpr.getReference(ollir), returnType);
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithObjectInitializationExpression(JmmNode jmmNode, MethodContext ctx) {
        var returnType = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var code = new StringBuilder();

        var resultVar = ctx.variables().getFreeVariable();
        var operationCode = ollir.newObject(returnType);
        code.append(ollir.assignment(ollir.withType(resultVar, returnType), returnType, operationCode));

        var resultReference = ollir.withType(resultVar, returnType);
        code.append(ollir.invoke(OllirGenerator.InvocationType.SPECIAL, resultReference, "<init>", List.of(), JmmVoidType.getInstance()))
                .append(";\n");

        return new OllirExpression(resultVar, returnType, code.toString());
    }

    private OllirExpression dealWithIntegerLiterals(JmmNode jmmNode, MethodContext ctx) {
        var value = jmmNode.get("value");
        var type = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        return new OllirExpression(value, type, null);
    }

    private OllirExpression dealWithBooleanLiterals(JmmNode jmmNode, MethodContext ctx) {
        var value = jmmNode.get("value");
        var type = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        return new OllirExpression(value.equals("true") ? "1" : "0", type, null);
    }

    private OllirExpression dealWithThisLiteral(JmmNode jmmNode, MethodContext ctx) {
        var type = ctx.method()
                .getParentTable()
                .getThisClass();

        return new OllirExpression("this", type, null);
    }

    private OllirExpression dealWithVariableLiteral(JmmNode jmmNode, MethodContext ctx) {
        var name = jmmNode.get("name");
        var type = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        if (type instanceof JmmStaticReferenceType) {
            return new OllirExpression(type.toOllirTypeSuffix(), null, null);
        }

        var methodSymbolTable = ctx.method();
        var parameterIndex = methodSymbolTable.getParameterIndexByName(name);

        if (parameterIndex.isPresent())
            return new OllirExpression("$%d.%s".formatted(parameterIndex.getAsInt() + 1, name), type, null);

        if (methodSymbolTable.getLocalVariableByName(name).isPresent())
            return new OllirExpression(name, type, null);

        var code = new StringBuilder();

        var classTable = methodSymbolTable.getParentTable();
        var thisRef = ollir.withType("this", classTable.getThisClass());
        var operationCode = ollir.getField(thisRef, name, type);

        var resultVar = ctx.variables().getFreeVariable();
        code.append(ollir.assignment(ollir.withType(resultVar, type), type, operationCode));

        return new OllirExpression(resultVar, type, code.toString());
    }
}
