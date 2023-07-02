package pt.up.fe.comp2023.ollir.optimization.registers;

import java.util.*;
import java.util.stream.Collectors;

public class InterferenceGraph<T> {

    private static class Node<U> {

        private final Set<Node<U>> neighbors = new HashSet<>();
        private final U data;

        private Node(U data) {
            this.data = data;
        }

        private void connectTo(Node<U> node) {
            neighbors.add(node);
        }
    }

    private final Map<T, Node<T>> nodes = new HashMap<>();

    public void put(T value) {
        if (nodes.containsKey(value))
            return;

        var node = new Node<>(value);
        nodes.put(value, node);
    }

    public void putAll(Collection<T> collection) {
        collection.forEach(this::put);
    }

    public void connect(T first, T second) {
        var firstNode = nodes.get(first);
        var secondNode = nodes.get(second);

        if (firstNode == null || secondNode == null)
            return;

        firstNode.connectTo(secondNode);
        secondNode.connectTo(firstNode);
    }

    public void connectAll(Collection<T> collection) {
        var nodesToConnect = new ArrayList<>(collection);

        for (int i = 0; i < nodesToConnect.size() - 1; i++) {
            var first = nodesToConnect.get(i);

            for (int j = i; j < nodesToConnect.size(); j++) {
                var second = nodesToConnect.get(j);
                connect(first, second);
            }
        }
    }

    public Set<T> getNeighbors(T value) {
        var node = nodes.get(value);
        if (node == null)
            return null;

        return node.neighbors.stream()
                .map(neighbor -> neighbor.data)
                .collect(Collectors.toSet());
    }

    public Optional<Integer> numOfEdgesOf(Node<T> node) {
        if(!nodes.values().contains(node))
            return Optional.empty();

        return Optional.of(node.neighbors.size());
    }

    public Set<Node<T>> getNeighborsOf(Node<T> node) {
        return node.neighbors;
    }

    public boolean removeNeighbor(Node<T> node, Node<T> nodeToRemove) {
        var neighbors = node.neighbors;

        return neighbors.remove(nodeToRemove);
    }

    public void iterate() {

    }
}
