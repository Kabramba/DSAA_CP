package com.simulator.view;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MazeRenderer extends Canvas {
    private final GraphMaze maze;
    private static final int CELL_SIZE = 60;
    private static final int WALL_THICKNESS = 4;

    public MazeRenderer(GraphMaze maze, double width, double height) {
        super(width, height);
        this.maze = maze;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        for (GraphCell cell : maze.getAllNodes()) {
            int px = cell.getX() * CELL_SIZE;
            int py = cell.getY() * CELL_SIZE;

            gc.setFill(Color.web("#2d2d2d"));
            gc.fillRect(px, py, CELL_SIZE, CELL_SIZE);

            gc.setFill(Color.CYAN);

            // Refactored to use readable boolean queries
            if (cell.hasNorthWall()) {
                gc.fillRect(px, py, CELL_SIZE, WALL_THICKNESS);
            }
            if (cell.hasSouthWall()) {
                gc.fillRect(px, py + CELL_SIZE - WALL_THICKNESS, CELL_SIZE, WALL_THICKNESS);
            }
            if (cell.hasWestWall()) {
                gc.fillRect(px, py, WALL_THICKNESS, CELL_SIZE);
            }
            if (cell.hasEastWall()) {
                gc.fillRect(px + CELL_SIZE - WALL_THICKNESS, py, WALL_THICKNESS, CELL_SIZE);
            }
        }
    }
}