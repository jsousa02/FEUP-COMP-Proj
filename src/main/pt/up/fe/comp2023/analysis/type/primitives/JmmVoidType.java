package pt.up.fe.comp2023.analysis.type.primitives;

public class JmmVoidType extends JmmType {

    public static final String NAME = "void";
    private static final JmmVoidType INSTANCE = new JmmVoidType();

    public static JmmVoidType getInstance() {
        return INSTANCE;
    }

    private JmmVoidType() {
        super(NAME);
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        return false;
    }

    @Override
    public String toOllirTypeSuffix() {
        return "V";
    }
}
