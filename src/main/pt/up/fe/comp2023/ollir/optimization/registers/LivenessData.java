package pt.up.fe.comp2023.ollir.optimization.registers;

import org.specs.comp.ollir.Instruction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LivenessData {

    private final Map<Integer, Set<String>> liveIns;
    private final Map<Integer, Set<String>> liveOuts;
    private final Map<Integer, Set<String>> uses;
    private final Map<Integer, Set<String>> defs;

    public LivenessData(Map<Integer, Set<String>> liveIns, Map<Integer, Set<String>> liveOuts, Map<Integer, Set<String>> uses, Map<Integer, Set<String>> defs) {
        this.liveIns = new TreeMap<>(liveIns);
        this.liveOuts = new TreeMap<>(liveOuts);
        this.uses = new TreeMap<>(uses);
        this.defs = new TreeMap<>(defs);
    }

    public Map<Integer, Set<String>> getLiveIns() {
        return liveIns;
    }

    public Map<Integer, Set<String>> getLiveOuts() {
        return liveOuts;
    }

    public Map<Integer, Set<String>> getUses() {
        return uses;
    }

    public Map<Integer, Set<String>> getDefs() {
        return defs;
    }
}
