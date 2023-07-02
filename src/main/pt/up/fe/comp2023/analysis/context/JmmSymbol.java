package pt.up.fe.comp2023.analysis.context;

import pt.up.fe.comp2023.analysis.type.primitives.JmmType;

import java.util.Objects;

public record JmmSymbol(JmmType type, String name) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JmmSymbol jmmSymbol)) return false;
        return Objects.equals(type, jmmSymbol.type) && Objects.equals(name, jmmSymbol.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "JmmSymbol{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
