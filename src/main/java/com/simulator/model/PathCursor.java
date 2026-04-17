package com.simulator.model;

import java.util.ArrayList;
import java.util.List;

public class PathCursor {
    private GraphCell currentCell;
    private final GraphCell targetCell;

    // Animation rendering coordinates
    private double renderX;
    private double renderY;

    // Tracks the traversal history for visualization
    private final List<GraphCell> searchHistory;

    public PathCursor(GraphCell startCell, GraphCell targetCell) {
        this.currentCell = startCell;
        this.targetCell = targetCell;
        this.renderX = startCell.getX();
        this.renderY = startCell.getY();
        this.searchHistory = new ArrayList<>();
        this.searchHistory.add(startCell);
    }

    public void moveToVirtualNode(GraphCell nextNode) {
        this.currentCell = nextNode;
        if (!searchHistory.contains(nextNode)) {
            searchHistory.add(nextNode);
        }
    }

    public GraphCell getCurrentCell() { return currentCell; }
    public GraphCell getTargetCell() { return targetCell; }

    public double getRenderX() { return renderX; }
    public double getRenderY() { return renderY; }
    public void setRenderX(double x) { this.renderX = x; }
    public void setRenderY(double y) { this.renderY = y; }

    public List<GraphCell> getSearchHistory() { return searchHistory; }
}