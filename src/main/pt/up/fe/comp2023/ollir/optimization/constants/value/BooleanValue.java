package pt.up.fe.comp2023.ollir.optimization.constants.value;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2023.analysis.type.primitives.JmmBooleanType;

import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class BooleanValue extends TypedValue<Boolean> {

    private final boolean value;

    public BooleanValue(boolean value) {
        super(JmmBooleanType.getInstance(), "BooleanLiteral");
        this.value = value;
    }

    public static Optional<BooleanValue> fromJmmNode(JmmNode node) {
        return Optional.of(node)
                .filter(val -> val.getKind().equals("BooleanLiteral"))
                .map(val -> node.get("value").equals("true"))
                .map(BooleanValue::new);
    }

    @Override
    public Boolean value() {
        return value;
    }

    @Override
    public TypedValue<Boolean> map(UnaryOperator<Boolean> mapper) {
        return new BooleanValue(mapper.apply(value));
    }

    @Override
    public JmmNode toJmmNode() {
        var node = super.toJmmNode();
        node.put("value", Boolean.toString(value));

        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BooleanValue that)) return false;
        if (!super.equals(o)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "BooleanValue{" +
                "value=" + value +
                '}';
    }
}
