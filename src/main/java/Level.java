package com.example.demo1;

import java.util.List;

public class Level {
    private final int levelNumber;
    private final int startValue;
    private final int targetValue;
    private final List<Integer> pipeOptions;

    public Level(int levelNumber, int startValue, int targetValue, List<Integer> pipeOptions) {
        this.levelNumber = levelNumber;
        this.startValue = startValue;
        this.targetValue = targetValue;
        this.pipeOptions = pipeOptions;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getStartValue() {
        return startValue;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public List<Integer> getPipeOptions() {
        return pipeOptions;
    }
}