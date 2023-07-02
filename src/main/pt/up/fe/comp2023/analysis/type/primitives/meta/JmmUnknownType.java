package pt.up.fe.comp2023.analysis.type.primitives.meta;

import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

public class JmmUnknownType extends JmmType {

    public static final String NAME = "<unknown-type>";
    private static final JmmUnknownType INSTANCE = new JmmUnknownType();

    public static JmmUnknownType getInstance() {
        return INSTANCE;
    }

    private JmmUnknownType() {
        super(NAME);
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        return false;
    }

    @Override
    public String toOllirTypeSuffix() {
        return null;
    }
}
