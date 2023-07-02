package pt.up.fe.comp2023.ollir.optimization.constants;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.ollir.optimization.constants.value.TypedValue;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class OptimizationContext {

    protected final Map<String, TypedValue<?>> optimizations = new TreeMap<>();

    public void assign(String varName, TypedValue<?> value) {
        optimizations.put(varName, value);
    }

    public void drop(String varName) {
        optimizations.remove(varName);
    }

    public Optional<TypedValue<?>> getOptimizedValue(String varName) {
        return Optional.ofNullable(optimizations.get(varName));
    }

    public OptimizationContext copy() {
        var newContext = new OptimizationContext();
        newContext.optimizations.putAll(optimizations);

        return newContext;
    }

    public void show() {
        optimizations.forEach((key, value) -> System.out.println(key + " " + value.toString()));
    }

    public OptimizationContext intersect(OptimizationContext other) {
        var intersectedContext = new OptimizationContext();

        var intersectedOptimizations = intersectedContext.optimizations;
        optimizations.entrySet()
                .stream()
                .filter(entry -> {
                    var valueInOther = other.optimizations.get(entry.getKey());
                    return entry.getValue().equals(valueInOther);
                })
                .forEach(entry -> intersectedOptimizations.put(entry.getKey(), entry.getValue()));

        return intersectedContext;
    }
}
