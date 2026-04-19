package com.simulator.algorithm;

import com.simulator.model.GraphCell;
import com.simulator.model.Mouse;

import java.util.*;

public class TremauxExplorer implements ExplorationAlgorithm {
    private final Map<Long, Integer> edgeMarks = new HashMap<>();
    private int lastHeading = -1;
    private boolean reachedTarget = false;

    public boolean hasReachedTarget() { return reachedTarget; }

    @Override
    public boolean executeNextStep(Mouse mouse) {
        GraphCell current = mouse.getKnownCurrentCell();

        if (!reachedTarget && mouse.getTargetCell() != null && current == mouse.getTargetCell()) {
            reachedTarget = true;
        }

        List<Passage> passages = getAccessiblePassages(current);
        if (passages.isEmpty()) return true;

        if (lastHeading >= 0) {
            int arrivalDir = (lastHeading + 2) % 4;
            markEdge(current, arrivalDir);
        }

        Passage chosen = choosePassage(current, passages);
        if (chosen == null) return true;

        markEdge(current, chosen.heading);
        lastHeading = chosen.heading;
        moveInDirection(mouse, chosen.heading);

        if (!reachedTarget && mouse.getTargetCell() != null
                && mouse.getKnownCurrentCell() == mouse.getTargetCell()) {
            reachedTarget = true;
        }

        return false;
    }

    private Passage choosePassage(GraphCell current, List<Passage> passages) {
        if (passages.size() == 1) {
            int marks = getEdgeMark(current, passages.get(0).heading);
            return marks < 2 ? passages.get(0) : null;
        }

        List<Passage> unmarked = new ArrayList<>();
        List<Passage> singleMarked = new ArrayList<>();

        for (Passage p : passages) {
            int marks = getEdgeMark(current, p.heading);
            if (marks == 0) unmarked.add(p);
            else if (marks == 1) singleMarked.add(p);
        }

        int arrivalDir = lastHeading >= 0 ? (lastHeading + 2) % 4 : -1;

        if (!unmarked.isEmpty()) {
            for (Passage p : unmarked) {
                if (p.heading != arrivalDir) return p;
            }
            return unmarked.get(0);
        }

        if (arrivalDir >= 0) {
            int arrivalMarks = getEdgeMark(current, arrivalDir);
            if (arrivalMarks < 2) {
                for (Passage p : passages) {
                    if (p.heading == arrivalDir) return p;
                }
            }
        }

        if (!singleMarked.isEmpty()) {
            return singleMarked.get(0);
        }

        return null;
    }

    private void markEdge(GraphCell cell, int heading) {
        long key = edgeKey(cell, heading);
        edgeMarks.merge(key, 1, Integer::sum);
    }

    private int getEdgeMark(GraphCell cell, int heading) {
        return edgeMarks.getOrDefault(edgeKey(cell, heading), 0);
    }

    private long edgeKey(GraphCell cell, int heading) {
        return ((long) System.identityHashCode(cell) << 4) | heading;
    }

    private void moveInDirection(Mouse mouse, int heading) {
        switch (heading) {
            case 0 -> mouse.moveNorth();
            case 1 -> mouse.moveEast();
            case 2 -> mouse.moveSouth();
            case 3 -> mouse.moveWest();
        }
    }

    private List<Passage> getAccessiblePassages(GraphCell cell) {
        List<Passage> passages = new ArrayList<>();
        if (!cell.hasNorthWall() && cell.northNode != null) passages.add(new Passage(cell.northNode, 0));
        if (!cell.hasEastWall()  && cell.eastNode  != null) passages.add(new Passage(cell.eastNode, 1));
        if (!cell.hasSouthWall() && cell.southNode != null) passages.add(new Passage(cell.southNode, 2));
        if (!cell.hasWestWall()  && cell.westNode  != null) passages.add(new Passage(cell.westNode, 3));
        return passages;
    }

    private record Passage(GraphCell cell, int heading) {}
}
