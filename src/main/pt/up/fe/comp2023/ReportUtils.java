package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;

public class ReportUtils {

    public static Report newReport(ReportType type, Stage stage, JmmNode jmmNode, String message) {
        int line = Integer.parseInt(jmmNode.get("lineStart"));
        int column = Integer.parseInt(jmmNode.get("colStart"));
        return new Report(type, stage, line, column, message);
    }

    public static Report newReport(JmmNode jmmNode, AnalysisException exception) {
        ReportType type = switch (exception.getSeverity()) {
            case WARNING -> ReportType.WARNING;
            case ERROR -> ReportType.ERROR;
        };

        return ReportUtils.newReport(type, Stage.SYNTATIC, jmmNode, exception.getMessage());
    }

    public static Report newLog(Stage stage, JmmNode jmmNode, String message) {
        return ReportUtils.newReport(ReportType.LOG, stage, jmmNode, message);
    }

    public static Report newDebug(Stage stage, JmmNode jmmNode, String message) {
        return ReportUtils.newReport(ReportType.DEBUG, stage, jmmNode, message);
    }

    public static Report newWarning(Stage stage, JmmNode jmmNode, String message) {
        return ReportUtils.newReport(ReportType.WARNING, stage, jmmNode, message);
    }

    public static Report newError(Stage stage, JmmNode jmmNode, String message) {
        return ReportUtils.newReport(ReportType.ERROR, stage, jmmNode, message);
    }
}
