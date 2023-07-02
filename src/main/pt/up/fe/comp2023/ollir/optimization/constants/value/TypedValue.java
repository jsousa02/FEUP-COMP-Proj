package pt.up.fe.comp2023.ollir.optimization.constants.value;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp2023.utils.JmmNodeUtils;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class TypedValue<T> {

    private final JmmType type;
    private final String nodeKind;

    protected TypedValue(JmmType type, String nodeKind) {
        this.type = type;
        this.nodeKind = nodeKind;
    }

    public JmmNode toJmmNode() {
        var node = new JmmNodeImpl(nodeKind);
        JmmNodeUtils.setNodeType(node, type);

        return node;
    }

    public abstract T value();

    public abstract TypedValue<T> map(UnaryOperator<T> mapper);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedValue<?> that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
