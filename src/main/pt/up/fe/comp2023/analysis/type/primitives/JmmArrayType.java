package pt.up.fe.comp2023.analysis.type.primitives;

public class JmmArrayType<T extends JmmType> extends JmmType {

    private final T elementType;

    public JmmArrayType(T elementType) {
        super("%s[]".formatted(elementType.getName()));
        this.elementType = elementType;
    }

    public T getElementType() {
        return elementType;
    }

    @Override
    public boolean isAssignableTo(JmmType other) {
        if (!(other instanceof JmmArrayType<?> jmmArrayType))
            return false;

        return this.elementType.isAssignableTo(jmmArrayType.getElementType());
    }

    @Override
    public String toOllirTypeSuffix() {
        return "array.%s".formatted(elementType.toOllirTypeSuffix());
    }
}
