package com.simulator.io;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class MazeGenerator {

    /**
     * Generates a randomized, perfectly traversable maze using DFS Recursive Backtracking.
     */
    public static void generateAndSave(int width, int height, String filename) throws IOException {
        GraphCell[][] grid = new GraphCell[width][height];
        List<GraphCell> allNodes = new ArrayList<>();

        // 1. Initialize a grid where every cell is completely walled off
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GraphCell cell = new GraphCell(x, y);
                cell.setNorthWall(true);
                cell.setSouthWall(true);
                cell.setEastWall(true);
                cell.setWestWall(true);
                grid[x][y] = cell;
                allNodes.add(cell);
            }
        }

        // 2. Execute Randomized Depth-First Search
        Stack<GraphCell> stack = new Stack<>();
        GraphCell current = grid[0][0];
        current.markVisited();
        stack.push(current);

        while (!stack.isEmpty()) {
            current = stack.pop();
            List<GraphCell> unvisitedNeighbors = getUnvisitedNeighbors(current, grid, width, height);

            if (!unvisitedNeighbors.isEmpty()) {
                // Push current back to stack to backtrack later
                stack.push(current);

                // Pick a random unvisited neighbor
                Collections.shuffle(unvisitedNeighbors);
                GraphCell next = unvisitedNeighbors.get(0);

                // Tear down the walls between the current node and the next node
                removeWalls(current, next);

                next.markVisited();
                stack.push(next);
            }
        }

        // 3. Package the graph and serialize it to the disk
        GraphMaze generatedMaze = new GraphMaze(allNodes, grid[0][0]);
        MazeFileManager.saveMaze(generatedMaze, filename);
    }

    private static List<GraphCell> getUnvisitedNeighbors(GraphCell cell, GraphCell[][] grid, int w, int h) {
        List<GraphCell> neighbors = new ArrayList<>();
        int x = cell.getX();
        int y = cell.getY();

        // Check boundaries and visited state
        if (y > 0 && !grid[x][y - 1].isVisited()) neighbors.add(grid[x][y - 1]); // North
        if (y < h - 1 && !grid[x][y + 1].isVisited()) neighbors.add(grid[x][y + 1]); // South
        if (x < w - 1 && !grid[x + 1][y].isVisited()) neighbors.add(grid[x + 1][y]); // East
        if (x > 0 && !grid[x - 1][y].isVisited()) neighbors.add(grid[x - 1][y]); // West

        return neighbors;
    }

    private static void removeWalls(GraphCell current, GraphCell next) {
        int dx = current.getX() - next.getX();
        int dy = current.getY() - next.getY();

        if (dx == 1) { // Next is to the West
            current.setWestWall(false);
            next.setEastWall(false);
        } else if (dx == -1) { // Next is to the East
            current.setEastWall(false);
            next.setWestWall(false);
        }

        if (dy == 1) { // Next is to the North
            current.setNorthWall(false);
            next.setSouthWall(false);
        } else if (dy == -1) { // Next is to the South
            current.setSouthWall(false);
            next.setNorthWall(false);
        }
    }
}