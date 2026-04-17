package com.simulator;

import com.simulator.algorithm.BFSPathfinder;
import com.simulator.algorithm.ExplorationAlgorithm;
import com.simulator.algorithm.LeftWallFollower;
import com.simulator.algorithm.PathfindingAlgorithm;
import com.simulator.io.MazeFileManager;
import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Mouse;
import com.simulator.model.PathCursor;
import com.simulator.view.MazeRenderer;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {
    private Timeline simulationClock;

    // Distinct State Pointers
    private ExplorationAlgorithm activeExplorationAlgo = null;
    private PathfindingAlgorithm activePathfindingAlgo = null;

    private PathCursor virtualCursor = null;

    @Override
    public void start(Stage primaryStage) {
        GraphMaze simMap;
        try { simMap = MazeFileManager.loadMaze("massive_maze.mms"); }
        catch (IOException e) { e.printStackTrace(); return; }

        GraphCell startTrue = simMap.getStartNode();
        GraphCell startKnown = new GraphCell(startTrue.getX(), startTrue.getY());
        ArrayList<GraphCell> initialMemoryNodes = new ArrayList<>();
        initialMemoryNodes.add(startKnown);
        GraphMaze interpretMap = new GraphMaze(initialMemoryNodes, startKnown);

        Mouse mouse = new Mouse(startTrue, startKnown, interpretMap);

        MazeRenderer simRenderer = new MazeRenderer(simMap);
        MazeRenderer interpretRenderer = new MazeRenderer(interpretMap);
        simRenderer.setMouseEntity(mouse);
        interpretRenderer.setMouseEntity(mouse);

        BorderPane root = new BorderPane();
        HBox viewports = new HBox();
        HBox.setHgrow(simRenderer, Priority.ALWAYS);
        HBox.setHgrow(interpretRenderer, Priority.ALWAYS);
        simRenderer.widthProperty().bind(viewports.widthProperty().divide(2));
        interpretRenderer.widthProperty().bind(viewports.widthProperty().divide(2));
        simRenderer.heightProperty().bind(viewports.heightProperty());
        interpretRenderer.heightProperty().bind(viewports.heightProperty());
        viewports.getChildren().addAll(simRenderer, interpretRenderer);
        root.setCenter(viewports);

        HBox controlBar = new HBox(10);
        controlBar.setPadding(new Insets(10));
        controlBar.setStyle("-fx-background-color: #333333;");

        ComboBox<String> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll("Manual Teleop", "[SLAM] Left Wall Follower", "[DSA] BFS Pathfinding");
        modeSelector.setValue("Manual Teleop");

        modeSelector.setOnAction(e -> {
            String selected = modeSelector.getValue();
            simulationClock.pause(); // Reset clock on context switch

            if (selected.equals("[SLAM] Left Wall Follower")) {
                activeExplorationAlgo = new LeftWallFollower();
                activePathfindingAlgo = null;
            } else if (selected.equals("[DSA] BFS Pathfinding")) {
                activeExplorationAlgo = null;
                activePathfindingAlgo = new BFSPathfinder();

                // Initialize virtual agent if a target exists
                if (mouse.getTargetCell() != null) {
                    virtualCursor = new PathCursor(mouse.getKnownCurrentCell(), mouse.getTargetCell());
                    interpretRenderer.setPathCursor(virtualCursor);
                }
            } else {
                activeExplorationAlgo = null;
                activePathfindingAlgo = null;
            }
        });

        Button btnToggle = new Button("Play / Pause");
        btnToggle.setOnAction(e -> {
            if (simulationClock.getStatus() == Timeline.Status.RUNNING) simulationClock.pause();
            else simulationClock.play();
        });

        controlBar.getChildren().addAll(modeSelector, btnToggle);
        root.setTop(controlBar);

        // Spatial Targeting: Click to set destination and spawn Virtual Agent
        interpretRenderer.setOnMouseClicked(event -> {
            GraphCell clickedCell = interpretRenderer.getCellAtPixel(event.getX(), event.getY());
            if (clickedCell != null) {
                mouse.setTargetCell(clickedCell);
                // Dynamically bind the virtual cursor if in offline-planning mode
                if (activePathfindingAlgo != null) {
                    virtualCursor = new PathCursor(mouse.getKnownCurrentCell(), clickedCell);
                    interpretRenderer.setPathCursor(virtualCursor);
                }
                interpretRenderer.draw();
            }
        });

        // Temporal Execution Logic
        simulationClock = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
            boolean finished = false;

            // Route execution cycle based on active state pointer
            if (activeExplorationAlgo != null) {
                finished = activeExplorationAlgo.executeNextStep(mouse);
            } else if (activePathfindingAlgo != null && virtualCursor != null) {
                finished = activePathfindingAlgo.executeNextStep(virtualCursor, interpretMap);
            }

            if (finished) {
                simulationClock.pause();
                System.out.println("Execution State: Terminal Condition Met.");
            }
        }));
        simulationClock.setCycleCount(Timeline.INDEFINITE);

        // LERP Rendering Engine
        AnimationTimer renderEngine = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double smoothing = 0.2;

                // Animate Physical Agent
                double targetX = mouse.getKnownCurrentCell().getX();
                double targetY = mouse.getKnownCurrentCell().getY();
                mouse.setRenderX(mouse.getRenderX() + (targetX - mouse.getRenderX()) * smoothing);
                mouse.setRenderY(mouse.getRenderY() + (targetY - mouse.getRenderY()) * smoothing);

                // Animate Virtual Agent
                if (virtualCursor != null) {
                    double vTargetX = virtualCursor.getCurrentCell().getX();
                    double vTargetY = virtualCursor.getCurrentCell().getY();
                    virtualCursor.setRenderX(virtualCursor.getRenderX() + (vTargetX - virtualCursor.getRenderX()) * smoothing);
                    virtualCursor.setRenderY(virtualCursor.getRenderY() + (vTargetY - virtualCursor.getRenderY()) * smoothing);
                }

                simRenderer.draw();
                interpretRenderer.draw();
            }
        };
        renderEngine.start();

        Scene scene = new Scene(root, 1200, 650);
        primaryStage.setTitle("Algorithmic Micromouse SLAM Environment");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}