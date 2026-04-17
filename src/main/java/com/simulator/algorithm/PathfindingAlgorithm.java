package com.simulator.algorithm;
import com.simulator.model.GraphMaze;
import com.simulator.model.PathCursor;

public interface PathfindingAlgorithm {
    /**
     * Governs offline graph traversal. The algorithm only receives the virtual cursor
     * and the localized memory graph (Interpret_Map).
     */
    boolean executeNextStep(PathCursor virtualCursor, GraphMaze interpretMap);
}