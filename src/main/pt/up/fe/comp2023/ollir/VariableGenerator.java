package pt.up.fe.comp2023.ollir;

public class VariableGenerator {

    private int id = 1;

    public String getFreeVariable() {
        return "t" + id++;
    }
}
