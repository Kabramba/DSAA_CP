package com.simulator;

import com.simulator.algorithm.AStarPathfinder;
import com.simulator.algorithm.BFSPathfinder;
import com.simulator.algorithm.DijkstraPathfinder;
import com.simulator.algorithm.ExplorationAlgorithm;
import com.simulator.algorithm.LeftWallFollower;
import com.simulator.algorithm.PathfindingAlgorithm;
import com.simulator.io.MazeFileManager;
import com.simulator.model.GraphCell;
import com.simulator.model.GraphMaze;
import com.simulator.model.Mouse;
import com.simulator.model.Path;
import com.simulator.model.PathCursor;
import com.simulator.view.MazeRenderer;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {
    private Timeline simulationClock;
    private int stepCount = 0;

    private boolean manualTeleop = true;
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

        // ── Header bar ──
        HBox headerBar = new HBox();
        headerBar.getStyleClass().add("header-bar");

        Label appTitle = new Label("Micromouse Simulator");
        appTitle.getStyleClass().add("app-title");

        Region sep1 = new Region();
        sep1.getStyleClass().add("header-separator");

        // Exploration algorithm selector
        Label exploreLabel = new Label("Exploration");
        exploreLabel.getStyleClass().add("speed-label");

        ComboBox<String> explorationSelector = new ComboBox<>();
        explorationSelector.getItems().addAll("Manual Teleop", "Left Wall Follower");
        explorationSelector.setValue("Manual Teleop");
        explorationSelector.getStyleClass().add("control-chip");

        Region sep2 = new Region();
        sep2.getStyleClass().add("header-separator");

        // Pathfinding algorithm selector
        Label pathLabel = new Label("Pathfinding");
        pathLabel.getStyleClass().add("speed-label");

        ComboBox<String> pathfindingSelector = new ComboBox<>();
        pathfindingSelector.getItems().addAll("None", "BFS", "Dijkstra", "A*");
        pathfindingSelector.setValue("None");
        pathfindingSelector.getStyleClass().add("control-chip");

        Region sep3 = new Region();
        sep3.getStyleClass().add("header-separator");

        Button btnToggle = new Button("\u25B6  Play");
        btnToggle.getStyleClass().add("play-btn");
        btnToggle.setFocusTraversable(false);

        Button btnStep = new Button("Step");
        btnStep.getStyleClass().add("control-chip");
        btnStep.setFocusTraversable(false);

        Button btnReset = new Button("Reset");
        btnReset.getStyleClass().add("control-chip");
        btnReset.setFocusTraversable(false);

        Region sep4 = new Region();
        sep4.getStyleClass().add("header-separator");

        Label speedLabel = new Label("Speed");
        speedLabel.getStyleClass().add("speed-label");

        Slider speedSlider = new Slider(50, 1000, 500);
        speedSlider.setPrefWidth(120);
        speedSlider.setBlockIncrement(50);
        speedSlider.setFocusTraversable(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stepLabel = new Label("Steps: 0");
        stepLabel.getStyleClass().add("status-text");

        headerBar.getChildren().addAll(
                appTitle, sep1,
                exploreLabel, explorationSelector, sep2,
                pathLabel, pathfindingSelector, sep3,
                btnToggle, btnStep, btnReset, sep4,
                speedLabel, speedSlider,
                spacer, stepLabel
        );

        // ── Viewports ──
        VBox simCard = createViewportCard("GROUND TRUTH", simRenderer);
        VBox interpretCard = createViewportCard("MOUSE MEMORY", interpretRenderer);

        HBox viewports = new HBox(10);
        viewports.getStyleClass().add("viewport-container");
        HBox.setHgrow(simCard, Priority.ALWAYS);
        HBox.setHgrow(interpretCard, Priority.ALWAYS);
        viewports.getChildren().addAll(simCard, interpretCard);

        // ── Status bar ──
        HBox statusBar = new HBox();
        statusBar.getStyleClass().add("status-bar");

        Label modeStatus = new Label("Exploration: Manual Teleop");
        modeStatus.getStyleClass().add("status-text");

        Label pathStatus = new Label("Pathfinding: None");
        pathStatus.getStyleClass().add("status-text");

        Label stateStatus = new Label("State: Idle");
        stateStatus.getStyleClass().add("status-text");

        Label mazeInfo = new Label(String.format("Maze: %dx%d", simMap.getLogicalWidth(), simMap.getLogicalHeight()));
        mazeInfo.getStyleClass().add("status-text");

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        Label hint = new Label("Arrow keys to move  |  Click Memory map to set target");
        hint.getStyleClass().add("status-text");

        statusBar.getChildren().addAll(modeStatus, pathStatus, stateStatus, mazeInfo, statusSpacer, hint);

        // ── Root layout ──
        BorderPane root = new BorderPane();
        root.setTop(headerBar);
        root.setCenter(viewports);
        root.setBottom(statusBar);

        // ── Simulation tick handler ──
        Runnable tickAction = () -> {
            boolean finished = false;

            if (activeExplorationAlgo != null) {
                finished = activeExplorationAlgo.executeNextStep(mouse);
            } else if (activePathfindingAlgo != null && virtualCursor != null) {
                finished = activePathfindingAlgo.executeNextStep(virtualCursor, interpretMap);
            }

            stepCount++;
            stepLabel.setText("Steps: " + stepCount);

            if (finished) {
                simulationClock.pause();
                stateStatus.setText("State: Finished");
                btnToggle.setText("\u25B6  Play");

                if (activePathfindingAlgo != null) {
                    Path result = activePathfindingAlgo.getResultPath();
                    interpretRenderer.setPath(result);
                }
            }
        };

        // ── Simulation clock ──
        simulationClock = new Timeline(new KeyFrame(Duration.millis(500), event -> tickAction.run()));
        simulationClock.setCycleCount(Timeline.INDEFINITE);

        // ── Speed slider binding ──
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double ms = 1050 - newVal.doubleValue();
            boolean wasRunning = simulationClock.getStatus() == Timeline.Status.RUNNING;
            simulationClock.stop();
            simulationClock.getKeyFrames().setAll(new KeyFrame(Duration.millis(ms), event -> tickAction.run()));
            simulationClock.setCycleCount(Timeline.INDEFINITE);
            if (wasRunning) simulationClock.play();
        });

        // ── Exploration selector ──
        explorationSelector.setOnAction(e -> {
            String selected = explorationSelector.getValue();
            simulationClock.pause();
            btnToggle.setText("\u25B6  Play");
            stateStatus.setText("State: Idle");

            if (selected.equals("Manual Teleop")) {
                manualTeleop = true;
                activeExplorationAlgo = null;
                hint.setText("Arrow keys to move  |  Click Memory map to set target");
            } else if (selected.equals("Left Wall Follower")) {
                manualTeleop = false;
                activeExplorationAlgo = new LeftWallFollower();
                hint.setText("Press Play to run exploration  |  Click Memory map to set target");
            }
            modeStatus.setText("Exploration: " + selected);
        });

        // ── Pathfinding selector ──
        pathfindingSelector.setOnAction(e -> {
            String selected = pathfindingSelector.getValue();
            simulationClock.pause();
            btnToggle.setText("\u25B6  Play");
            stateStatus.setText("State: Idle");

            activePathfindingAlgo = createPathfinder(selected);
            interpretRenderer.setPath(null);
            if (activePathfindingAlgo != null && mouse.getTargetCell() != null) {
                virtualCursor = new PathCursor(mouse.getKnownCurrentCell(), mouse.getTargetCell());
                interpretRenderer.setPathCursor(virtualCursor);
            } else if (activePathfindingAlgo == null) {
                virtualCursor = null;
                interpretRenderer.setPathCursor(null);
            }
            pathStatus.setText("Pathfinding: " + selected);
        });

        // ── Play / Pause ──
        btnToggle.setOnAction(e -> {
            if (simulationClock.getStatus() == Timeline.Status.RUNNING) {
                simulationClock.pause();
                btnToggle.setText("\u25B6  Play");
                stateStatus.setText("State: Paused");
            } else {
                simulationClock.play();
                btnToggle.setText("\u23F8  Pause");
                stateStatus.setText("State: Running");
            }
        });

        // ── Single step ──
        btnStep.setOnAction(e -> {
            simulationClock.pause();
            btnToggle.setText("\u25B6  Play");
            tickAction.run();
            if (!stateStatus.getText().equals("State: Finished")) {
                stateStatus.setText("State: Stepped");
            }
        });

        // ── Reset ──
        btnReset.setOnAction(e -> {
            simulationClock.pause();
            btnToggle.setText("\u25B6  Play");
            stateStatus.setText("State: Idle");
            stepCount = 0;
            stepLabel.setText("Steps: 0");

            String explSelection = explorationSelector.getValue();
            if (explSelection.equals("Left Wall Follower")) {
                activeExplorationAlgo = new LeftWallFollower();
            }

            interpretRenderer.setPath(null);

            String pathSelection = pathfindingSelector.getValue();
            activePathfindingAlgo = createPathfinder(pathSelection);
            if (activePathfindingAlgo != null && mouse.getTargetCell() != null) {
                virtualCursor = new PathCursor(mouse.getKnownCurrentCell(), mouse.getTargetCell());
                interpretRenderer.setPathCursor(virtualCursor);
            }
        });

        // ── Click to set target ──
        interpretRenderer.setOnMouseClicked(event -> {
            GraphCell clickedCell = interpretRenderer.getCellAtPixel(event.getX(), event.getY());
            if (clickedCell != null) {
                mouse.setTargetCell(clickedCell);
                interpretRenderer.setPath(null);
                if (activePathfindingAlgo != null) {
                    activePathfindingAlgo = createPathfinder(pathfindingSelector.getValue());
                    virtualCursor = new PathCursor(mouse.getKnownCurrentCell(), clickedCell);
                    interpretRenderer.setPathCursor(virtualCursor);
                }
                interpretRenderer.draw();
            }
        });

        // ── Render loop ──
        AnimationTimer renderEngine = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double smoothing = 0.15;

                double targetX = mouse.getKnownCurrentCell().getX();
                double targetY = mouse.getKnownCurrentCell().getY();
                mouse.setRenderX(mouse.getRenderX() + (targetX - mouse.getRenderX()) * smoothing);
                mouse.setRenderY(mouse.getRenderY() + (targetY - mouse.getRenderY()) * smoothing);

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

        // ── Scene setup ──
        Scene scene = new Scene(root, 1400, 750);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        // ── Keyboard handling for Manual Teleop ──
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!manualTeleop) return;

            switch (event.getCode()) {
                case UP, W -> {
                    mouse.moveNorth();
                    stepCount++;
                    stepLabel.setText("Steps: " + stepCount);
                    stateStatus.setText("State: Moving");
                    event.consume();
                }
                case DOWN, S -> {
                    mouse.moveSouth();
                    stepCount++;
                    stepLabel.setText("Steps: " + stepCount);
                    stateStatus.setText("State: Moving");
                    event.consume();
                }
                case RIGHT, D -> {
                    mouse.moveEast();
                    stepCount++;
                    stepLabel.setText("Steps: " + stepCount);
                    stateStatus.setText("State: Moving");
                    event.consume();
                }
                case LEFT, A -> {
                    mouse.moveWest();
                    stepCount++;
                    stepLabel.setText("Steps: " + stepCount);
                    stateStatus.setText("State: Moving");
                    event.consume();
                }
                default -> {}
            }
        });

        primaryStage.setTitle("Micromouse Simulator");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static PathfindingAlgorithm createPathfinder(String name) {
        return switch (name) {
            case "BFS" -> new BFSPathfinder();
            case "Dijkstra" -> new DijkstraPathfinder();
            case "A*" -> new AStarPathfinder();
            default -> null;
        };
    }

    private VBox createViewportCard(String title, MazeRenderer renderer) {
        VBox card = new VBox();
        card.getStyleClass().add("viewport-card");

        Label label = new Label(title);
        label.getStyleClass().add("viewport-label");

        StackPane canvasWrapper = new StackPane(renderer);
        canvasWrapper.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(canvasWrapper, Priority.ALWAYS);

        renderer.widthProperty().bind(canvasWrapper.widthProperty());
        renderer.heightProperty().bind(canvasWrapper.heightProperty());

        card.getChildren().addAll(label, canvasWrapper);
        return card;
    }

    public static void main(String[] args) { launch(args); }
}
