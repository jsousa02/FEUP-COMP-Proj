package pt.up.fe.comp2023.ollir.visitor;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.type.primitives.JmmArrayType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmStaticReferenceType;
import pt.up.fe.comp2023.ollir.OllirExpression;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.analysis.context.JmmSymbol;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.type.primitives.JmmClassType;
import pt.up.fe.comp2023.ollir.LabelGenerator;
import pt.up.fe.comp2023.ollir.OllirGenerator;
import pt.up.fe.comp2023.ollir.VariableGenerator;

public class OllirVisitor extends AJmmVisitor<MethodContext, String> {

    private final OllirGenerator ollir = new OllirGenerator();
    private final OllirExpressionVisitor expressionVisitor = new OllirExpressionVisitor();
    private final JmmSymbolTable symbolTable;

    public OllirVisitor(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MainMethodDeclaration", this::dealWithMainMethodDeclaration);
        addVisit("GenericMethodDeclaration", this::dealWithGenericMethodDeclaration);
        addVisit("ParameterDeclaration", this::dealWithParameterDeclaration);
        addVisit("BlockStatement", this::dealWithBlockStatement);
        addVisit("ExpressionStatement", this::dealWithExpressionStatement);
        addVisit("VariableAssignmentStatement", this::dealWithVariableAssignmentStatement);
        addVisit("ArrayIndexAssignmentStatement", this::dealWithArrayIndexAssignmentStatement);
        addVisit("IfStatement", this::dealWithIfStatement);
        addVisit("WhileStatement", this::dealWithWhileStatement);
        setDefaultVisit((node, ctx) -> "");
    }

    private String dealWithProgram(JmmNode jmmNode, MethodContext ctx) {
        var code = new StringBuilder();

        for (JmmClassType importedClass : symbolTable.getImports().values()) {
            code.append("import ").append(importedClass.getName()).append(";\n");
        }

        code.append('\n');

        var classNode = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);
        code.append(visit(classNode, ctx));

        return code.toString();
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, MethodContext ctx) {

        JmmClassType thisClass = symbolTable.getThisClass();
        var extendsSuperClass = thisClass.hasSuperClass() && !thisClass.getSuperClass().equals(JmmClassType.OBJECT)
                ? "extends %s ".formatted(thisClass.getSuperClass().getSimpleName()) : "";

        var code = new StringBuilder();
        for (var childNode : jmmNode.getChildren()) {
            code.append(visit(childNode, ctx).indent(4));
        }

        return """
                %s %s{
                %s
                    .construct %1$s().V {
                        invokespecial(this, "<init>").V;
                    }
                }
                """
                .formatted(thisClass.getSimpleName(), extendsSuperClass, code.toString())
                .stripIndent();
    }

    private String dealWithVarDeclaration(JmmNode jmmNode, MethodContext ctx) {
        if (ctx != null) return "";

        var name = jmmNode.get("name");
        var symbol = symbolTable.getFieldByName(name).orElseThrow();

        return "\n.field public %s;".formatted(ollir.withType(symbol.name(), symbol.type()));
    }

    private String dealWithMainMethodDeclaration(JmmNode jmmNode, MethodContext ctx) {
        var methodName = jmmNode.get("methodName");
        var method = symbolTable.getMethodByName(methodName).orElseThrow();

        var modifiers = new StringBuilder();
        var isPublic = jmmNode.getObject("isPublic", Boolean.class);
        if (isPublic) modifiers.append(" public");
        modifiers.append(" static");

        var argsName = jmmNode.get("arrayName");
        var methodHeader = ".method%s main(%s.array.String)"
                .formatted(modifiers.toString(), argsName);

        var childContext = new MethodContext(method, new VariableGenerator(), new LabelGenerator());

        var code = new StringBuilder();
        for (var childNode : jmmNode.getChildren()) {
            code.append(visit(childNode, childContext).indent(4));
        }

        return """
               
               %s {
               %s
                   ret.V;
               
               }
               """
                .formatted(ollir.withType(methodHeader, method.getReturnType()), code.toString())
                .stripIndent();
    }

    private String dealWithGenericMethodDeclaration(JmmNode jmmNode, MethodContext ctx) {
        var methodName = jmmNode.get("methodName");
        var method = symbolTable.getMethodByName(methodName).orElseThrow();

        var modifiers = new StringBuilder();
        var isPublic = jmmNode.getObject("isPublic", Boolean.class);
        if (isPublic) modifiers.append(" public");

        var childContext = new MethodContext(method, new VariableGenerator(), new LabelGenerator());

        var header = new StringBuilder();

        if (!method.getParameters().isEmpty()) {
            header.append(visit(jmmNode.getJmmChild(1), childContext));

            for (int i = 2; i < method.getParameters().size() + 1; i++) {
                var childNode = jmmNode.getJmmChild(i);
                header.append(", ").append(visit(childNode, childContext));
            }
        }

        var methodHeader = ".method%s %s(%s)"
                .formatted(modifiers.toString(), methodName, header.toString());

        var code = new StringBuilder();

        for (int i = method.getParameters().size() + 1; i < jmmNode.getNumChildren() - 1; i++) {
            var childNode = jmmNode.getJmmChild(i);
            code.append(visit(childNode, childContext).indent(4));
        }

        var returnChild = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);
        var returnResult = expressionVisitor.visit(returnChild, childContext);

        returnResult.getCode().ifPresent(c -> code.append('\n').append(c.indent(4)));
        code.append(ollir.withType("\n    ret", method.getReturnType())).append(' ').append(returnResult.getReference(ollir)).append(";\n");

        return """
               
               %s {
               %s
               }
               """
                .formatted(ollir.withType(methodHeader, method.getReturnType()), code.toString())
                .stripIndent();
    }

    private String dealWithParameterDeclaration(JmmNode jmmNode, MethodContext ctx) {
        String name = jmmNode.get("name");
        JmmSymbol parameter = ctx.method().getParameterByName(name).orElseThrow();

        return ollir.withType(parameter.name(), parameter.type());
    }

    private String dealWithBlockStatement(JmmNode jmmNode, MethodContext ctx) {
        var code = new StringBuilder();
        for (var node : jmmNode.getChildren()) {
            code.append(visit(node, ctx));
        }

        return code.toString();
    }

    private String dealWithExpressionStatement(JmmNode jmmNode, MethodContext ctx) {
        var expression = expressionVisitor.visit(jmmNode.getJmmChild(0), ctx);
        return expression.getCode().map(code -> '\n' + code).orElse("");
    }

    private String dealWithVariableAssignmentStatement(JmmNode jmmNode, MethodContext ctx) {
        var name = jmmNode.get("name");
        var type = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var expression = expressionVisitor.visit(jmmNode.getJmmChild(0), ctx);
        var tempReference = expression.getReference(ollir);

        var methodSymbolTable = ctx.method();

        var parameterIndex = methodSymbolTable.getParameterIndexByName(name);
        if (parameterIndex.isPresent() || methodSymbolTable.getLocalVariableByName(name).isPresent()) {
            var permanentReference = parameterIndex.isPresent()
                    ? ollir.withType("$%d.%s".formatted(parameterIndex.getAsInt() + 1, name), type)
                    : ollir.withType(name, type);

            var assignmentCode = expression.getCode()
                    .map(expressionCode -> expressionCode.replaceAll(tempReference, permanentReference))
                    .orElse(ollir.assignment(permanentReference, type, tempReference));

            return '\n' + assignmentCode;
        }

        // FIELD
        var code = new StringBuilder();
        expression.getCode().ifPresent(code::append);

        var classTable = methodSymbolTable.getParentTable();
        var thisRef = ollir.withType("this", classTable.getThisClass());
        code.append(ollir.putField(thisRef, ollir.withType(name, type), expression.getReference(ollir)));

        return '\n' + code.toString();
    }

    private String dealWithArrayIndexAssignmentStatement(JmmNode jmmNode, MethodContext ctx) {
        var name = jmmNode.get("name");
        var type = JmmNodeUtils.getNodeType(jmmNode).orElseThrow();

        var code = new StringBuilder();

        var indexExpr = expressionVisitor.visit(jmmNode.getJmmChild(0), ctx);
        var elementExpr = expressionVisitor.visit(jmmNode.getJmmChild(1), ctx);

        indexExpr.getCode().ifPresent(code::append);
        elementExpr.getCode().ifPresent(code::append);

        var methodSymbolTable = ctx.method();

        var parameterIndex = methodSymbolTable.getParameterIndexByName(name);
        if (parameterIndex.isPresent() || methodSymbolTable.getLocalVariableByName(name).isPresent()) {
            var reference = parameterIndex.isPresent()
                    ? "$%d.%s".formatted(parameterIndex.getAsInt() + 1, name)
                    : name;

            var arrayIndex= ollir.arrayIndex(reference, indexExpr.getReference(ollir), type);
            code.append(ollir.assignment(arrayIndex, type, elementExpr.getReference(ollir)));
            return '\n' + code.toString();
        }

        // FIELD
        var classTable = methodSymbolTable.getParentTable();
        var thisRef = ollir.withType("this", classTable.getThisClass());

        var arrayType = new JmmArrayType<>(type);

        var getfield = ollir.getField(thisRef, name, arrayType);
        var localArray = ctx.variables().getFreeVariable();
        code.append(ollir.assignment(ollir.withType(localArray, arrayType), type, getfield));

        var arrayIndex = ollir.arrayIndex(localArray, indexExpr.getReference(ollir), type);
        code.append(ollir.assignment(arrayIndex, type, elementExpr.getReference(ollir)));

        return '\n' + code.toString();
    }

    private String dealWithIfStatement(JmmNode jmmNode, MethodContext ctx) {
        var conditionNode = jmmNode.getJmmChild(0);
        var conditionExpr = expressionVisitor.visit(conditionNode, ctx);

        var code = new StringBuilder();
        conditionExpr.getCode().ifPresent(code::append);

        code.append('\n');

        var ifLabels = ctx.labels().newIf();
        code.append(ollir.condGoto(conditionExpr.getReference(ollir), ifLabels.getIf()));

        // IF FALSE
//        code.append(ollir.label(ifLabels.getElse()));

        if (jmmNode.getNumChildren() > 2) {
            code.append(visit(jmmNode.getJmmChild(2), ctx).indent(4));
            code.append('\n');
        }

        code.append(ollir.uncondGoto(ifLabels.getEndIf()));

        // IF TRUE
        code.append('\n').append(ollir.label(ifLabels.getIf()));
        code.append(visit(jmmNode.getJmmChild(1), ctx).indent(4));

        // END
        code.append('\n').append(ollir.label(ifLabels.getEndIf()));

        return '\n' + code.toString();
    }

    private String dealWithWhileStatement(JmmNode jmmNode, MethodContext ctx) {
        var conditionNode = jmmNode.getJmmChild(0);
        var conditionExpr = expressionVisitor.visit(conditionNode, ctx);

        var whileLabels = ctx.labels().newWhile();

        var code = new StringBuilder();

        // WHILE CONDITION
        code.append(ollir.label(whileLabels.getWhileCond()));
        conditionExpr.getCode().ifPresent(conditionCode -> code.append('\n').append(conditionCode.indent(4)).append('\n'));

        // IF CONDITION IS TRUE, LOOP
        code.append(ollir.condGoto(conditionExpr.getReference(ollir), whileLabels.getWhileLoop()));
        // ELSE END WHILE
        code.append(ollir.uncondGoto(whileLabels.getWhileEnd()));

        // WHILE LOOP
        code.append('\n').append(ollir.label(whileLabels.getWhileLoop()));
        code.append(visit(jmmNode.getJmmChild(1), ctx).indent(4));

        // GO BACK TO START
        code.append('\n').append(ollir.uncondGoto(whileLabels.getWhileCond()));

        // WHILE END
        code.append(ollir.label(whileLabels.getWhileEnd()));

        return '\n' + code.toString();
    }
}
