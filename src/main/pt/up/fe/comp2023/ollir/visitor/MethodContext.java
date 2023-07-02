package pt.up.fe.comp2023.ollir.visitor;

import pt.up.fe.comp2023.analysis.context.table.JmmSymbolTable;
import pt.up.fe.comp2023.ollir.LabelGenerator;
import pt.up.fe.comp2023.ollir.VariableGenerator;

public record MethodContext(JmmSymbolTable.Method method, VariableGenerator variables, LabelGenerator labels) {
}
