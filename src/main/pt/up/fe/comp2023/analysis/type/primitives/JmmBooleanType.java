package pt.up.fe.comp2023.analysis.type.primitives;

public class JmmBooleanType extends JmmType {

    public static final String NAME = "boolean";
    private static final JmmBooleanType INSTANCE = new JmmBooleanType();

    public static JmmBooleanType getInstance() {
        return INSTANCE;
    }

    private JmmBooleanType() {
        super(NAME);
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        return other instanceof JmmBooleanType;
    }

    @Override
    public String toOllirTypeSuffix() {
        return "bool";
    }
}
