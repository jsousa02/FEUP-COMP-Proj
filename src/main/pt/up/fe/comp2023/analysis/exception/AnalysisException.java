package pt.up.fe.comp2023.analysis.exception;

import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

public class AnalysisException extends Exception {

    public enum Severity {
        WARNING, ERROR;

    }
    private final Severity severity;

    public AnalysisException(Severity severity, String message) {
        super(message);
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }

    public static AnalysisException warning(String message) {
        return new AnalysisException(Severity.WARNING, message);
    }

    public static AnalysisException error(String message) {
        return new AnalysisException(Severity.ERROR, message);
    }

    public static AnalysisException duplicatedImport(String importFqn) {
        return warning("Duplicated import for class `%s`".formatted(importFqn));
    }

    public static AnalysisException conflictingImport(String importSimpleName) {
        return error("Conflicting imports for symbol `%s`".formatted(importSimpleName));
    }

    public static AnalysisException cyclicExtension() {
        return error("Cyclic extensions are not allowed");
    }

    public static AnalysisException symbolNotFound(String symbolName) {
        return error("Could not find symbol `%s`".formatted(symbolName));
    }

    public static AnalysisException badMainMethodName() {
        return error("Only the main method can be static");
    }

    public static AnalysisException badMainMethodArguments() {
        return error("The single argument of the main method must be of type String[]");
    }

    public static AnalysisException incompatibleAssignment(String expected, JmmType actual) {
        return error("Expected %s, found `%s`".formatted(expected, actual.getName()));
    }

    public static AnalysisException incompatibleAssignment(JmmType expected, JmmType actual) {
        return incompatibleAssignment("`%s`".formatted(expected.getName()), actual);
    }

    public static AnalysisException methodIncorrectNumberOfArguments(int expectedNumArgs, int actualNumArgs) {
        return error("Wrong number of arguments provided, expected `%d` but found `%d`".formatted(expectedNumArgs, actualNumArgs));
    }

    public static AnalysisException variableAlreadyDefinedInScope(String name) {
        return error("Variable `%s` is already defined in this scope".formatted(name));
    }

    public static AnalysisException methodAlreadyDefined(String name) {
        return error("Method `%s` is already defined in this class".formatted(name));
    }

    public static AnalysisException instanceMethodReferencedInStaticContext(String name) {
        return error("Non-static method `%s` cannot be referenced in a static context".formatted(name));
    }

    public static AnalysisException instanceFieldReferencedInStaticContext(String name) {
        return error("Non-static method `%s` cannot be referenced in a static context".formatted(name));
    }

    public static AnalysisException thisReferencedInStaticContext() {
        return error("`this` cannot be referenced in a static context");
    }
}
