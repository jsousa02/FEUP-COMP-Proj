package pt.up.fe.comp2023.analysis.type.primitives.meta;

import pt.up.fe.comp2023.analysis.type.primitives.JmmClassType;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

public class JmmStaticReferenceType extends JmmType {

    private final JmmClassType classType;

    public JmmStaticReferenceType(JmmClassType classType) {
        super("<static-reference %s>".formatted(classType.getName()));
        this.classType = classType;
    }

    public JmmClassType getClassType() {
        return classType;
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        return false;
    }

    @Override
    public String toOllirTypeSuffix() {
        return classType.getSimpleName();
    }
}
