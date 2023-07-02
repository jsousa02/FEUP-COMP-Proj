package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp2023.analysis.type.primitives.JmmIntType;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;
import pt.up.fe.comp2023.analysis.type.primitives.JmmVoidType;

import java.util.List;

public class OllirGenerator {

    public enum InvocationType {
        STATIC("invokestatic"), VIRTUAL("invokevirtual"), SPECIAL("invokespecial");

        private final String instruction;

        InvocationType(String instruction) {
            this.instruction = instruction;
        }

        public String getInstruction() {
            return instruction;
        }
    }

    public String withType(String s, JmmType type) {
        return "%s.%s".formatted(s, type.toOllirTypeSuffix());
    }

    public String arrayIndex(String array, String index, JmmType type) {
        return withType(array + '[' + index + ']', type);
    }

    public String unaryOp(String op, JmmType type, String operand) {
        return withType(op, type) + " " + operand;
    }

    public String binaryOp(String op, JmmType type, String lhs, String rhs) {
        return lhs + " " + withType(op, type) + " " + rhs;
    }

    public String invoke(InvocationType invocationType, String callee, String methodName, List<String> args, JmmType returnType) {
        var invoke = new StringBuilder();

        invoke.append(invocationType.getInstruction())
                .append('(').append(callee)
                .append(", ").append('"').append(methodName).append('"');

        for (String arg : args) {
            invoke.append(", ").append(arg);
        }

        invoke.append(")");

        return withType(invoke.toString(), returnType);
    }

    public String arrayLength(String array) {
        return withType("arraylength(" + array + ')', JmmIntType.getInstance());
    }

    public String newArray(String size, JmmType arrayType) {
        return withType("new(array, " + size + ')', arrayType);
    }

    public String newObject(JmmType type) {
        return withType("new(" + type.toOllirTypeSuffix() + ')', type);
    }

    public String assignment(String lhs, JmmType lhsType, String rhs) {
        return binaryOp(":=", lhsType, lhs, rhs) + ";\n";
    }

    public String condGoto(String cond, String label) {
        return "if (%s) goto %s;\n".formatted(cond, label);
    }

    public String uncondGoto(String label) {
        return "goto %s;\n".formatted(label);
    }

    public String label(String label) {
        return label + ":\n";
    }

    public String getField(String obj, String fieldName, JmmType fieldType) {
        return withType("getfield(%s, %s)".formatted(obj, withType(fieldName, fieldType)), fieldType);
    }

    public String putField(String obj, String field, String value) {
        return "putfield(%s, %s, %s).V;\n".formatted(obj, field, value);
    }
}
