package com.simulator.algorithm;
import com.simulator.model.Mouse;

public interface ExplorationAlgorithm {
    /**
     * Governs SLAM kinematics. The algorithm acts upon the physical Mouse entity.
     */
    boolean executeNextStep(Mouse mouse);
}