package pt.up.fe.comp2023.analysis.context.visitor;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.ReportUtils;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JmmSymbolTableVisitor extends AJmmVisitor<JmmSymbolTable, JmmSymbolTable> {

    private final List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImports);
        addVisit("ClassDeclaration", this::dealWithClass);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("MainMethodDeclaration", this::dealWithMainMethodDeclaration);
        addVisit("GenericMethodDeclaration", this::dealWithGenericMethodDeclaration);
        setDefaultVisit((node, s) -> null);
    }

    public JmmSymbolTable dealWithProgram(JmmNode jmmNode, JmmSymbolTable ignored) {
        JmmNode classNode = jmmNode.getJmmChild(jmmNode.getNumChildren() - 1);

        String className = classNode.get("name");
        JmmSymbolTable symbolTable = new JmmSymbolTable(className);

        visitAllChildren(jmmNode, symbolTable);
        return symbolTable;
    }

    private JmmSymbolTable dealWithImports(JmmNode jmmNode, JmmSymbolTable symbolTable) {
        List<String> path = jmmNode.getObjectAsList("path", String.class);

        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);
        symbolTable.addImport(exceptionHandler, path);

        return null;
    }

    private JmmSymbolTable dealWithClass(JmmNode jmmNode, JmmSymbolTable symbolTable) {
        if(jmmNode.hasAttribute("superName")) {
            Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);
            symbolTable.setSuperClass(exceptionHandler, jmmNode.get("superName"));
        }

        visitAllChildren(jmmNode, symbolTable);
        return null;
    }

    private JmmSymbolTable dealWithVarDeclaration(JmmNode jmmNode, JmmSymbolTable symbolTable) {
        String name = jmmNode.get("name");
        Type type = JmmNodeUtils.getTypeOfFirstChild(jmmNode);

        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);
        symbolTable.addField(exceptionHandler, type, name);

        return null;
    }

    private JmmSymbolTable dealWithMainMethodDeclaration(JmmNode jmmNode, JmmSymbolTable symbolTable) {
        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);

        String methodName = jmmNode.get("methodName");
        String arrayType = jmmNode.get("arrayType");

        JmmSymbolTable.Method method = symbolTable.createMethod(exceptionHandler, methodName, new Type("void", false), true);

        String arrayName = jmmNode.get("arrayName");
        Type parameterType = new Type(arrayType, true);

        method.addParameter(exceptionHandler, parameterType, arrayName);

        JmmSymbolTableMethodVisitor methodVisitor = new JmmSymbolTableMethodVisitor();
        methodVisitor.visit(jmmNode, method);

        reports.addAll(methodVisitor.getReports());
        return null;
    }

    private JmmSymbolTable dealWithGenericMethodDeclaration(JmmNode jmmNode, JmmSymbolTable symbolTable) {
        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);

        String methodName = jmmNode.get("methodName");
        Type returnType = JmmNodeUtils.getTypeOfFirstChild(jmmNode);

        JmmSymbolTable.Method method = symbolTable.createMethod(exceptionHandler, methodName, returnType, false);

        JmmSymbolTableMethodVisitor methodVisitor = new JmmSymbolTableMethodVisitor();
        methodVisitor.visit(jmmNode, method);

        reports.addAll(methodVisitor.getReports());
        return null;
    }

    private Consumer<AnalysisException> createExceptionHandlerForNode(JmmNode jmmNode) {
        return exception -> {
            Report report = ReportUtils.newReport(jmmNode, exception);
            reports.add(report);
        };
    }
}
