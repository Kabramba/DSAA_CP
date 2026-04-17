package com.simulator.algorithm;

import com.simulator.model.GraphMaze;
import com.simulator.model.Path;
import com.simulator.model.PathCursor;

public interface PathfindingAlgorithm {
    boolean executeNextStep(PathCursor virtualCursor, GraphMaze interpretMap);
    Path getResultPath();
}