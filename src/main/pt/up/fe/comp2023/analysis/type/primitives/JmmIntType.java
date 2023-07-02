package pt.up.fe.comp2023.analysis.type.primitives;

public class JmmIntType extends JmmType {

    public static final String NAME = "int";
    private static final JmmIntType INSTANCE = new JmmIntType();

    public static JmmIntType getInstance() {
        return INSTANCE;
    }

    private JmmIntType() {
        super(NAME);
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        return other instanceof JmmIntType;
    }

    @Override
    public String toOllirTypeSuffix() {
        return "i32";
    }
}
