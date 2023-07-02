package pt.up.fe.comp2023.ollir.optimization.registers;

import pt.up.fe.comp2023.utils.Graph;
import pt.up.fe.comp2023.utils.Pair;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class GraphColorizer {

    public Pair<Integer, List<ColorData>> analyze(Graph<String> interferenceGraph) {
        var colorGraph = interferenceGraph.map(ColorData::new);
        var stack = new LinkedList<String>();

        int maxColors = 0;

        while (!interferenceGraph.isEmpty()) {
            int finalMaxColors = maxColors; // Effectively final maxColors
            Graph<String>.Node selectedNode = interferenceGraph.getNodeByPredicate(node -> node.getNumOfNeighbors() < finalMaxColors);

            if (selectedNode == null) {
                maxColors = interferenceGraph.getNodes()
                        .stream()
                        .map(Graph.Node::getNumOfNeighbors)
                        .min(Comparator.naturalOrder())
                        .orElseThrow() + 1;

                continue;
            }

            stack.push(selectedNode.getData());
            selectedNode.remove();
        }

        while (!stack.isEmpty()) {
            var varName = stack.pop();

            var colorNode = colorGraph.getNodeByPredicate(node -> node.getData().getName().equals(varName));

            var usedColors = new TreeSet<Integer>();
            for (var neighbor : colorNode.getNeighbors()) {
                var data = neighbor.getData();
                data.getRegister()
                        .ifPresent(usedColors::add);
            }

            int selectedColor = 0;
            while (usedColors.contains(selectedColor)) {
                selectedColor++;
            }

            maxColors = Math.max(maxColors, selectedColor + 1);

            var data = colorNode.getData();
            data.setRegister(selectedColor);
        }

        return Pair.of(
                maxColors,
                colorGraph.getNodes()
                    .stream()
                    .map(Graph.Node::getData)
                    .toList()
        );
    }
}
