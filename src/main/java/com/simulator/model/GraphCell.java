package com.simulator.model;

public class GraphCell {
    // Explicit topological coordinates for rendering
    private final int x;
    private final int y;

    // Explicit readable state flags
    private boolean northWall = false;
    private boolean southWall = false;
    private boolean eastWall  = false;
    private boolean westWall  = false;
    private boolean visited   = false;

    // Graph Edge References (Pointers to adjacent nodes)
    public GraphCell northNode;
    public GraphCell southNode;
    public GraphCell eastNode;
    public GraphCell westNode;

    public GraphCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // --- State Mutators and Accessors ---

    public void setNorthWall(boolean present) { this.northWall = present; }
    public boolean hasNorthWall() { return northWall; }

    public void setSouthWall(boolean present) { this.southWall = present; }
    public boolean hasSouthWall() { return southWall; }

    public void setEastWall(boolean present) { this.eastWall = present; }
    public boolean hasEastWall() { return eastWall; }

    public void setWestWall(boolean present) { this.westWall = present; }
    public boolean hasWestWall() { return westWall; }

    public void markVisited() { this.visited = true; }
    public boolean isVisited() { return visited; }

    public int getX() { return x; }
    public int getY() { return y; }
}