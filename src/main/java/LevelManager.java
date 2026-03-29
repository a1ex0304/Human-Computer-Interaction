package com.example.demo1;
import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private final List<Level> levels = new ArrayList<>();
    private int currentLevelIndex = 0;

    public LevelManager() {
        // Restoring the 5 original levels from your source
        levels.add(new Level(1, 5, 12, List.of(2, 11, 9, 5, 1)));
        levels.add(new Level(2, 4, 10, List.of(3, -1, 8, 10, 5)));
        levels.add(new Level(3, 6, 13, List.of(2, -2, 5, 9, 4)));
        levels.add(new Level(4, 3, 11, List.of(4, -1, 2, 9, 3)));
        levels.add(new Level(5, 8, 15, List.of(3, -2, 1, 9, 4)));
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    public void nextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
    }
}
