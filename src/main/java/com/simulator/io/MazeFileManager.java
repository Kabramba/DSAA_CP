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
    /**
     * Serializes a GraphMaize into the proprietary .mms text format.
     */
    public static void saveMaze(GraphMaze maize, String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        for (GraphCell cell : maize.getAllNodes()) {
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

    /**
     * Parses a .mms file and reconstructs the GraphMaize and its topological references.
     */
    public static GraphMaze loadMaze(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);

        List<GraphCell> loadedNodes = new ArrayList<>();
        Map<String, GraphCell> coordinateMap = new HashMap<>();

        // Phase 1: Parse data and instantiate discrete nodes
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

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

        // Phase 2: Reconstruct topological graph references
        for (GraphCell cell : loadedNodes) {
            int x = cell.getX();
            int y = cell.getY();

            cell.northNode = coordinateMap.get(x + "," + (y - 1));
            cell.southNode = coordinateMap.get(x + "," + (y + 1));
            cell.eastNode  = coordinateMap.get((x + 1) + "," + y);
            cell.westNode  = coordinateMap.get((x - 1) + "," + y);
        }

        GraphCell startNode = loadedNodes.isEmpty() ? null : loadedNodes.get(0);
        return new GraphMaze(loadedNodes, startNode);
    }
}
