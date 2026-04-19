package com.simulator.io;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;

import java.io.IOException;
import java.util.*;

public class MazeGenerator {

    public static void generateAndSave(int width, int height, String filename) throws IOException {
        GraphMaze maze = generate(width, height);
        MazeFileManager.saveMaze(maze, filename);
    }

    public static GraphMaze generate(int width, int height) {
        Random rng = new Random();
        GraphCell[][] grid = new GraphCell[width][height];
        List<GraphCell> allNodes = new ArrayList<>();

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

        // Phase 1: DFS spanning tree — guarantees full connectivity
        boolean[][] visited = new boolean[width][height];
        Stack<GraphCell> stack = new Stack<>();
        visited[0][0] = true;
        stack.push(grid[0][0]);

        while (!stack.isEmpty()) {
            GraphCell current = stack.peek();
            List<GraphCell> unvisited = getUnvisitedNeighbors(current, grid, visited, width, height);

            if (unvisited.isEmpty()) {
                stack.pop();
            } else {
                GraphCell next = unvisited.get(rng.nextInt(unvisited.size()));
                removeWallsBetween(current, next);
                visited[next.getX()][next.getY()] = true;
                stack.push(next);
            }
        }

        // Phase 2: Random wall removal to create loops and multiple paths
        // Higher ratio = more open maze with more loops
        double loopRatio = 0.15 + rng.nextDouble() * 0.10;
        List<int[]> interiorWalls = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x < width - 1 && grid[x][y].hasEastWall()) {
                    interiorWalls.add(new int[]{x, y, x + 1, y});
                }
                if (y < height - 1 && grid[x][y].hasSouthWall()) {
                    interiorWalls.add(new int[]{x, y, x, y + 1});
                }
            }
        }

        Collections.shuffle(interiorWalls, rng);
        int wallsToRemove = (int) (interiorWalls.size() * loopRatio);
        for (int i = 0; i < wallsToRemove && i < interiorWalls.size(); i++) {
            int[] wall = interiorWalls.get(i);
            removeWallsBetween(grid[wall[0]][wall[1]], grid[wall[2]][wall[3]]);
        }

        // Phase 3: Open the center goal zone (2x2 area, like real micromouse)
        int cx = width / 2 - 1;
        int cy = height / 2 - 1;
        cx = Math.max(1, Math.min(cx, width - 3));
        cy = Math.max(1, Math.min(cy, height - 3));

        for (int gx = cx; gx <= cx + 1; gx++) {
            for (int gy = cy; gy <= cy + 1; gy++) {
                if (gx < cx + 1) removeWallsBetween(grid[gx][gy], grid[gx + 1][gy]);
                if (gy < cy + 1) removeWallsBetween(grid[gx][gy], grid[gx][gy + 1]);
            }
        }

        // Phase 4: Ensure at least one entrance into the goal zone from each side
        ensureGoalEntrance(grid, cx, cy, cx + 1, cy + 1, width, height, rng);

        // Phase 5: Link topological graph edges
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (y > 0)          grid[x][y].northNode = grid[x][y - 1];
                if (y < height - 1) grid[x][y].southNode = grid[x][y + 1];
                if (x < width - 1)  grid[x][y].eastNode  = grid[x + 1][y];
                if (x > 0)          grid[x][y].westNode  = grid[x - 1][y];
            }
        }

        GraphCell goalNode = grid[cx][cy];
        return new GraphMaze(allNodes, grid[0][0], goalNode);
    }

    private static void ensureGoalEntrance(GraphCell[][] grid, int gx1, int gy1, int gx2, int gy2,
                                           int w, int h, Random rng) {
        boolean hasEntrance = false;

        // Check north edge of goal zone
        for (int x = gx1; x <= gx2; x++) {
            if (gy1 > 0 && !grid[x][gy1].hasNorthWall()) hasEntrance = true;
        }
        // Check south edge
        for (int x = gx1; x <= gx2; x++) {
            if (gy2 < h - 1 && !grid[x][gy2].hasSouthWall()) hasEntrance = true;
        }
        // Check west edge
        for (int y = gy1; y <= gy2; y++) {
            if (gx1 > 0 && !grid[gx1][y].hasWestWall()) hasEntrance = true;
        }
        // Check east edge
        for (int y = gy1; y <= gy2; y++) {
            if (gx2 < w - 1 && !grid[gx2][y].hasEastWall()) hasEntrance = true;
        }

        if (hasEntrance) return;

        // Force open a random entrance
        List<Runnable> candidates = new ArrayList<>();
        if (gy1 > 0) {
            int x = gx1 + rng.nextInt(gx2 - gx1 + 1);
            candidates.add(() -> removeWallsBetween(grid[x][gy1], grid[x][gy1 - 1]));
        }
        if (gy2 < h - 1) {
            int x = gx1 + rng.nextInt(gx2 - gx1 + 1);
            candidates.add(() -> removeWallsBetween(grid[x][gy2], grid[x][gy2 + 1]));
        }
        if (gx1 > 0) {
            int y = gy1 + rng.nextInt(gy2 - gy1 + 1);
            candidates.add(() -> removeWallsBetween(grid[gx1][y], grid[gx1 - 1][y]));
        }
        if (gx2 < w - 1) {
            int y = gy1 + rng.nextInt(gy2 - gy1 + 1);
            candidates.add(() -> removeWallsBetween(grid[gx2][y], grid[gx2 + 1][y]));
        }

        if (!candidates.isEmpty()) {
            candidates.get(rng.nextInt(candidates.size())).run();
        }
    }

    private static List<GraphCell> getUnvisitedNeighbors(GraphCell cell, GraphCell[][] grid,
                                                         boolean[][] visited, int w, int h) {
        List<GraphCell> neighbors = new ArrayList<>();
        int x = cell.getX(), y = cell.getY();
        if (y > 0 && !visited[x][y - 1])     neighbors.add(grid[x][y - 1]);
        if (y < h - 1 && !visited[x][y + 1]) neighbors.add(grid[x][y + 1]);
        if (x < w - 1 && !visited[x + 1][y]) neighbors.add(grid[x + 1][y]);
        if (x > 0 && !visited[x - 1][y])     neighbors.add(grid[x - 1][y]);
        return neighbors;
    }

    private static void removeWallsBetween(GraphCell a, GraphCell b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        if (dx == 1)  { a.setWestWall(false);  b.setEastWall(false);  }
        if (dx == -1) { a.setEastWall(false);  b.setWestWall(false);  }
        if (dy == 1)  { a.setNorthWall(false); b.setSouthWall(false); }
        if (dy == -1) { a.setSouthWall(false); b.setNorthWall(false); }
    }
}
