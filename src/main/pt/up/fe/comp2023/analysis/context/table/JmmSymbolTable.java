package pt.up.fe.comp2023.analysis.context.table;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp2023.analysis.context.JmmSymbol;
import pt.up.fe.comp2023.analysis.exception.AnalysisException;
import pt.up.fe.comp2023.analysis.type.JmmTypeUtils;
import pt.up.fe.comp2023.analysis.type.primitives.JmmClassType;
import pt.up.fe.comp2023.analysis.type.primitives.JmmType;
import pt.up.fe.comp2023.analysis.type.primitives.meta.JmmInvalidType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JmmSymbolTable {

    private final Map<String, JmmClassType> imports = new TreeMap<>();
    private JmmClassType thisClass;
    private final List<JmmSymbol> fields = new ArrayList<>(); // type-name
    private final List<Method> methods = new ArrayList<>();

    public JmmSymbolTable(String className) {
        thisClass = new JmmClassType(className, JmmClassType.OBJECT);
    }

    public Map<String, JmmClassType> getImports() {
        return imports;
    }

    public Map<String, JmmClassType> getClassesInScope() {
        Map<String, JmmClassType> classesInScope = new TreeMap<>(imports);
        classesInScope.put(JmmClassType.OBJECT.getSimpleName(), JmmClassType.OBJECT);
        classesInScope.put(JmmClassType.STRING.getSimpleName(), JmmClassType.STRING);
        classesInScope.put(thisClass.getSimpleName(), thisClass);

        return classesInScope;
    }

    public JmmClassType getThisClass() {
        return thisClass;
    }

    public List<JmmSymbol> getFields() {
        return fields;
    }

    public Optional<JmmSymbol> getFieldByName(String name) {
        return fields.stream()
                .filter(symbol -> symbol.name().equals(name))
                .findFirst();
    }

    public List<Method> getMethods() {
        return methods;
    }

    public Optional<Method> getMethodByName(String name) {
        return methods.stream()
                .filter(method -> method.getName().equals(name))
                .findFirst();
    }

    public void addImport(Consumer<AnalysisException> exceptionHandler, List<String> path) {
        String importFqn = String.join(".", path);
        JmmClassType importedType = new JmmClassType(importFqn, null);

        JmmClassType existingClassInScope = imports.get(importedType.getSimpleName());
        if (existingClassInScope != null && existingClassInScope.getSimpleName().equals(importedType.getSimpleName())) {
            if (existingClassInScope == importedType) {
                exceptionHandler.accept(AnalysisException.duplicatedImport(importFqn));
                return;
            }

            exceptionHandler.accept(AnalysisException.conflictingImport(importedType.getSimpleName()));
            return;
        }

        imports.put(importedType.getSimpleName(), importedType);
    }

    public void setSuperClass(Consumer<AnalysisException> exceptionHandler, String superClassName) {
        if (superClassName == null) {
            thisClass = new JmmClassType(thisClass.getName(),JmmClassType.OBJECT);
            return;
        }

        Map<String, JmmClassType> classesInScope = getClassesInScope();
        JmmClassType superClass = classesInScope.get(superClassName);

        if (superClass == null) {
            exceptionHandler.accept(AnalysisException.symbolNotFound(superClassName));
            return;
        }

        if (superClass == thisClass) {
            exceptionHandler.accept(AnalysisException.cyclicExtension());
            return;
        }

        thisClass = new JmmClassType(thisClass.getName(), superClass);
    }

    public void addField(Consumer<AnalysisException> exceptionHandler, Type type, String name) {
        Map<String, JmmClassType> classesInScope = getClassesInScope();
        JmmType jmmType = JmmTypeUtils.fromType(classesInScope::get, type)
                .orElseGet(submitErrorAndReturnInvalidType(exceptionHandler, type));

        Optional<JmmSymbol> existingField = getFieldByName(name);
        if (existingField.isPresent()) {
            exceptionHandler.accept(AnalysisException.variableAlreadyDefinedInScope(name));
        }

        fields.add(new JmmSymbol(jmmType, name));
    }

    public class Method {
        private final boolean isStatic;
        private final String name;
        private final JmmType returnType;
        private final List<JmmSymbol> parameters = new ArrayList<>();
        private final List<JmmSymbol> localVariables = new ArrayList<>();

        private Method(String methodName, JmmType returnType, boolean isStatic) {
            this.name = methodName;
            this.returnType = returnType;
            this.isStatic = isStatic;
        }

        public JmmSymbolTable getParentTable() {
            return JmmSymbolTable.this;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public String getName() {
            return name;
        }

        public JmmType getReturnType() {
            return returnType;
        }

        public List<JmmSymbol> getParameters() {
            return parameters;
        }

        public Optional<JmmSymbol> getParameterByName(String name) {
            return parameters.stream()
                    .filter(symbol -> symbol.name().equals(name))
                    .findFirst();
        }

        public OptionalInt getParameterIndexByName(String name) {
            for (int i = 0; i < parameters.size(); i++) {
                JmmSymbol symbol = parameters.get(i);
                if (symbol.name().equals(name)) {
                    return OptionalInt.of(i);
                }
            }

            return OptionalInt.empty();
        }

        public List<JmmSymbol> getLocalVariables() {
            return localVariables;
        }

        public Optional<JmmSymbol> getLocalVariableByName(String name) {
            return localVariables.stream()
                    .filter(symbol -> symbol.name().equals(name))
                    .findFirst();
        }

        public void addParameter(Consumer<AnalysisException> exceptionHandler, Type type, String name) {
            Map<String, JmmClassType> classesInScope = getClassesInScope();
            JmmType jmmType = JmmTypeUtils.fromType(classesInScope::get, type)
                    .orElseGet(submitErrorAndReturnInvalidType(exceptionHandler, type));

            Optional<JmmSymbol> existingParameter = getParameterByName(name);
            if (existingParameter.isPresent()) {
                exceptionHandler.accept(AnalysisException.variableAlreadyDefinedInScope(name));
            }

            parameters.add(new JmmSymbol(jmmType, name));

        }

        public void addLocalVariable(Consumer<AnalysisException> exceptionHandler, Type type, String name) {
            Map<String, JmmClassType> classesInScope = getClassesInScope();
            JmmType jmmType = JmmTypeUtils.fromType(classesInScope::get, type)
                    .orElseGet(submitErrorAndReturnInvalidType(exceptionHandler, type));

            Optional<JmmSymbol> existingVariable = getLocalVariableByName(name)
                    .or(() -> getParameterByName(name));

            if (existingVariable.isPresent()) {
                exceptionHandler.accept(AnalysisException.variableAlreadyDefinedInScope(name));
            }

            localVariables.add(new JmmSymbol(jmmType, name));
        }
    }

    public Method createMethod(Consumer<AnalysisException> exceptionHandler, String methodName, Type returnType, boolean isStatic) {
        Map<String, JmmClassType> classesInScope = getClassesInScope();
        JmmType jmmReturnType = JmmTypeUtils.fromType(classesInScope::get, returnType)
                .orElseGet(submitErrorAndReturnInvalidType(exceptionHandler, returnType));

        Optional<Method> existingMethod = getMethodByName(methodName);
        if (existingMethod.isPresent()) {
            exceptionHandler.accept(AnalysisException.methodAlreadyDefined(methodName));
        }

        Method method = new Method(methodName, jmmReturnType, isStatic);
        methods.add(method);

        return method;
    }

    private static Supplier<JmmInvalidType> submitErrorAndReturnInvalidType(Consumer<AnalysisException> exceptionHandler, Type type) {
        return () -> {
            exceptionHandler.accept(AnalysisException.symbolNotFound(type.getName()));
            return JmmInvalidType.getInstance();
        };
    }
}
