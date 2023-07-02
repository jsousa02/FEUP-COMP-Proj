package pt.up.fe.comp2023.analysis.type.visitor;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;
import pt.up.fe.comp2023.analysis.type.primitives.*;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmStaticReferenceType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmUnknownType;
import pt.up.fe.comp2023.utils.Pair;

import java.util.List;
import java.util.Optional;

public class TypeCheckingVisitor extends TypeVisitor {

    private final JmmSymbolTable symbolTable;

    public TypeCheckingVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        super.buildVisitor();

        // Method Declarations
        addVisit("MainMethodDeclaration", this::dealWithMainMethodDeclaration);
        addVisit("GenericMethodDeclaration", this::dealWithGenericMethodDeclaration);

        // Statements
        addVisit("IfStatement", this::dealWithConditionalBlocks);
        addVisit("WhileStatement", this::dealWithConditionalBlocks);
        addVisit("VariableAssignmentStatement", this::dealWithAssignments);
        addVisit("ArrayIndexAssignmentStatement", this::dealWithArrayIndexedWrites);

        // Expressions
        addVisit("ParenthesisExpression", this::dealWithParentheses);
        addVisit("ArrayIndexExpression", this::dealWithArrayIndexedReads);
        addVisit("MethodCallExpression", this::dealWithMethodCalls);
        addVisit("PropertyAccessExpression", this::dealWithPropertyAccesses);
        addVisit("UnaryOp", this::dealWithOperations);
        addVisit("BinaryOp", this::dealWithOperations);
        addVisit("ArrayInitializationExpression", this::dealWithArrayInitializations);
        addVisit("ObjectInitializationExpression", this::dealWithObjectInitializations);
        addVisit("IntegerLiteral", this::dealWithIntegerLiterals);
        addVisit("BooleanLiteral", this::dealWithBooleanLiterals);
        addVisit("ThisLiteral", this::dealWithThisLiterals);
        addVisit("VariableLiteral", this::dealWithVariableLiterals);
    }

    // Method Declarations

    private Void dealWithMainMethodDeclaration(JmmNode node, JmmSymbolTable.Method ctx) {
        var methodName = node.get("methodName");
        if (!methodName.equals("main")) {
            addErrorsToNode(node, List.of(AnalysisException.badMainMethodName()));
            return null;
        }

        var arrayType = node.get("arrayType");
        if (!arrayType.equals("String")) {
            addErrorsToNode(node, List.of(AnalysisException.badMainMethodArguments()));
            return null;
        }

        var method = symbolTable.getMethodByName(methodName).orElse(null);
        visitAllChildren(node, method);

        return null;
    }

    private Void dealWithGenericMethodDeclaration(JmmNode node, JmmSymbolTable.Method ctx) {
        var methodName = node.get("methodName");
        var method = symbolTable.getMethodByName(methodName).orElse(null);

        if (method == null) {
            return null;
        }

        visitAllChildren(node, method);

        var returnExpr = node.getJmmChild(node.getNumChildren() - 1);
        var returnType = JmmNodeUtils.getNodeType(returnExpr).orElse(null);

        if (returnType == null) {
            return null;
        }

        tryToAssign(returnType, method.getReturnType(), true)
                .inspect(type -> JmmNodeUtils.setNodeType(node, type))
                .inspectError(errors -> addErrorsToNode(node, errors));

        return null;
    }

    // Statements

    private Void dealWithConditionalBlocks(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var conditionExpr = node.getJmmChild(0);
        var conditionType = JmmNodeUtils.getNodeType(conditionExpr).orElse(null);

        tryToAssign(conditionType, JmmBooleanType.getInstance(), false)
                .inspect(type -> JmmNodeUtils.setNodeType(node, type))
                .inspectError(errors -> addErrorsToNode(node, errors));

        return null;
    }

    private Void dealWithAssignments(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var variableName = node.get("name");
        var variableResult = tryToResolveVariableByName(ctx, variableName, false);

        if (variableResult.isError()) {
            addErrorsToNode(node, variableResult.unwrapErr());
            return null;
        }

        var sourceExpr = node.getJmmChild(0);
        var sourceType = JmmNodeUtils.getNodeType(sourceExpr).orElse(null);

        tryToAssign(sourceType, variableResult.unwrap(), true)
                .inspect(type -> JmmNodeUtils.setNodeType(node, type))
                .inspectError(errors -> addErrorsToNode(sourceExpr, errors));

        return null;
    }

    private Void dealWithArrayIndexedWrites(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var indexedVariableName = node.get("name");
        var indexedVariableResult = tryToResolveVariableByName(ctx, indexedVariableName, false);

        if (indexedVariableResult.isError()) {
            addErrorsToNode(node, indexedVariableResult.unwrapErr());
            return null;
        }

        var indexedType = indexedVariableResult.unwrap();
        if (!(indexedType instanceof JmmArrayType<?> arrayType)) {
            addErrorsToNode(node, List.of(AnalysisException.incompatibleAssignment("array", indexedType)));
            return null;
        }

        var indexExpr = node.getJmmChild(0);
        var indexType = JmmNodeUtils.getNodeType(indexExpr).orElse(null);

        var indexAssignmentResult = tryToAssign(indexType, JmmIntType.getInstance(), false);

        if (indexAssignmentResult.isError()) {
            addErrorsToNode(indexExpr, indexAssignmentResult.unwrapErr());
            return null;
        }

        var sourceExpr = node.getJmmChild(1);
        var sourceType = JmmNodeUtils.getNodeType(sourceExpr).orElse(null);

        tryToAssign(sourceType, arrayType.getElementType(), true)
                .inspect(type -> JmmNodeUtils.setNodeType(node, type))
                .inspectError(errors -> addErrorsToNode(sourceExpr, errors));

        return null;
    }

    // Expressions

    private Void dealWithParentheses(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var childExpr = node.getJmmChild(0);
        JmmNodeUtils.getNodeType(childExpr)
                .ifPresent(childType -> JmmNodeUtils.setNodeType(node, childType));

        return null;
    }

    private Void dealWithArrayIndexedReads(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var indexedExpr = node.getJmmChild(0);
        var indexedType = JmmNodeUtils.getNodeType(indexedExpr).orElse(null);

        if (indexedType == null) {
            return null;
        }

        if (!(indexedType instanceof JmmArrayType<?> arrayType)) {
            addErrorsToNode(indexedExpr,
                    List.of(AnalysisException.incompatibleAssignment("array", indexedType)));

            return null;
        }

        var indexExpr = node.getJmmChild(1);
        var indexType = JmmNodeUtils.getNodeType(indexExpr).orElse(null);

        tryToAssign(indexType, JmmIntType.getInstance(), false)
                .inspect(type -> JmmNodeUtils.setNodeType(node, arrayType.getElementType()))
                .inspectError(errors -> addErrorsToNode(indexExpr, errors));

        return null;
    }

    private Void dealWithMethodCalls(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        String methodName = node.get("name");

        JmmNode calleeExpr = node.getJmmChild(0);
        Optional<Pair<JmmClassType, Boolean>> accessedClassAndIsStatic = JmmNodeUtils.getNodeType(calleeExpr)
                .map(type -> {
                    if (type instanceof JmmClassType jmmClassType) {
                        return new Pair<>(jmmClassType, false);
                    }

                    if (type instanceof JmmStaticReferenceType jmmStaticReferenceType) {
                        return new Pair<>(jmmStaticReferenceType.getClassType(), true);
                    }

                    addErrorsToNode(calleeExpr,
                            List.of(AnalysisException.incompatibleAssignment("object or static reference", type)));

                    return null;
                });

        if (accessedClassAndIsStatic.isEmpty())
            return null;

        JmmClassType accessedClass = accessedClassAndIsStatic.get().first();
        boolean isAccessStatic = accessedClassAndIsStatic.get().second();

        JmmType returnType = null;
        JmmSymbolTable.Method method = null;

        if (!accessedClass.hasWellKnownStructure()) {
            returnType = JmmUnknownType.getInstance();
        } else if (accessedClass.equals(ctx.getParentTable().getThisClass())) {
            Optional<JmmSymbolTable.Method> resolvedMethod = ctx.getParentTable().getMethodByName(methodName);
            if (resolvedMethod.isPresent()) {
                method = resolvedMethod.get();
                returnType = method.getReturnType();
            }
        }

        if (returnType == null) {
            addErrorsToNode(node,
                    List.of(AnalysisException.symbolNotFound("%s#%s".formatted(accessedClass.getSimpleName(), methodName))));

            return null;
        }

        JmmNodeUtils.setNodeType(node, returnType);
        if (method == null) {
            return null;
        }

        if (!method.isStatic() && isAccessStatic) {
            addErrorsToNode(node,
                    List.of(AnalysisException.instanceMethodReferencedInStaticContext(methodName)));
        }

        int numParametersGiven = node.getNumChildren() - 1;
        if (numParametersGiven != method.getParameters().size()) {
            addErrorsToNode(node, List.of(
                    AnalysisException.methodIncorrectNumberOfArguments(method.getParameters().size(), numParametersGiven)));
        }

        for (int i = 0; i < Math.min(method.getParameters().size(), numParametersGiven); i++) {
            JmmType parameterType = method.getParameters().get(i).type();
            JmmNode argumentExpr = node.getJmmChild(i + 1);

            JmmNodeUtils.getNodeType(argumentExpr)
                    .map(argumentExprType -> {
                        if (!argumentExprType.isAssignableTo(parameterType)) {
                            addErrorsToNode(argumentExpr, List.of(AnalysisException.incompatibleAssignment(parameterType, argumentExprType)));
                            return null;
                        }

                        return argumentExprType;
                    });
        }

        return null;
    }

    private Void dealWithPropertyAccesses(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        JmmNode arrayExpr = node.getJmmChild(0);
        Optional<JmmArrayType<?>> arrayType = JmmNodeUtils.getNodeType(arrayExpr)
                .map(arrayExprType -> {
                    if (!(arrayExprType instanceof JmmArrayType<?> jmmArrayType)) {
                        addErrorsToNode(arrayExpr,
                                List.of(AnalysisException.incompatibleAssignment("array", arrayExprType)));

                        return null;
                    }

                    return jmmArrayType;
                });

        if (arrayType.isEmpty()) {
            return null;
        }

        String property = node.get("name");
        if (!property.equals("length")) {
            addErrorsToNode(node,
                    List.of(AnalysisException.symbolNotFound("%s#%s".formatted(arrayType.get().getName(), property))));

            return null;
        }

        JmmNodeUtils.setNodeType(node, JmmIntType.getInstance());
        return null;
    }

    private Pair<JmmType, JmmType> getOperationDescription(String op) {
        return switch (op) {
            case "+", "-", "*", "/" -> Pair.of(JmmIntType.getInstance(), JmmIntType.getInstance());
            case "<" -> Pair.of(JmmIntType.getInstance(), JmmBooleanType.getInstance());
            case "!", "&&" -> Pair.of(JmmBooleanType.getInstance(), JmmBooleanType.getInstance());
            default -> throw new IllegalArgumentException("Unrecognized operation: " + op);
        };
    }

    private Void dealWithOperations(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var operation = getOperationDescription(node.get("op"));
        var validOperandType = operation.first();

        boolean allValid = true;
        for (var child : node.getChildren()) {
            var childType = JmmNodeUtils.getNodeType(child).orElse(null);

            var childAssignmentResult = tryToAssign(childType, validOperandType, false)
                    .inspectError(errors -> addErrorsToNode(node, errors));

            allValid = allValid && childAssignmentResult.isOk();
        }

        if (allValid) {
            JmmNodeUtils.setNodeType(node, operation.second());
        }

        return null;
    }

    private Void dealWithObjectInitializations(JmmNode node, JmmSymbolTable.Method ctx) {
        String className = node.get("className");

        JmmClassType classType = ctx.getParentTable()
                .getClassesInScope()
                .get(className);

        if (classType != null) {
            JmmNodeUtils.setNodeType(node, classType);
        }

        return null;
    }

    private Void dealWithArrayInitializations(JmmNode node, JmmSymbolTable.Method ctx) {
        visitAllChildren(node, ctx);

        ///

        var indexExpr = node.getJmmChild(0);
        var indexType = JmmNodeUtils.getNodeType(indexExpr).orElse(null);

        tryToAssign(indexType, JmmIntType.getInstance(), false)
                .inspect(type -> JmmNodeUtils.setNodeType(node, new JmmArrayType<>(JmmIntType.getInstance())))
                .inspectError(errors -> addErrorsToNode(node, errors));

        return null;
    }

    private Void dealWithIntegerLiterals(JmmNode node, JmmSymbolTable.Method ctx) {
        JmmNodeUtils.setNodeType(node, JmmIntType.getInstance());
        return null;
    }

    private Void dealWithBooleanLiterals(JmmNode node, JmmSymbolTable.Method ctx) {
        JmmNodeUtils.setNodeType(node, JmmBooleanType.getInstance());
        return null;
    }

    private Void dealWithThisLiterals(JmmNode node, JmmSymbolTable.Method ctx) {
        if (ctx.isStatic()) {
            var errors = List.of(AnalysisException.thisReferencedInStaticContext());
            addErrorsToNode(node, errors);

            return null;
        }

        var thisClass = ctx.getParentTable().getThisClass();
        JmmNodeUtils.setNodeType(node, thisClass);

        return null;
    }

    private Void dealWithVariableLiterals(JmmNode node, JmmSymbolTable.Method ctx) {
        var name = node.get("name");

        tryToResolveVariableByName(ctx, name, true)
                .inspect(type -> JmmNodeUtils.setNodeType(node, type))
                .inspectError(errors -> addErrorsToNode(node, errors));

        return null;
    }
}
