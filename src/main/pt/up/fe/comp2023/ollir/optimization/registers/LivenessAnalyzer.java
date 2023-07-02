package pt.up.fe.comp2023.ollir.optimization.registers;

import org.specs.comp.ollir.AssignInstruction;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.tree.TreeNode;

import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class LivenessAnalyzer {

    public LivenessData analyze(Method method) {
        var liveIns = new TreeMap<Integer, Set<String>>();
        var liveOuts = new TreeMap<Integer, Set<String>>();
        var uses = new TreeMap<Integer, Set<String>>();
        var defs = new TreeMap<Integer, Set<String>>();

        for (var instr : method.getInstructions()) {
            var useSet = new TreeSet<String>();
            calculateUseSet(instr, useSet);
            uses.put(instr.getId(), useSet);

            var defSet = new TreeSet<String>();
            calculateDefSet(instr, defSet);
            defs.put(instr.getId(), defSet);
        }

        while (true) {
            boolean hasChanged = false;
            
            for (var instr : method.getInstructions()) {
                var instrId = instr.getId();

                var liveIn = liveIns.getOrDefault(instrId, Collections.emptySet());
                var liveOut = liveOuts.getOrDefault(instrId, Collections.emptySet());
                var use = uses.getOrDefault(instrId, Collections.emptySet());
                var def = defs.getOrDefault(instrId, Collections.emptySet());

                var newLiveIn = new TreeSet<>(use);
                liveOut.stream()
                        .filter(name -> !def.contains(name))
                        .forEach(newLiveIn::add);

                var newLiveOut = new TreeSet<String>();
                instr.getSuccessors().stream()
                        .map(node -> liveIns.getOrDefault(node.getId(), Collections.emptySet()))
                        .forEach(newLiveOut::addAll);

                liveIns.put(instr.getId(), newLiveIn);
                liveOuts.put(instr.getId(), newLiveOut);

                if (!liveIn.containsAll(newLiveIn) || !liveOut.containsAll(newLiveOut)) {
                    hasChanged = true;
                }
            }
            
            if (!hasChanged)
                break;
        }

        return new LivenessData(liveIns, liveOuts, uses, defs);
    }

    private void calculateUseSet(TreeNode node, Set<String> useSet) {
        if (node instanceof AssignInstruction assignInstruction) {
            calculateUseSet(assignInstruction.getRhs(), useSet);
            return;
        }

        if (node instanceof Operand operand) {
            if (!isOptimizable(operand))
                return;

            useSet.add(operand.getName());
            return;
        }

        node.getChildrenStream()
                .forEach(child -> calculateUseSet(child, useSet));
    }

    private void calculateDefSet(TreeNode node, Set<String> defSet) {
        if (node instanceof AssignInstruction assignInstruction) {
            var lhs = (Operand) assignInstruction.getDest();
            if (!isOptimizable(lhs))
                return;

            defSet.add(lhs.getName());
            return;
        }

        node.getChildrenStream()
                .forEach(child -> calculateDefSet(child, defSet));
    }

    private boolean isOptimizable(Operand operand) {
        var elementType = operand.getType().getTypeOfElement();
        if (elementType == ElementType.CLASS || elementType == ElementType.THIS || elementType == ElementType.VOID)
            return false;

        return !operand.isParameter();
    }
}
