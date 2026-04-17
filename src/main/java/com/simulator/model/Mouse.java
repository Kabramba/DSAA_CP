package com.simulator.model;

public class Mouse {
    private GraphCell trueCurrentCell;
    private GraphCell knownCurrentCell;
    private final GraphMaze interpretMaze;

    private GraphCell targetCell = null;

    // Fractional coordinates for continuous animation rendering (LERP)
    private double renderX;
    private double renderY;

    public Mouse(GraphCell startTrue, GraphCell startKnown, GraphMaze interpretMaze) {
        this.trueCurrentCell = startTrue;
        this.knownCurrentCell = startKnown;
        this.interpretMaze = interpretMaze;

        // Initialize render coordinates to strictly match logical bounds
        this.renderX = startTrue.getX();
        this.renderY = startTrue.getY();

        discoverSurroundings();
    }

    public void setTargetCell(GraphCell target) { this.targetCell = target; }
    public GraphCell getTargetCell() { return targetCell; }

    public double getRenderX() { return renderX; }
    public double getRenderY() { return renderY; }
    public void setRenderX(double x) { this.renderX = x; }
    public void setRenderY(double y) { this.renderY = y; }

    public void moveNorth() {
        if (!trueCurrentCell.hasNorthWall() && trueCurrentCell.northNode != null) {
            trueCurrentCell = trueCurrentCell.northNode;
            knownCurrentCell = knownCurrentCell.northNode;
            discoverSurroundings();
        }
    }

    public void moveSouth() {
        if (!trueCurrentCell.hasSouthWall() && trueCurrentCell.southNode != null) {
            trueCurrentCell = trueCurrentCell.southNode;
            knownCurrentCell = knownCurrentCell.southNode;
            discoverSurroundings();
        }
    }

    public void moveEast() {
        if (!trueCurrentCell.hasEastWall() && trueCurrentCell.eastNode != null) {
            trueCurrentCell = trueCurrentCell.eastNode;
            knownCurrentCell = knownCurrentCell.eastNode;
            discoverSurroundings();
        }
    }

    public void moveWest() {
        if (!trueCurrentCell.hasWestWall() && trueCurrentCell.westNode != null) {
            trueCurrentCell = trueCurrentCell.westNode;
            knownCurrentCell = knownCurrentCell.westNode;
            discoverSurroundings();
        }
    }

    private void discoverSurroundings() {
        trueCurrentCell.markVisited();
        knownCurrentCell.markVisited();

        knownCurrentCell.setNorthWall(trueCurrentCell.hasNorthWall());
        knownCurrentCell.setSouthWall(trueCurrentCell.hasSouthWall());
        knownCurrentCell.setEastWall(trueCurrentCell.hasEastWall());
        knownCurrentCell.setWestWall(trueCurrentCell.hasWestWall());

        allocatePathway(knownCurrentCell, 0, -1, "NORTH");
        allocatePathway(knownCurrentCell, 0, 1, "SOUTH");
        allocatePathway(knownCurrentCell, 1, 0, "EAST");
        allocatePathway(knownCurrentCell, -1, 0, "WEST");
    }

    private void allocatePathway(GraphCell origin, int dx, int dy, String direction) {
        boolean wallBlocked = false;
        switch (direction) {
            case "NORTH" -> wallBlocked = origin.hasNorthWall();
            case "SOUTH" -> wallBlocked = origin.hasSouthWall();
            case "EAST"  -> wallBlocked = origin.hasEastWall();
            case "WEST"  -> wallBlocked = origin.hasWestWall();
        }

        if (!wallBlocked) {
            int targetX = origin.getX() + dx;
            int targetY = origin.getY() + dy;

            GraphCell targetNode = interpretMaze.getNodeAt(targetX, targetY);

            if (targetNode == null) {
                targetNode = new GraphCell(targetX, targetY);
                interpretMaze.addNode(targetNode);
            }

            switch (direction) {
                case "NORTH" -> { origin.northNode = targetNode; targetNode.southNode = origin; }
                case "SOUTH" -> { origin.southNode = targetNode; targetNode.northNode = origin; }
                case "EAST"  -> { origin.eastNode = targetNode; targetNode.westNode = origin; }
                case "WEST"  -> { origin.westNode = targetNode; targetNode.eastNode = origin; }
            }
        }
    }

    public GraphCell getTrueCurrentCell() { return trueCurrentCell; }
    public GraphCell getKnownCurrentCell() { return knownCurrentCell; }
}