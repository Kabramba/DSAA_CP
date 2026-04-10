package com.simulator.model;

import java.util.ArrayList;
import java.util.List;

public class GraphMaze {
    private GraphCell startNode;
    private final List<GraphCell> allNodes;

    public GraphMaze() {
        this.allNodes = new ArrayList<>();
        buildIrregularMaze();
    }
    // NEW Constructor for the I/O Loader
    public GraphMaze(List<GraphCell> nodes, GraphCell startNode) {
        this.allNodes = nodes;
        this.startNode = startNode;
    }

    private void buildIrregularMaze() {
        GraphCell center = new GraphCell(2, 2);
        GraphCell top = new GraphCell(2, 1);
        GraphCell bottom = new GraphCell(2, 3);
        GraphCell left = new GraphCell(1, 2);
        GraphCell right = new GraphCell(3, 2);
        GraphCell farRight = new GraphCell(4, 2);

        allNodes.addAll(List.of(center, top, bottom, left, right, farRight));
        this.startNode = center;

        // Establish topological links
        center.northNode = top;    top.southNode = center;
        center.southNode = bottom; bottom.northNode = center;
        center.westNode = left;    left.eastNode = center;
        center.eastNode = right;   right.westNode = center;
        right.eastNode = farRight; farRight.westNode = right;

        // Apply physical wall boundaries explicitly
        top.setNorthWall(true);
        top.setEastWall(true);
        top.setWestWall(true);

        bottom.setSouthWall(true);
        bottom.setEastWall(true);
        bottom.setWestWall(true);

        left.setNorthWall(true);
        left.setSouthWall(true);
        left.setWestWall(true);

        farRight.setNorthWall(true);
        farRight.setSouthWall(true);
        farRight.setEastWall(true);

        // Internal walls
        center.setNorthWall(true);
        top.setSouthWall(true);
    }

    public List<GraphCell> getAllNodes() { return allNodes; }
    public GraphCell getStartNode() { return startNode; }
}