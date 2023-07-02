package pt.up.fe.comp2023.analysis.type.primitives.meta;

import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

public class JmmInvalidType extends JmmType {

    public static final String NAME = "<invalid-type>";
    private static final JmmInvalidType INSTANCE = new JmmInvalidType();

    public static JmmInvalidType getInstance() {
        return INSTANCE;
    }

    private JmmInvalidType() {
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
