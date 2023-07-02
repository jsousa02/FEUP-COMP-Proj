package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.*;

public class InstructionGenerator {

    public static String getClassPath(String fqn) {
        return fqn.replaceAll("\\.", "/");
    }

    public static String getClassPath(ClassUnit classUnit, Type type) {
        if (type instanceof ClassType classType) {
            if (classUnit.isImportedClass(classType.getName())) {
                var path = classUnit.getImports().stream()
                        .filter(fqn -> Ollir.getSimpleClassName(fqn).equals(classType.getName()))
                        .findAny()
                        .get();

                return getClassPath(path);
            }

            return classType.getName();
        }

        throw new RuntimeException("Could not get class path for type %s".formatted(type));
    }

    public static String getDescriptor(ClassUnit classUnit, Type type) {
        return switch (type.getTypeOfElement()) {
            case STRING -> "Ljava/lang/String;";
            case ARRAYREF -> {
                if (!(type instanceof ArrayType arrayType)) {
                    throw new IllegalArgumentException("Argument of type ARRAYREF is not an ArrayType");
                }

                yield "[%s".formatted(getDescriptor(classUnit, arrayType.getElementType()));
            }
            case BOOLEAN -> "Z";
            case THIS, CLASS, OBJECTREF -> {
                if (!(type instanceof ClassType classType)) {
                    throw new IllegalArgumentException("Argument of type OBJECTREF is not a ClassType");
                }

                yield "L%s;".formatted(getClassPath(classUnit, classType));
            }
            case INT32 -> "I";
            case VOID -> "V";
        };
    }

    public static String getInstructionVariant(Type type) {
        return switch (type.getTypeOfElement()) {
            case THIS, CLASS, STRING, ARRAYREF, OBJECTREF -> "a";
            case INT32, BOOLEAN -> "i";
            case VOID -> "";
            default -> throw new RuntimeException("Invalid Type");
        };
    }

    public String limitStack(int num) {
        return "\t.limit stack %d\n".formatted(num);
    }

    public String limitLocals(int num) {
        return "\t.limit locals %d\n".formatted(num);
    }

    public String className(String className) {
        return ".class public %s\n".formatted(className);
    }

    public String superClass(String superClass) {
        return ".super %s\n".formatted(getClassPath(superClass));
    }

    public String constructor(String superClass) {
        return """
            .method public <init>()V
            \taload_0
            \tinvokespecial %s/<init>()V
            \treturn
            .end method\n
            """.stripIndent().formatted(getClassPath(superClass));
    }

    public String field(List<AccessSpec> accessSpecs, String fieldName, String descriptor, Integer initialValue) {
        StringBuilder accessSpecsString = new StringBuilder();
        for (AccessSpec accessSpec : accessSpecs) {
            accessSpecsString
                    .append(accessSpec.name().toLowerCase())
                    .append(' ');
        }

        String initializer = initialValue != null ?
                " = %d".formatted(initialValue) : "";

        return ".field %s%s %s%s\n".formatted(accessSpecsString.toString(), fieldName, descriptor, initializer);
    }

    public String startMethod(List<AccessSpec> accessType, String methodName, List<String> argDescriptors, String returnTypeDescriptor) {
        StringBuilder header = new StringBuilder();

        header.append(".method ");

        for (AccessSpec accessSpec : accessType) {
            header.append(accessSpec.name().toLowerCase())
                    .append(' ');
        }

        header.append(methodName).append('(');


        for (String arg : argDescriptors) {
            header.append(arg);
        }

        header.append(')')
                .append(returnTypeDescriptor)
                .append('\n');

        return header.toString();
    }
    
    public String endMethod() {
        return ".end method";
    }

    public String invoke(String callType, String path, String name, List<String> argDescriptors, String returnTypeDescriptor) {
        StringBuilder args = new StringBuilder();
        for (String argDescriptor : argDescriptors) {
            args.append(argDescriptor);
        }
        return '\t' + callType + ' ' + path + '/' + name + '(' + args + ')' + returnTypeDescriptor + '\n';
    }

    public String _new(String returnTypeDescriptor) {
        return "\tnew " + returnTypeDescriptor + '\n';
    }

    public String ldc(int literal) {
        return '\t' + "ldc " + literal + '\n';
    }

    public String newArray(String type) {
        return "\tnewarray " + type + '\n';
    }

    public String arrayLength() {
        return "\tarraylength \n";
    }

    public String sipush(int value) {
        return "\tsipush " + value + "\n";
    }

    public String bipush(int value) {
        return "\tbipush " + value + "\n";
    }

    public String iconst(int value) {
        var absValue = Math.abs(value);
        return "\ticonst_%s%d\n".formatted(value < 0 ? "m" : "", absValue);
    }

    public String iinc(int value, int register) {
        return "\tiinc " + register + " " + value + "\n";
    }

    public String load(String jasminType, int register) {
        if (register <= 3) {
            return "\t" + jasminType + "load_" + register + "\n";
        }
        return '\t' + jasminType + "load " + register + '\n';
    }

    public String store(String jasminType, int register) {
        if (register <= 3) {
            return "\t" + jasminType + "store_" + register + "\n";
        }
        return '\t' + jasminType + "store " + register + '\n';
    }

    public String operation(String jasminType, String operationType) {
        return '\t' + jasminType + operationType + '\n';
    }

    public String putfield(String path, String name, String fieldType) {
        return '\t' + "putfield " + path + '/' + name + ' ' + fieldType + '\n';
    }

    public String getfield(String path, String name, String fieldType) {
        return '\t' + "getfield " + path + '/' + name + ' ' + fieldType + '\n';
    }

    public String label(String label) {
        return '\n' + label + ":\n";
    }

    public String unconditionalGoto(String label) {
        return "\tgoto " + label + "\n";
    }

    public String conditionalGoto(String condition, String label) {
        return '\t' + condition + ' ' + label + '\n';
    }

    public String pop() {
        return "\tpop\n";
    }

    public String _return(String jasminType) {
        return '\t' + jasminType + "return\n";
    }

    public String ifne(String label) {
        return "\tifne " + label + '\n';
    }

}
