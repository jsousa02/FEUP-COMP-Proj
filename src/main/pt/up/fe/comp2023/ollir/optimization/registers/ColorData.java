package pt.up.fe.comp2023.ollir.optimization.registers;

import java.util.Objects;
import java.util.OptionalInt;

public class ColorData {

    private final String name;
    private int register = -1;

    public ColorData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasRegister() {
        return register != -1;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public OptionalInt getRegister() {
        return register != -1
                ? OptionalInt.of(register)
                : OptionalInt.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorData colorData)) return false;
        return register == colorData.register && Objects.equals(name, colorData.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, register);
    }
}
