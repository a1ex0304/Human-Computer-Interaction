package com.example.demo1;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    private int startValue;
    private int targetValue;
    private int currentValue;
    private boolean gameWon = false;

    private UIManager ui;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
        do {
            startValue = (int)(Math.random() * 15) + 1;
            targetValue = (int)(Math.random() * 15) + 1;
        } while (startValue == targetValue);

        currentValue = startValue;
        ui = new UIManager(this);
        ui.build(startValue, targetValue, currentValue);
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.DIGIT1, () -> applyOperation(2));
        onKeyDown(KeyCode.DIGIT2, () -> applyOperation(-1));
        onKeyDown(KeyCode.R, this::resetGame);
    }

    public void applyOperation(int value) {
        if (gameWon) return;
        currentValue += value;
        ui.update(currentValue, targetValue);
        checkGameState();
    }

    public void resetGame() {
        currentValue = startValue;
        gameWon = false;
        ui.update(currentValue, targetValue);
        ui.showPlaying(startValue, targetValue);
    }

    private void checkGameState() {
        if (currentValue == targetValue) {
            gameWon = true;
            ui.showWin();
        } else if (currentValue > targetValue) {
            ui.showTooHigh();
        } else {
            ui.showPlaying(startValue, targetValue);
        }
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
