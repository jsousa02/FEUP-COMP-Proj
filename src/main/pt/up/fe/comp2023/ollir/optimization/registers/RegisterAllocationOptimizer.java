package pt.up.fe.comp2023.ollir.optimization.registers;

import org.specs.comp.ollir.OllirErrorException;
import org.specs.comp.ollir.Operand;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.ollir.optimization.registers.LivenessAnalyzer;
import pt.up.fe.specs.util.SpecsLogs;

import java.util.ArrayList;

public class RegisterAllocationOptimizer {

    public OllirResult optimize(OllirResult ollirResult) {
        int maxRegisters = Integer.parseInt(ollirResult.getConfig().getOrDefault("registerAllocation", "-1"));
        if (maxRegisters == -1)
            return ollirResult;

        var classUnit = ollirResult.getOllirClass();

        classUnit.buildCFGs();

        var liveness = new LivenessAnalyzer();
        var interference = new InterferenceAnalyzer();
        var colorizer = new GraphColorizer();

        int effectiveMaxRegisters = 0;

        for (var method : classUnit.getMethods()) {
            var livenessResult = liveness.analyze(method);
            var interferenceResult = interference.analyze(method, livenessResult);
            var colorizerResult = colorizer.analyze(interferenceResult);

            int minUsableRegister = method.getParams().size() + (method.isStaticMethod() ? 0 : 1);
            int neededRegisters = minUsableRegister + colorizerResult.first();

            effectiveMaxRegisters = Math.max(effectiveMaxRegisters, neededRegisters);
            var registerMapping = colorizerResult.second();

            var regTable = method.getVarTable();
            registerMapping.forEach(data -> {
                var currentDescriptor = regTable.get(data.getName());
                var register = minUsableRegister + data.getRegister().orElseThrow();

                currentDescriptor.setVirtualReg(register);
            });
        }

        if (effectiveMaxRegisters > maxRegisters) {
            var report = new Report(maxRegisters == 0 ? ReportType.DEBUG : ReportType.ERROR,
                    Stage.OPTIMIZATION, -1, "The minimum number of registers needed is %d".formatted(effectiveMaxRegisters));

            ollirResult.getReports().add(report);
        }

        return ollirResult;
    }
}
