package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTableAdapter;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.context.visitor.JmmSymbolTableVisitor;
import pt.up.fe.comp2023.analysis.type.visitor.TypeCheckingVisitor;

import java.util.ArrayList;
import java.util.List;


public class SimpleAnalyzer implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmSymbolTableVisitor symbolTableVisitor = new JmmSymbolTableVisitor();
        JmmSymbolTable symbolTable = symbolTableVisitor.visit(jmmParserResult.getRootNode());

        TypeCheckingVisitor typeCheckingVisitorVisitor = new TypeCheckingVisitor(symbolTable);
        typeCheckingVisitorVisitor.visit(jmmParserResult.getRootNode());

        List<Report> reports = new ArrayList<>();
        reports.addAll(symbolTableVisitor.getReports());
        reports.addAll(typeCheckingVisitorVisitor.getReports());

        return new JmmSemanticsResult(jmmParserResult, new JmmSymbolTableAdapter(symbolTable), reports);
    }
}