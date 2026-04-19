package com.simulator.io;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MazeFileManager {

    public static void saveMaze(GraphMaze maze, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        GraphCell start = maze.getStartNode();
        GraphCell goal = maze.getGoalNode();
        int startX = start != null ? start.getX() : 0;
        int startY = start != null ? start.getY() : 0;
        int goalX = goal != null ? goal.getX() : -1;
        int goalY = goal != null ? goal.getY() : -1;
        lines.add(String.format("META:%d,%d,%d,%d", startX, startY, goalX, goalY));

        for (GraphCell cell : maze.getAllNodes()) {
            String line = String.format("%d,%d,%d,%d,%d,%d",
                    cell.getX(),
                    cell.getY(),
                    cell.hasNorthWall() ? 1 : 0,
                    cell.hasSouthWall() ? 1 : 0,
                    cell.hasEastWall() ? 1 : 0,
                    cell.hasWestWall() ? 1 : 0
            );
            lines.add(line);
        }

        Path path = Paths.get(filePath);
        Files.write(path, lines);
    }

    public static GraphMaze loadMaze(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);

        List<GraphCell> loadedNodes = new ArrayList<>();
        Map<String, GraphCell> coordinateMap = new HashMap<>();

        int startX = 0, startY = 0;
        int goalX = -1, goalY = -1;
        boolean hasMeta = false;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            if (line.startsWith("META:")) {
                String[] metaTokens = line.substring(5).split(",");
                startX = Integer.parseInt(metaTokens[0]);
                startY = Integer.parseInt(metaTokens[1]);
                goalX = Integer.parseInt(metaTokens[2]);
                goalY = Integer.parseInt(metaTokens[3]);
                hasMeta = true;
                continue;
            }

            String[] tokens = line.split(",");
            int x = Integer.parseInt(tokens[0]);
            int y = Integer.parseInt(tokens[1]);

            GraphCell cell = new GraphCell(x, y);
            cell.setNorthWall(tokens[2].equals("1"));
            cell.setSouthWall(tokens[3].equals("1"));
            cell.setEastWall(tokens[4].equals("1"));
            cell.setWestWall(tokens[5].equals("1"));

            loadedNodes.add(cell);
            coordinateMap.put(x + "," + y, cell);
        }

        for (GraphCell cell : loadedNodes) {
            int x = cell.getX();
            int y = cell.getY();
            cell.northNode = coordinateMap.get(x + "," + (y - 1));
            cell.southNode = coordinateMap.get(x + "," + (y + 1));
            cell.eastNode  = coordinateMap.get((x + 1) + "," + y);
            cell.westNode  = coordinateMap.get((x - 1) + "," + y);
        }

        GraphCell startNode;
        if (hasMeta) {
            startNode = coordinateMap.get(startX + "," + startY);
        } else {
            startNode = loadedNodes.isEmpty() ? null : loadedNodes.get(0);
        }

        GraphCell goalNode = null;
        if (goalX >= 0 && goalY >= 0) {
            goalNode = coordinateMap.get(goalX + "," + goalY);
        }

        return new GraphMaze(loadedNodes, startNode, goalNode);
    }
}
