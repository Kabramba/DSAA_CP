package com.simulator.algorithm;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.PathCursor;

import java.util.*;

public class BFSPathfinder implements PathfindingAlgorithm {
    private final Queue<GraphCell> queue = new LinkedList<>();
    private final Set<GraphCell> visited = new HashSet<>();
    private boolean initialized = false;

    @Override
    public boolean executeNextStep(PathCursor cursor, GraphMaze interpretMap) {
        // Initialization state
        if (!initialized) {
            queue.add(cursor.getCurrentCell());
            visited.add(cursor.getCurrentCell());
            initialized = true;
        }

        // Exhausted graph space without finding target
        if (queue.isEmpty()) return true;

        // Dequeue and visually update the cursor position
        GraphCell current = queue.poll();
        cursor.moveToVirtualNode(current);

        // Terminal condition check
        if (current == cursor.getTargetCell()) {
            return true;
        }

        // Enqueue accessible adjacent topological nodes
        for (GraphCell neighbor : getAccessibleNeighbors(current)) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        return false;
    }

    /**
     * Evaluates boolean wall state flags to determine traversable graph edges.
     */
    private List<GraphCell> getAccessibleNeighbors(GraphCell node) {
        List<GraphCell> open = new ArrayList<>();
        if (!node.hasNorthWall() && node.northNode != null) open.add(node.northNode);
        if (!node.hasSouthWall() && node.southNode != null) open.add(node.southNode);
        if (!node.hasEastWall() && node.eastNode != null) open.add(node.eastNode);
        if (!node.hasWestWall() && node.westNode != null) open.add(node.westNode);
        return open;
    }
}