package com.simulator.algorithm;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Path;
import com.simulator.model.PathCursor;

import java.util.*;

public class AStarPathfinder implements PathfindingAlgorithm {
    private PriorityQueue<GraphCell> frontier;
    private final Map<GraphCell, Double> gScore = new HashMap<>();
    private final Map<GraphCell, Double> fScore = new HashMap<>();
    private final Map<GraphCell, GraphCell> parentMap = new HashMap<>();
    private final Set<GraphCell> closedSet = new HashSet<>();
    private boolean initialized = false;
    private Path resultPath = null;
    private GraphCell target = null;

    @Override
    public boolean executeNextStep(PathCursor cursor, GraphMaze interpretMap) {
        if (!initialized) {
            target = cursor.getTargetCell();
            frontier = new PriorityQueue<>(Comparator.comparingDouble(
                    cell -> fScore.getOrDefault(cell, Double.MAX_VALUE)));

            GraphCell start = cursor.getCurrentCell();
            gScore.put(start, 0.0);
            fScore.put(start, heuristic(start, target));
            parentMap.put(start, null);
            frontier.add(start);
            initialized = true;
        }

        if (frontier.isEmpty()) return true;

        GraphCell current = frontier.poll();
        closedSet.add(current);
        cursor.moveToVirtualNode(current);

        if (current == target) {
            resultPath = Path.buildFromParentChain(current, parentMap);
            return true;
        }

        double currentG = gScore.get(current);

        for (GraphCell neighbor : getAccessibleNeighbors(current)) {
            if (closedSet.contains(neighbor)) continue;

            double tentativeG = currentG + 1.0;

            if (tentativeG < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                gScore.put(neighbor, tentativeG);
                fScore.put(neighbor, tentativeG + heuristic(neighbor, target));
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

    private double heuristic(GraphCell from, GraphCell to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
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
