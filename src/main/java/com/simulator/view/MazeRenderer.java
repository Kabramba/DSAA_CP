package com.simulator.view;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Mouse;
import com.simulator.model.PathCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MazeRenderer extends Canvas {
    private final GraphMaze maze;
    private Mouse activeMouse = null;
    private static final double WALL_THICKNESS_RATIO = 0.08;

    private double currentCellSize = 0;
    private double currentOffsetX = 0;
    private double currentOffsetY = 0;

    public MazeRenderer(GraphMaze maze) {
        this.maze = maze;
    }

    public void setMouseEntity(Mouse mouse) {
        this.activeMouse = mouse;
    }

    private PathCursor activePathCursor = null;

    public void setPathCursor(PathCursor cursor) {
        this.activePathCursor = cursor;
    }
    /**
     * Executes an inverse mapping from screen pixel coordinates to logical graph coordinates.
     */
    public GraphCell getCellAtPixel(double pixelX, double pixelY) {
        if (currentCellSize == 0) return null;
        int logicalX = (int) ((pixelX - currentOffsetX) / currentCellSize);
        int logicalY = (int) ((pixelY - currentOffsetY) / currentCellSize);
        return maze.getNodeAt(logicalX, logicalY);
    }

    public void draw() {
        double canvasWidth = getWidth();
        double canvasHeight = getHeight();
        if (canvasWidth <= 0 || canvasHeight <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        int logicalWidth = Math.max(1, maze.getLogicalWidth());
        int logicalHeight = Math.max(1, maze.getLogicalHeight());

        double cellWidth = canvasWidth / logicalWidth;
        double cellHeight = canvasHeight / logicalHeight;
        this.currentCellSize = Math.min(cellWidth, cellHeight);
        double wallThickness = currentCellSize * WALL_THICKNESS_RATIO;

        this.currentOffsetX = (canvasWidth - (logicalWidth * currentCellSize)) / 2.0;
        this.currentOffsetY = (canvasHeight - (logicalHeight * currentCellSize)) / 2.0;

        for (GraphCell cell : maze.getAllNodes()) {
            double px = currentOffsetX + (cell.getX() * currentCellSize);
            double py = currentOffsetY + (cell.getY() * currentCellSize);

            if (cell.isVisited()) { gc.setFill(Color.web("#2a3b2a")); }
            else { gc.setFill(Color.web("#2d2d2d")); }
            gc.fillRect(px, py, currentCellSize, currentCellSize);

            // Target Highlight
            if (activeMouse != null && cell == activeMouse.getTargetCell()) {
                gc.setFill(Color.web("#8b0000", 0.5)); // Semi-transparent red
                gc.fillRect(px, py, currentCellSize, currentCellSize);
            }

            gc.setFill(Color.CYAN);
            if (cell.hasNorthWall()) gc.fillRect(px, py, currentCellSize, wallThickness);
            if (cell.hasSouthWall()) gc.fillRect(px, py + currentCellSize - wallThickness, currentCellSize, wallThickness);
            if (cell.hasWestWall()) gc.fillRect(px, py, wallThickness, currentCellSize);
            if (cell.hasEastWall()) gc.fillRect(px + currentCellSize - wallThickness, py, wallThickness, currentCellSize);
        }

        // --- Virtual PathCursor Rendering Pipeline ---
        if (activePathCursor != null) {
            // Render the expanded search history as a trail
            gc.setFill(Color.web("#ff00ff", 0.3)); // Translucent Magenta
            for (GraphCell historyNode : activePathCursor.getSearchHistory()) {
                double hx = currentOffsetX + (historyNode.getX() * currentCellSize);
                double hy = currentOffsetY + (historyNode.getY() * currentCellSize);
                gc.fillRect(hx, hy, currentCellSize, currentCellSize);
            }

            // Render the active Virtual Cursor head with LERP coordinates
            gc.setFill(Color.MAGENTA);
            double virtualPadding = currentCellSize * 0.35; // Slightly smaller than the physical mouse
            double renderPx = currentOffsetX + (activePathCursor.getRenderX() * currentCellSize);
            double renderPy = currentOffsetY + (activePathCursor.getRenderY() * currentCellSize);
            gc.fillOval(renderPx + virtualPadding, renderPy + virtualPadding,
                    currentCellSize - (virtualPadding * 2), currentCellSize - (virtualPadding * 2));
        }
        // Continuous Interpolated Cursor Rendering
        if (activeMouse != null) {
            gc.setFill(Color.YELLOW);
            double cursorPadding = currentCellSize * 0.25;
            double renderPx = currentOffsetX + (activeMouse.getRenderX() * currentCellSize);
            double renderPy = currentOffsetY + (activeMouse.getRenderY() * currentCellSize);
            gc.fillOval(renderPx + cursorPadding, renderPy + cursorPadding, currentCellSize - (cursorPadding * 2), currentCellSize - (cursorPadding * 2));
        }
    }

    @Override public boolean isResizable() { return true; }
    @Override public double prefWidth(double height) { return getWidth(); }
    @Override public double prefHeight(double width) { return getHeight(); }
}