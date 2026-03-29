package com.example.demo1;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.onKeyDown;

public class BasicGameApp extends GameApplication {

    private final LevelManager levelManager = new LevelManager();
    private Level currentLevel;

    private int startValue;
    private int targetValue;
    private int currentValue;

    private boolean gameWon = false;

    private UIManager uiManager;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
        uiManager = new UIManager(this);

        currentLevel = levelManager.getCurrentLevel();
        loadLevel(currentLevel);

        uiManager.build(currentLevel, currentValue);
        uiManager.showPlaying(currentLevel, currentValue);
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.DIGIT1, () -> {
            if (currentLevel.getPipeOptions().size() >= 1) {
                applyOperation(currentLevel.getPipeOptions().get(0));
            }
        });

        onKeyDown(KeyCode.DIGIT2, () -> {
            if (currentLevel.getPipeOptions().size() >= 2) {
                applyOperation(currentLevel.getPipeOptions().get(1));
            }
        });

        onKeyDown(KeyCode.DIGIT3, () -> {
            if (currentLevel.getPipeOptions().size() >= 3) {
                applyOperation(currentLevel.getPipeOptions().get(2));
            }
        });

        onKeyDown(KeyCode.R, this::resetGame);
    }

    private void loadLevel(Level level) {
        currentLevel = level;
        startValue = level.getStartValue();
        targetValue = level.getTargetValue();
        currentValue = startValue;
        gameWon = false;
    }

    public void applyOperation(int value) {
        if (gameWon) {
            return;
        }

        currentValue += value;
        uiManager.update(currentLevel, currentValue);
        checkGameState();
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void resetGame() {
        loadLevel(levelManager.getCurrentLevel());
        uiManager.build(currentLevel, currentValue);
        uiManager.showPlaying(currentLevel, currentValue);
    }

    private void checkGameState() {
        if (currentValue == targetValue) {
            gameWon = true;

            if (levelManager.hasNextLevel()) {
                Level nextLevel = levelManager.loadNextLevel();
                loadLevel(nextLevel);
                uiManager.build(currentLevel, currentValue);
                uiManager.showLevelComplete(nextLevel.getLevelNumber() - 1, nextLevel.getLevelNumber());
                uiManager.showPlaying(currentLevel, currentValue);
            } else {
                uiManager.showFinalWin();
            }

        } else if (currentValue > targetValue) {
            uiManager.showTooHigh(currentLevel, currentValue);
        } else {
            uiManager.showPlaying(currentLevel, currentValue);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
