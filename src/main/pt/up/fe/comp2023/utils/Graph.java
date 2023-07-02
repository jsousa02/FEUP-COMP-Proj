package pt.up.fe.comp2023.utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Graph<Data> {

    public class Node {

        private final Set<Node> neighbors = new HashSet<>();

        private final Data data;

        public Node(Data data) {
            this.data = data;
        }

        public Data getData() {
            return data;
        }

        public Set<Node> getNeighbors() {
            return neighbors;
        }

        public int getNumOfNeighbors() {
            return neighbors.size();
        }

        public void connectTo(Data data) {
            connectTo(nodes.get(data));
        }

        public void connectTo(Node node) {
            if (node == null)
                return;

            node.neighbors.add(this);
            this.neighbors.add(node);
        }

        public void disconnectFrom(Data id) {
            disconnectFrom(nodes.get(id));
        }

        public void disconnectFrom(Node node) {
            if (node == null)
                return;

            node.neighbors.remove(this);
            this.neighbors.remove(node);
        }

        public void remove() {
            var neighborsCopy = new ArrayList<>(neighbors);
            for (var node : neighborsCopy)
                disconnectFrom(node);

            nodes.remove(data);
        }
    }

    private final Map<Data, Node> nodes = new HashMap<>();

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public Node getNodeByData(Data data) {
        return nodes.get(data);
    }

    public Node getNodeByPredicate(Predicate<Node> predicate) {
        return nodes.values()
                .stream()
                .filter(predicate)
                .findAny()
                .orElse(null);
    }

    public Node addNode(Data data) {
        if (nodes.containsKey(data))
            return nodes.get(data);

        var node = new Node(data);
        nodes.put(node.getData(), node);

        return node;
    }

    public void addAll(Collection<Data> nodeData) {
        nodeData.forEach(this::addNode);
    }

    public void connectAll(Collection<Data> nodeData) {
        var nodesToConnect = nodeData.stream()
                .map(nodes::get)
                .filter(Objects::nonNull)
                .toList();

        for (int i = 0; i < nodesToConnect.size() - 1; i++) {
            var first = nodesToConnect.get(i);

            for (int j = i + 1; j < nodesToConnect.size(); j++) {
                var second = nodesToConnect.get(j);
                first.connectTo(second);
            }
        }
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public <NewData> Graph<NewData> map(Function<Data, NewData> mapper) {
        var graph = new Graph<NewData>();

        nodes.keySet()
                .stream()
                .map(mapper)
                .forEach(graph::addNode);

        nodes.forEach((data, node) -> {
            var newId = mapper.apply(node.data);
            var newNode = graph.getNodeByData(newId);

            for (var neighbor : node.neighbors) {
                var newNeighborId = mapper.apply(neighbor.data);
                var newNeighbor = graph.getNodeByData(newNeighborId);

                newNode.connectTo(newNeighbor);
            }
        });

        return graph;
    }
}
