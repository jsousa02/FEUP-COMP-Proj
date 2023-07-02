package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.VarScope;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp2023.jasmin.JasminBackend;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        if (!validateConfig(config)) {
            System.exit(1);
        }

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // ... add remaining stages
        SpecsLogs.info("Code:\n\n" + code + "\n");

        SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer();
        JmmSemanticsResult analyzerResult = simpleAnalyzer.semanticAnalysis(parserResult);

        TestUtils.noErrors(analyzerResult.getReports());

        SpecsLogs.info("Symbol Table:\n\n" + analyzerResult.getSymbolTable().print());
        SpecsLogs.info("AST:\n\n" + analyzerResult.getRootNode().toTree());

        SimpleOptimizer simpleOptimizer = new SimpleOptimizer();

        JmmSemanticsResult optimizedAstResult = simpleOptimizer.optimize(analyzerResult);
        SpecsLogs.info("OPTIMIZED AST:\n\n" + optimizedAstResult.getRootNode().toTree());

        OllirResult ollirResult = simpleOptimizer.toOllir(optimizedAstResult);
        System.out.println("OLLIR RESULT:\n\n" + ollirResult.getOllirCode());

        OllirResult optimizedOllirResult = simpleOptimizer.optimize(ollirResult);
        System.out.println("OPTIMIZED OLLIR RESULT:\n\n" + optimizedOllirResult.getOllirCode());

        JasminBackend jasminBackend = new JasminBackend();

        JasminResult jasminResult = jasminBackend.toJasmin(optimizedOllirResult);
        System.out.println("JASMIN CODE: \n\n" + jasminResult.getJasminCode());

        if (config.getOrDefault("registerAllocation", "-1").equals("-1"))
            return;


        System.out.println("VARIABLE MAPPING:\n");

        var optimizedClassUnit = optimizedOllirResult.getOllirClass();
        for (Method method : optimizedClassUnit.getMethods()) {
            var optimizedVarTable = method.getVarTable();

            int maxRegister = 0;
            var mapping = new StringBuilder();

            for (var entry : optimizedVarTable.entrySet()) {
                var varName = entry.getKey();
                var descriptor = entry.getValue();

                if (descriptor.getVirtualReg() > maxRegister) {
                    maxRegister = descriptor.getVirtualReg();
                }

                mapping.append("Variable ").append(varName).append(" assigned to register #").append(descriptor.getVirtualReg()).append("\n");
            }

            System.out.println("Register allocation for method `" + method.getMethodName() + "`: " + (maxRegister + 1) + " registers are needed");
            System.out.println(mapping);
        }

    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        for (String arg : args) {
            String[] parts = arg.split("=", 2);

            String key = parts[0];
            String value = "true";
            if (parts.length > 1) {
                value = parts[1];
            }

            switch (key) {
                case "-i" -> config.put("inputFile", value);
                case "-r" -> config.put("registerAllocation", value);
                case "-o" -> config.put("optimize", value);
                case "-d" -> config.put("debug", value);
                default -> SpecsLogs.warn("Unknown option [" + key + "] was provided, ignoring...");
            }
        }

        return config;
    }

    public static void printUsage() {
        SpecsLogs.info("Usage: jmm [-r=<num>] [-o] [-d] -i=<input file.jmm>");
    }

    public static boolean validateConfig(Map<String, String> config) {
        boolean valid = true;

        if (!config.containsKey("inputFile")) {
            SpecsLogs.msgSevere("Input file [-i] was not provided");
            valid = false;
        }

        String filename = config.get("inputFile");
        File file = new File(filename);
        if (!file.exists()) {
            SpecsLogs.msgSevere("Input file [-i] doesn't exist (\"" + filename + "\" was provided)");
            valid = false;
        }

        String registerAllocation = config.get("registerAllocation");
        try {
            Integer.parseInt(registerAllocation);
        } catch (NumberFormatException e) {
            SpecsLogs.msgSevere("Invalid number of register allocations [-r] (\"" + registerAllocation + "\" was provided)");
            valid = false;
        }

        String optimize = config.get("optimize");
        if (!optimize.equals("true") && !optimize.equals("false")) {
            SpecsLogs.msgSevere("Optimize flag [-o] doesn't have a boolean value (\"" + optimize + "\" was provided)");
            valid = false;
        }

        String debug = config.get("debug");
        if (!debug.equals("true") && !debug.equals("false")) {
            SpecsLogs.msgSevere("Debug flag [-d] doesn't have a boolean value (\"" + debug + "\" was provided)");
            valid = false;
        }

        if (!valid) {
            printUsage();
            return false;
        }

        return true;
    }

}
