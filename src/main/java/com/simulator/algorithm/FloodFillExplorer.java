package com.simulator.algorithm;

import com.simulator.model.GraphCell;
import com.simulator.model.Mouse;

import java.util.*;

public class FloodFillExplorer implements ExplorationAlgorithm {
    private final Map<GraphCell, Integer> floodValues = new HashMap<>();
    private boolean reachedTarget = false;

    public boolean hasReachedTarget() { return reachedTarget; }

    @Override
    public boolean executeNextStep(Mouse mouse) {
        GraphCell current = mouse.getKnownCurrentCell();
        GraphCell target = mouse.getTargetCell();

        if (target != null && current == target) {
            reachedTarget = true;
            return true;
        }

        List<Neighbor> neighbors = getAccessibleNeighbors(current);
        if (neighbors.isEmpty()) return true;

        GraphCell next = null;

        if (target != null) {
            floodFrom(target);
            if (floodValues.containsKey(current)) {
                next = pickLowestNeighbor(current, neighbors);
            }
        }

        if (next == null) {
            next = pickExploreMove(current, target);
        }

        if (next == null) return true;

        moveToward(mouse, current, next);
        if (target != null && mouse.getKnownCurrentCell() == target) {
            reachedTarget = true;
            return true;
        }
        return false;
    }

    private void floodFrom(GraphCell source) {
        floodValues.clear();
        floodValues.put(source, 0);
        Queue<GraphCell> queue = new LinkedList<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            GraphCell cell = queue.poll();
            int nextVal = floodValues.get(cell) + 1;
            for (Neighbor n : getAccessibleNeighbors(cell)) {
                if (!floodValues.containsKey(n.cell)) {
                    floodValues.put(n.cell, nextVal);
                    queue.add(n.cell);
                }
            }
        }
    }

    private GraphCell pickLowestNeighbor(GraphCell current, List<Neighbor> neighbors) {
        int currentVal = floodValues.getOrDefault(current, Integer.MAX_VALUE);
        GraphCell best = null;
        int bestVal = currentVal;
        for (Neighbor n : neighbors) {
            int val = floodValues.getOrDefault(n.cell, Integer.MAX_VALUE);
            if (val < bestVal) {
                bestVal = val;
                best = n.cell;
            } else if (val == bestVal && best != null && best.isVisited() && !n.cell.isVisited()) {
                best = n.cell;
            }
        }
        return best;
    }

    private GraphCell pickExploreMove(GraphCell start, GraphCell target) {
        Map<GraphCell, GraphCell> firstStep = new HashMap<>();
        Map<GraphCell, Integer> dist = new HashMap<>();
        Queue<GraphCell> queue = new LinkedList<>();
        Set<GraphCell> seen = new HashSet<>();

        queue.add(start);
        seen.add(start);
        firstStep.put(start, null);
        dist.put(start, 0);

        int bestDist = Integer.MAX_VALUE;
        List<GraphCell> bestFrontier = new ArrayList<>();

        while (!queue.isEmpty()) {
            GraphCell cell = queue.poll();
            int d = dist.get(cell);
            if (d > bestDist) break;

            if (cell != start && !cell.isVisited()) {
                if (d < bestDist) {
                    bestDist = d;
                    bestFrontier.clear();
                }
                bestFrontier.add(cell);
                continue;
            }

            for (Neighbor n : getAccessibleNeighbors(cell)) {
                if (!seen.contains(n.cell)) {
                    seen.add(n.cell);
                    dist.put(n.cell, d + 1);
                    firstStep.put(n.cell, firstStep.get(cell) == null ? n.cell : firstStep.get(cell));
                    queue.add(n.cell);
                }
            }
        }

        if (bestFrontier.isEmpty()) return null;

        GraphCell chosen;
        if (target != null) {
            int tx = target.getX(), ty = target.getY();
            chosen = bestFrontier.stream()
                    .min(Comparator.comparingInt(c -> Math.abs(c.getX() - tx) + Math.abs(c.getY() - ty)))
                    .orElse(bestFrontier.get(0));
        } else {
            chosen = bestFrontier.get(0);
        }

        return firstStep.get(chosen);
    }

    private void moveToward(Mouse mouse, GraphCell from, GraphCell to) {
        if (from.northNode == to) mouse.moveNorth();
        else if (from.southNode == to) mouse.moveSouth();
        else if (from.eastNode == to) mouse.moveEast();
        else if (from.westNode == to) mouse.moveWest();
    }

    private List<Neighbor> getAccessibleNeighbors(GraphCell cell) {
        List<Neighbor> neighbors = new ArrayList<>();
        if (!cell.hasNorthWall() && cell.northNode != null) neighbors.add(new Neighbor(cell.northNode, 0));
        if (!cell.hasEastWall()  && cell.eastNode  != null) neighbors.add(new Neighbor(cell.eastNode, 1));
        if (!cell.hasSouthWall() && cell.southNode != null) neighbors.add(new Neighbor(cell.southNode, 2));
        if (!cell.hasWestWall()  && cell.westNode  != null) neighbors.add(new Neighbor(cell.westNode, 3));
        return neighbors;
    }

    private record Neighbor(GraphCell cell, int heading) {}
}
