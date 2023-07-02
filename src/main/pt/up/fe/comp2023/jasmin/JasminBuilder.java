package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.*;

public class JasminBuilder {
    private final StringBuilder code = new StringBuilder();
    private final InstructionGenerator generator = new InstructionGenerator();

    private final ClassUnit classUnit;

    public JasminBuilder(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }

    public JasminBuilder className(String className) {
        code.append(generator.className(className));
        return this;
    }

    public JasminBuilder superClass(String superClass) {
        code.append(generator.superClass(superClass));
        return this;
    }

    public JasminBuilder newLine() {
        code.append('\n');
        return this;
    }

    public JasminBuilder constructor(String superClass) {
        code.append(generator.constructor(superClass));
        return this;
    }

    public JasminBuilder field(Field field) {
        List<AccessSpec> accessSpecs = new ArrayList<>();

        switch (field.getFieldAccessModifier()) {
            case PUBLIC -> accessSpecs.add(AccessSpec.PUBLIC);
            case PRIVATE -> accessSpecs.add(AccessSpec.PRIVATE);
            case PROTECTED -> accessSpecs.add(AccessSpec.PROTECTED);
            case DEFAULT -> {}
        }

        if (field.isStaticField()) {
            accessSpecs.add(AccessSpec.STATIC);
        }

        if (field.isFinalField()) {
            accessSpecs.add(AccessSpec.FINAL);
        }

        Integer initialValue = field.isInitialized() ?
                field.getInitialValue() : null;

        String fieldCode = generator.field(accessSpecs,
                field.getFieldName(),
                InstructionGenerator.getDescriptor(classUnit, field.getFieldType()),
                initialValue);

        code.append(fieldCode);
        return this;
    }


    public JasminBuilder method(Method method) {
        JasminMethodBuilder methodBuilder = new JasminMethodBuilder(method);
        
        for (Instruction instruction : method.getInstructions()) {
            methodBuilder.instruction(instruction);

            if ((instruction instanceof CallInstruction) && (((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID)) {
                methodBuilder.pop();
            }
        }

        code.append(methodBuilder.build());
        return this;
    }

    public String build() {
        return code.toString();
    }
}
