package com.simulator.algorithm;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Path;
import com.simulator.model.PathCursor;

import java.util.*;

public class DijkstraPathfinder implements PathfindingAlgorithm {
    private final PriorityQueue<GraphCell> frontier;
    private final Map<GraphCell, Double> costSoFar = new HashMap<>();
    private final Map<GraphCell, GraphCell> parentMap = new HashMap<>();
    private boolean initialized = false;
    private Path resultPath = null;

    public DijkstraPathfinder() {
        this.frontier = new PriorityQueue<>(Comparator.comparingDouble(
                cell -> costSoFar.getOrDefault(cell, Double.MAX_VALUE)));
    }

    @Override
    public boolean executeNextStep(PathCursor cursor, GraphMaze interpretMap) {
        if (!initialized) {
            GraphCell start = cursor.getCurrentCell();
            costSoFar.put(start, 0.0);
            parentMap.put(start, null);
            frontier.add(start);
            initialized = true;
        }

        if (frontier.isEmpty()) return true;

        GraphCell current = frontier.poll();
        cursor.moveToVirtualNode(current);

        if (current == cursor.getTargetCell()) {
            resultPath = Path.buildFromParentChain(current, parentMap);
            return true;
        }

        double currentCost = costSoFar.get(current);

        for (GraphCell neighbor : getAccessibleNeighbors(current)) {
            double newCost = currentCost + 1.0;

            if (newCost < costSoFar.getOrDefault(neighbor, Double.MAX_VALUE)) {
                costSoFar.put(neighbor, newCost);
                parentMap.put(neighbor, current);
                frontier.remove(neighbor);
                frontier.add(neighbor);
            }
        }

        return false;
    }

    @Override
    public Path getResultPath() {
        return resultPath;
    }

    private List<GraphCell> getAccessibleNeighbors(GraphCell node) {
        List<GraphCell> open = new ArrayList<>();
        if (!node.hasNorthWall() && node.northNode != null) open.add(node.northNode);
        if (!node.hasSouthWall() && node.southNode != null) open.add(node.southNode);
        if (!node.hasEastWall() && node.eastNode != null) open.add(node.eastNode);
        if (!node.hasWestWall() && node.westNode != null) open.add(node.westNode);
        return open;
    }
}