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

public class JmmSymbolTableMethodVisitor extends AJmmVisitor<JmmSymbolTable.Method, Void> {

    private final List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        return reports;
    }

    @Override
    protected void buildVisitor() {
        addVisit("MainMethodDeclaration", this::visitAllChildren);
        addVisit("GenericMethodDeclaration", this::visitAllChildren);
        addVisit("ParameterDeclaration", this::dealWithParameterDeclaration);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        setDefaultVisit((node, ctx) -> null);
    }

    private Void dealWithParameterDeclaration(JmmNode jmmNode, JmmSymbolTable.Method method) {
        String name = jmmNode.get("name");
        Type type = JmmNodeUtils.getTypeOfFirstChild(jmmNode);

        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);
        method.addParameter(exceptionHandler, type, name);

        return null;
    }

    private Void dealWithVarDeclaration(JmmNode jmmNode, JmmSymbolTable.Method method) {
        String name = jmmNode.get("name");
        Type type = JmmNodeUtils.getTypeOfFirstChild(jmmNode);

        Consumer<AnalysisException> exceptionHandler = createExceptionHandlerForNode(jmmNode);
        method.addLocalVariable(exceptionHandler, type, name);

        return null;
    }

    private Consumer<AnalysisException> createExceptionHandlerForNode(JmmNode jmmNode) {
        return exception -> {
            Report report = ReportUtils.newReport(jmmNode, exception);
            reports.add(report);
        };
    }
}
