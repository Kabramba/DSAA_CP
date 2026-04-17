package com.simulator.model;

import java.util.*;

public class Path {
    private final List<GraphCell> nodes;
    private final Set<GraphCell> nodeSet;

    public Path(GraphCell startCell) {
        if (startCell == null) throw new IllegalArgumentException("Start cell cannot be null");
        this.nodes = new ArrayList<>();
        this.nodeSet = new HashSet<>();
        nodes.add(startCell);
        nodeSet.add(startCell);
    }

    public void append(GraphCell cell) {
        if (cell == null) throw new IllegalArgumentException("Cannot append null cell");

        if (nodeSet.contains(cell)) {
            throw new IllegalStateException(
                    "Cycle violation: cell (" + cell.getX() + "," + cell.getY() + ") already exists in path");
        }

        GraphCell tail = nodes.get(nodes.size() - 1);
        if (!isTraversableNeighbor(tail, cell)) {
            throw new IllegalStateException(
                    "Wall violation: no open passage from (" + tail.getX() + "," + tail.getY()
                            + ") to (" + cell.getX() + "," + cell.getY() + ")");
        }

        nodes.add(cell);
        nodeSet.add(cell);
    }

    private boolean isTraversableNeighbor(GraphCell from, GraphCell to) {
        if (from.northNode == to && !from.hasNorthWall()) return true;
        if (from.southNode == to && !from.hasSouthWall()) return true;
        if (from.eastNode == to && !from.hasEastWall()) return true;
        if (from.westNode == to && !from.hasWestWall()) return true;
        return false;
    }

    public static Path buildFromParentChain(GraphCell target, Map<GraphCell, GraphCell> parentMap) {
        LinkedList<GraphCell> reversed = new LinkedList<>();
        GraphCell current = target;
        while (current != null) {
            reversed.addFirst(current);
            current = parentMap.get(current);
        }

        Iterator<GraphCell> it = reversed.iterator();
        Path path = new Path(it.next());
        while (it.hasNext()) {
            path.append(it.next());
        }
        return path;
    }

    public List<GraphCell> getNodes() { return Collections.unmodifiableList(nodes); }
    public GraphCell getHead() { return nodes.get(0); }
    public GraphCell getTail() { return nodes.get(nodes.size() - 1); }
    public int length() { return nodes.size(); }
    public boolean contains(GraphCell cell) { return nodeSet.contains(cell); }
}