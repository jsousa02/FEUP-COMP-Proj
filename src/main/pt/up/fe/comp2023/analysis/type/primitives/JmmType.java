package pt.up.fe.comp2023.analysis.type.primitives;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public abstract class JmmType {

    private final String name;

    protected JmmType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isAssignableTo(JmmType other);

    public abstract String toOllirTypeSuffix();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JmmType jmmType)) return false;
        return Objects.equals(name, jmmType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "JmmType{" +
                "name='" + name + '\'' +
                '}';
    }
}
