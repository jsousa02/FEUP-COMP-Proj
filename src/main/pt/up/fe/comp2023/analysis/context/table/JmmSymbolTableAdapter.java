package pt.up.fe.comp2023.analysis.context.table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2023.analysis.context.JmmSymbolUtils;
import pt.up.fe.comp2023.analysis.type.JmmTypeUtils;
import pt.up.fe.comp2023.analysis.type.primitives.JmmClassType;

import java.util.List;

public class JmmSymbolTableAdapter implements SymbolTable {

    private final JmmSymbolTable symbolTable;

    public JmmSymbolTableAdapter(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<String> getImports() {
        return symbolTable.getImports().values()
                .stream()
                .map(JmmClassType::getName)
                .toList();
    }

    @Override
    public String getClassName() {
        return JmmTypeUtils.toType(symbolTable.getThisClass()).getName();
    }

    @Override
    public String getSuper() {
        return JmmTypeUtils.toType(symbolTable.getThisClass().getSuperClass()).getName();
    }

    @Override
    public List<Symbol> getFields() {
        return symbolTable.getFields()
                .stream()
                .map(JmmSymbolUtils::toSymbol)
                .toList();
    }

    @Override
    public List<String> getMethods() {
        return symbolTable.getMethods()
                .stream()
                .map(JmmSymbolTable.Method::getName)
                .toList();
    }

    @Override
    public Type getReturnType(String s) {
        return symbolTable.getMethodByName(s)
                .map(JmmSymbolTable.Method::getReturnType)
                .map(JmmTypeUtils::toType)
                .orElse(null);
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return symbolTable.getMethodByName(s)
                .map(JmmSymbolTable.Method::getParameters)
                .map(listOfSymbols -> listOfSymbols.stream()
                        .map(JmmSymbolUtils::toSymbol)
                        .toList())
                .orElse(null);
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return symbolTable.getMethodByName(s)
                .map(JmmSymbolTable.Method::getLocalVariables)
                .map(listOfSymbols -> listOfSymbols.stream()
                        .map(JmmSymbolUtils::toSymbol)
                        .toList())
                .orElse(null);
    }

    public JmmSymbolTable getJmmSymbolTable() {
        return symbolTable;
    }
}
