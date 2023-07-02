package pt.up.fe.comp2023.analysis.type.visitor;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.ReportUtils;
import pt.up.fe.comp2023.analysis.context.JmmSymbol;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmStaticReferenceType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmUnknownType;
import pt.up.fe.comp2023.utils.Result;

import java.util.ArrayList;
import java.util.List;

public abstract class TypeVisitor extends AJmmVisitor<JmmSymbolTable.Method, Void> {

    private final List<Report> reports = new ArrayList<>();

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::visitAllChildren);
    }

    public List<Report> getReports() {
        return reports;
    }

    public void addErrorsToNode(JmmNode node, List<AnalysisException> exceptions) {
            List<Report> nodeReports = exceptions.stream()
                    .map(exception -> ReportUtils.newReport(node, exception))
                    .toList();

            reports.addAll(nodeReports);
    }

    public Result<JmmType, List<AnalysisException>> tryToResolveVariableByName(JmmSymbolTable.Method ctx, String name, boolean allowStaticReferences) {
        var typeInMethod = ctx.getLocalVariableByName(name)
                .or(() -> ctx.getParameterByName(name))
                .map(JmmSymbol::type)
                .orElse(null);

        if (typeInMethod != null) {
            return Result.ok(typeInMethod);
        }

        var typeInInstanceContext = ctx.getParentTable()
                .getFieldByName(name)
                .map(JmmSymbol::type)
                .orElse(null);

        if (typeInInstanceContext != null) {
            if (ctx.isStatic()) { // Contexts are incompatible
                return Result.error(List.of(AnalysisException.instanceFieldReferencedInStaticContext(name)));
            }

            return Result.ok(typeInInstanceContext);
        }

        if (allowStaticReferences) {
            var typeAsStaticReference = ctx.getParentTable()
                    .getClassesInScope()
                    .get(name);

            if (typeAsStaticReference != null) {
                return Result.ok(new JmmStaticReferenceType(typeAsStaticReference));
            }
        }

        return Result.error(List.of(AnalysisException.symbolNotFound(name)));
    }

    public Result<JmmType, List<AnalysisException>> tryToAssign(JmmType source, JmmType target, boolean allowUnknownSource) {
        if (source == null) return Result.error(List.of());

        return (allowUnknownSource && source instanceof JmmUnknownType) || source.isAssignableTo(target) ?
                Result.ok(target) : Result.error(List.of(AnalysisException.incompatibleAssignment(target, source)));
    }
}
