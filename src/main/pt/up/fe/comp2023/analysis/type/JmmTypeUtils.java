package pt.up.fe.comp2023.analysis.type;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2023.analysis.type.primitives.*;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmUnknownType;

import java.util.Optional;
import java.util.function.Function;

public class JmmTypeUtils {

    private static Optional<JmmType> getBaseType(Function<String, JmmClassType> classResolver, Type type) {
        return Optional.ofNullable(switch (type.getName()) {
            case JmmIntType.NAME -> JmmIntType.getInstance();
            case JmmBooleanType.NAME -> JmmBooleanType.getInstance();
            case JmmVoidType.NAME -> JmmVoidType.getInstance();
            case JmmUnknownType.NAME -> JmmUnknownType.getInstance();
            default -> classResolver.apply(type.getName());
        });
    }

    public static Optional<JmmType> fromType(Function<String, JmmClassType> classResolver, Type type) {
        return JmmTypeUtils.getBaseType(classResolver, type)
                .map(baseType -> type.isArray() ? new JmmArrayType<>(baseType) : baseType);
    }

    public static Type toType(JmmType type) {
        JmmType baseType = type instanceof JmmArrayType<?> arrayType ? arrayType.getElementType() : type;
        String name = baseType instanceof JmmClassType jmmClassType ? jmmClassType.getSimpleName() : baseType.getName();

        return new Type(name, type instanceof JmmArrayType<?>);
    }
}
