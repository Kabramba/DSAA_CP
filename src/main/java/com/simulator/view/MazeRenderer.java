package com.simulator.view;

import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Mouse;
import com.simulator.model.Path;
import com.simulator.model.PathCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.List;

public class MazeRenderer extends Canvas {
    private final GraphMaze maze;
    private Mouse activeMouse = null;
    private PathCursor activePathCursor = null;
    private Path activePath = null;

    private static final double WALL_THICKNESS_RATIO = 0.06;
    private static final double WALL_INSET = 0.5;

    private static final Color BG_COLOR = Color.web("#13131a");
    private static final Color CELL_DEFAULT = Color.web("#1e1f2e");
    private static final Color CELL_VISITED = Color.web("#1a2332");
    private static final Color WALL_COLOR = Color.web("#3b4261");
    private static final Color MOUSE_COLOR = Color.web("#7aa2f7");
    private static final Color MOUSE_GLOW = Color.web("#7aa2f7", 0.3);
    private static final Color TARGET_COLOR = Color.web("#f7768e");
    private static final Color TARGET_GLOW = Color.web("#f7768e", 0.15);
    private static final Color CURSOR_COLOR = Color.web("#bb9af7");
    private static final Color CURSOR_GLOW = Color.web("#bb9af7", 0.25);
    private static final Color SEARCH_TRAIL = Color.web("#bb9af7", 0.10);
    private static final Color PATH_LINE_COLOR = Color.web("#7dcfff");
    private static final Color PATH_GLOW_COLOR = Color.web("#7dcfff", 0.15);

    private double currentCellSize = 0;
    private double currentOffsetX = 0;
    private double currentOffsetY = 0;

    public MazeRenderer(GraphMaze maze) {
        this.maze = maze;
    }

    public void setMouseEntity(Mouse mouse) {
        this.activeMouse = mouse;
    }

    public void setPathCursor(PathCursor cursor) {
        this.activePathCursor = cursor;
    }

    public void setPath(Path path) {
        this.activePath = path;
    }

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
        gc.save();

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        int logicalWidth = Math.max(1, maze.getLogicalWidth());
        int logicalHeight = Math.max(1, maze.getLogicalHeight());

        double padding = 16;
        double availW = canvasWidth - padding * 2;
        double availH = canvasHeight - padding * 2;
        double cellWidth = availW / logicalWidth;
        double cellHeight = availH / logicalHeight;
        this.currentCellSize = Math.min(cellWidth, cellHeight);
        double wallThickness = Math.max(1.5, currentCellSize * WALL_THICKNESS_RATIO);

        this.currentOffsetX = (canvasWidth - (logicalWidth * currentCellSize)) / 2.0;
        this.currentOffsetY = (canvasHeight - (logicalHeight * currentCellSize)) / 2.0;

        drawCells(gc, wallThickness);
        drawSearchTrail(gc);
        drawPath(gc);
        drawTarget(gc);
        drawPathCursor(gc);
        drawMouse(gc);

        gc.restore();
    }

    private void drawCells(GraphicsContext gc, double wallThickness) {
        double halfWall = wallThickness / 2.0;

        for (GraphCell cell : maze.getAllNodes()) {
            double px = currentOffsetX + (cell.getX() * currentCellSize);
            double py = currentOffsetY + (cell.getY() * currentCellSize);

            gc.setFill(cell.isVisited() ? CELL_VISITED : CELL_DEFAULT);
            gc.fillRect(px + WALL_INSET, py + WALL_INSET,
                    currentCellSize - WALL_INSET * 2, currentCellSize - WALL_INSET * 2);

            gc.setStroke(WALL_COLOR);
            gc.setLineWidth(wallThickness);
            gc.setLineCap(StrokeLineCap.ROUND);

            if (cell.hasNorthWall()) {
                gc.strokeLine(px, py + halfWall, px + currentCellSize, py + halfWall);
            }
            if (cell.hasSouthWall()) {
                gc.strokeLine(px, py + currentCellSize - halfWall, px + currentCellSize, py + currentCellSize - halfWall);
            }
            if (cell.hasWestWall()) {
                gc.strokeLine(px + halfWall, py, px + halfWall, py + currentCellSize);
            }
            if (cell.hasEastWall()) {
                gc.strokeLine(px + currentCellSize - halfWall, py, px + currentCellSize - halfWall, py + currentCellSize);
            }
        }
    }

    private void drawTarget(GraphicsContext gc) {
        if (activeMouse == null || activeMouse.getTargetCell() == null) return;

        GraphCell target = activeMouse.getTargetCell();
        double px = currentOffsetX + (target.getX() * currentCellSize);
        double py = currentOffsetY + (target.getY() * currentCellSize);
        double cx = px + currentCellSize / 2.0;
        double cy = py + currentCellSize / 2.0;

        gc.setFill(TARGET_GLOW);
        double glowSize = currentCellSize * 1.4;
        gc.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);

        gc.setFill(TARGET_COLOR.deriveColor(0, 1, 1, 0.25));
        gc.fillRect(px + WALL_INSET, py + WALL_INSET,
                currentCellSize - WALL_INSET * 2, currentCellSize - WALL_INSET * 2);

        gc.setStroke(TARGET_COLOR.deriveColor(0, 1, 1, 0.6));
        gc.setLineWidth(1.5);
        double inset = currentCellSize * 0.3;
        gc.strokeOval(px + inset, py + inset, currentCellSize - inset * 2, currentCellSize - inset * 2);

        gc.setFill(TARGET_COLOR);
        double dotSize = currentCellSize * 0.15;
        gc.fillOval(cx - dotSize / 2, cy - dotSize / 2, dotSize, dotSize);
    }

    private void drawSearchTrail(GraphicsContext gc) {
        if (activePathCursor == null) return;

        for (GraphCell historyNode : activePathCursor.getSearchHistory()) {
            double hx = currentOffsetX + (historyNode.getX() * currentCellSize);
            double hy = currentOffsetY + (historyNode.getY() * currentCellSize);
            gc.setFill(SEARCH_TRAIL);
            gc.fillRect(hx + WALL_INSET, hy + WALL_INSET,
                    currentCellSize - WALL_INSET * 2, currentCellSize - WALL_INSET * 2);
        }
    }

    private void drawPath(GraphicsContext gc) {
        if (activePath == null) return;

        List<GraphCell> nodes = activePath.getNodes();
        if (nodes.size() < 2) return;

        double half = currentCellSize / 2.0;
        double lineWidth = Math.max(3, currentCellSize * 0.22);

        // Glow layer
        gc.setStroke(PATH_GLOW_COLOR);
        gc.setLineWidth(lineWidth * 3);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.beginPath();
        gc.moveTo(currentOffsetX + nodes.get(0).getX() * currentCellSize + half,
                  currentOffsetY + nodes.get(0).getY() * currentCellSize + half);
        for (int i = 1; i < nodes.size(); i++) {
            gc.lineTo(currentOffsetX + nodes.get(i).getX() * currentCellSize + half,
                      currentOffsetY + nodes.get(i).getY() * currentCellSize + half);
        }
        gc.stroke();

        // Main line
        gc.setStroke(PATH_LINE_COLOR);
        gc.setLineWidth(lineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.beginPath();
        gc.moveTo(currentOffsetX + nodes.get(0).getX() * currentCellSize + half,
                  currentOffsetY + nodes.get(0).getY() * currentCellSize + half);
        for (int i = 1; i < nodes.size(); i++) {
            gc.lineTo(currentOffsetX + nodes.get(i).getX() * currentCellSize + half,
                      currentOffsetY + nodes.get(i).getY() * currentCellSize + half);
        }
        gc.stroke();

        // Start dot
        GraphCell head = nodes.get(0);
        double dotR = lineWidth * 0.8;
        gc.setFill(PATH_LINE_COLOR);
        gc.fillOval(currentOffsetX + head.getX() * currentCellSize + half - dotR,
                    currentOffsetY + head.getY() * currentCellSize + half - dotR,
                    dotR * 2, dotR * 2);

        // End dot
        GraphCell tail = nodes.get(nodes.size() - 1);
        gc.fillOval(currentOffsetX + tail.getX() * currentCellSize + half - dotR,
                    currentOffsetY + tail.getY() * currentCellSize + half - dotR,
                    dotR * 2, dotR * 2);
    }

    private void drawPathCursor(GraphicsContext gc) {
        if (activePathCursor == null) return;

        double renderPx = currentOffsetX + (activePathCursor.getRenderX() * currentCellSize);
        double renderPy = currentOffsetY + (activePathCursor.getRenderY() * currentCellSize);
        double cx = renderPx + currentCellSize / 2.0;
        double cy = renderPy + currentCellSize / 2.0;

        gc.setFill(CURSOR_GLOW);
        double glowR = currentCellSize * 0.9;
        gc.fillOval(cx - glowR / 2, cy - glowR / 2, glowR, glowR);

        double size = currentCellSize * 0.35;
        gc.setFill(CURSOR_COLOR);
        gc.fillOval(cx - size / 2, cy - size / 2, size, size);
    }

    private void drawMouse(GraphicsContext gc) {
        if (activeMouse == null) return;

        double renderPx = currentOffsetX + (activeMouse.getRenderX() * currentCellSize);
        double renderPy = currentOffsetY + (activeMouse.getRenderY() * currentCellSize);
        double cx = renderPx + currentCellSize / 2.0;
        double cy = renderPy + currentCellSize / 2.0;

        gc.setFill(MOUSE_GLOW);
        double glowR = currentCellSize * 1.1;
        gc.fillOval(cx - glowR / 2, cy - glowR / 2, glowR, glowR);

        double size = currentCellSize * 0.45;
        gc.setFill(MOUSE_COLOR);
        gc.fillOval(cx - size / 2, cy - size / 2, size, size);

        double innerSize = size * 0.45;
        gc.setFill(Color.web("#c0caf5", 0.7));
        gc.fillOval(cx - innerSize / 2, cy - innerSize / 2, innerSize, innerSize);
    }

    @Override public boolean isResizable() { return true; }
    @Override public double prefWidth(double height) { return getWidth(); }
    @Override public double prefHeight(double width) { return getHeight(); }
}