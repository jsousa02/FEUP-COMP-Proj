package pt.up.fe.comp2023.ollir.optimization.registers;

import org.specs.comp.ollir.Method;
import pt.up.fe.comp2023.utils.Graph;

import java.util.TreeSet;

public class InterferenceAnalyzer {

    public Graph<String> analyze(Method method, LivenessData data) {
        var graph = new Graph<String>();

        var liveIns = data.getLiveIns();
        var liveOuts = data.getLiveOuts();
        var defs = data.getDefs();

        for (int id = 1; id <= method.getInstructions().size(); id++) {

            var instrLiveIns = liveIns.get(id);
            graph.addAll(instrLiveIns);
            graph.connectAll(instrLiveIns);

            var instrLiveOuts = liveOuts.get(id);
            var instrDefs = defs.get(id);

            var union = new TreeSet<String>();
            union.addAll(instrLiveOuts);
            union.addAll(instrDefs);

            graph.addAll(union);
            graph.connectAll(union);
        }

        return graph;
    }
}
