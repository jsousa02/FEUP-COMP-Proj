package pt.up.fe.comp2023.ollir.optimization.constants.value;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2023.analysis.type.primitives.JmmIntType;

import javax.swing.text.html.Option;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class IntegerValue extends TypedValue<Integer> {

    private final int value;

    public IntegerValue(int value) {
        super(JmmIntType.getInstance(), "IntegerLiteral");
        this.value = value;
    }

    public static Optional<IntegerValue> fromJmmNode(JmmNode node) {
        return Optional.of(node)
                .filter(val -> val.getKind().equals("IntegerLiteral"))
                .map(val -> Integer.parseInt(node.get("value")))
                .map(IntegerValue::new);
    }

    @Override
    public Integer value() {
        return value;
    }

    @Override
    public TypedValue<Integer> map(UnaryOperator<Integer> mapper) {
        return new IntegerValue(mapper.apply(value));
    }

    @Override
    public JmmNode toJmmNode() {
        var node = super.toJmmNode();
        node.put("value", Integer.toString(value));

        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntegerValue that)) return false;
        if (!super.equals(o)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "IntegerValue{" +
                "value=" + value +
                '}';
    }
}
