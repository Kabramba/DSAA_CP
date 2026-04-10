package com.simulator;

import com.simulator.io.MazeFileManager;
import com.simulator.io.MazeGenerator;
import com.simulator.model.GraphMaze;
import com.simulator.view.MazeRenderer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GraphMaze maze;

        try {
            // 1. Generate a massive, random 12x9 maze and save it to the disk
            MazeGenerator.generateAndSave(12, 9, "massive_maze.mms");

            // 2. Read that exact file back into the engine
            maze = MazeFileManager.loadMaze("massive_maze.mms");

            System.out.println("Successfully loaded maze with " + maze.getAllNodes().size() + " nodes.");
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: File operations failed.");
            e.printStackTrace();
            return;
        }

        // Initialize the rendering pipeline
        MazeRenderer renderer = new MazeRenderer(maze, 800, 600);
        StackPane root = new StackPane();
        root.getChildren().add(renderer);

        // Construct the primary window
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Graph-Based Micromouse Simulator");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Bootstraps the JavaFX application thread
        launch(args);
    }
}