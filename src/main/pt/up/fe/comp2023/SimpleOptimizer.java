package pt.up.fe.comp2023;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Operand;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTableAdapter;
import pt.up.fe.comp2023.ollir.optimization.constants.visitor.ConstantsOptimizer;
import pt.up.fe.comp2023.ollir.optimization.registers.RegisterAllocationOptimizer;
import pt.up.fe.comp2023.ollir.visitor.OllirVisitor;

import java.util.Collections;

public class SimpleOptimizer implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        var adapter = (JmmSymbolTableAdapter) semanticsResult.getSymbolTable();
        var visitor = new OllirVisitor(adapter.getJmmSymbolTable());

        var ollirGeneratedCode = visitor.visit(semanticsResult.getRootNode());
        return new OllirResult(semanticsResult, ollirGeneratedCode, Collections.emptyList());
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        var config = semanticsResult.getConfig();
        var optimizeFlag = config.getOrDefault("optimize", "false")
                .equals("true");

        if (!optimizeFlag)
            return semanticsResult;

        var optimizer = new ConstantsOptimizer();

        var rootNode = semanticsResult.getRootNode();
        optimizer.visit(rootNode);

        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        var optimizer = new RegisterAllocationOptimizer();
        return optimizer.optimize(ollirResult);
    }
}
