package pt.up.fe.comp2023.analysis.context;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp2023.analysis.type.JmmTypeUtils;

public class JmmSymbolUtils {
    public static Symbol toSymbol(JmmSymbol jmmSymbol) {
        return new Symbol(JmmTypeUtils.toType(jmmSymbol.type()), jmmSymbol.name());
    }
}
