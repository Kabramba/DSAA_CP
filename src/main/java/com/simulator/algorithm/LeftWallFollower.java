package com.simulator.algorithm;

import com.simulator.model.Mouse;

public class LeftWallFollower implements ExplorationAlgorithm {
    // 0: North, 1: East, 2: South, 3: West
    private int currentHeading = 0;

    @Override
    public boolean executeNextStep(Mouse mouse) {
        if (mouse.getTargetCell() != null && mouse.getKnownCurrentCell() == mouse.getTargetCell()) {
            return true; // Terminal state reached
        }

        // Left wall following logic: prioritize turning left, then going straight, then right, then U-turn
        int[] headingPreferences = {
                (currentHeading + 3) % 4, // Left
                currentHeading,           // Straight
                (currentHeading + 1) % 4, // Right
                (currentHeading + 2) % 4  // U-Turn
        };

        for (int heading : headingPreferences) {
            if (attemptMove(mouse, heading)) {
                currentHeading = heading;
                return false;
            }
        }
        return false;
    }

    private boolean attemptMove(Mouse mouse, int heading) {
        switch (heading) {
            case 0:
                if (!mouse.getKnownCurrentCell().hasNorthWall() && mouse.getKnownCurrentCell().northNode != null) {
                    mouse.moveNorth(); return true;
                } break;
            case 1:
                if (!mouse.getKnownCurrentCell().hasEastWall() && mouse.getKnownCurrentCell().eastNode != null) {
                    mouse.moveEast(); return true;
                } break;
            case 2:
                if (!mouse.getKnownCurrentCell().hasSouthWall() && mouse.getKnownCurrentCell().southNode != null) {
                    mouse.moveSouth(); return true;
                } break;
            case 3:
                if (!mouse.getKnownCurrentCell().hasWestWall() && mouse.getKnownCurrentCell().westNode != null) {
                    mouse.moveWest(); return true;
                } break;
        }
        return false;
    }
}