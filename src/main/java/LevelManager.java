//package com.example.demo1;
import java.util.ArrayList;
import java.util.List;

// Manages game levels and progression
public class LevelManager {
    private final List<Level> levels = new ArrayList<>();
    private int currentLevelIndex = 0;

    public LevelManager() {
        // Create 5 levels with start value, target value, and pipe options
        levels.add(new Level(1, 5, 12, List.of(1, 2, 5, 9, 11)));
        levels.add(new Level(2, 4, 10, List.of(-1, 3, 5, 8, 10)));
        levels.add(new Level(3, 6, 13, List.of(-2, 2, 4, 5, 9)));
        levels.add(new Level(4, 3, 11, List.of(-1, 2, 3, 4, 9)));
        levels.add(new Level(5, 8, 15, List.of(1, -2, 3, 4, 9)));
    }

    // Returns the current level
    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    // Check if there are more levels
    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    // Advance to next level
    public void nextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
    }
}
