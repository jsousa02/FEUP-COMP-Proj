package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import static pt.up.fe.comp2023.jasmin.InstructionGenerator.getClassPath;

public class JasminBackend implements pt.up.fe.comp.jmm.jasmin.JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit classUnit = ollirResult.getOllirClass();
        String normalizedSuperClass = classUnit.getSuperClass() == null ?
                "java/lang/Object" : getClassPath(classUnit, new ClassType(ElementType.CLASS, classUnit.getSuperClass()));

        JasminBuilder jasminBuilder = new JasminBuilder(classUnit);

        jasminBuilder.className(classUnit.getClassName())
                    .superClass(normalizedSuperClass)
                    .newLine();

        for (Field field : classUnit.getFields()) {
            jasminBuilder.field(field);
        }

        for (int i = 0; i < classUnit.getNumMethods(); i++) {
            if (classUnit.getMethod(i).isConstructMethod()) {
                jasminBuilder.constructor(normalizedSuperClass);
            } else {
                jasminBuilder.method(classUnit.getMethod(i));
            }
        }

        return new JasminResult(jasminBuilder.build(), ollirResult.getConfig());
    }
}
