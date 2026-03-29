package com.example.demo1;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    public static final int UNIT_WIDTH = 42;
    public static final int START_NUM = 0;
    public static final int OFFSET_X = 65;

    public LevelManager levelManager = new LevelManager();
    public static BasicGameApp instance;
    private Level currentLevel;

    private Text feedbackText;
    private Text goalText;
    private Button nextLevelButton;

    public BasicGameApp() { instance = this; }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1000);
        settings.setHeight(750);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
        loadLevel();
    }

    private void loadLevel() {
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
        getGameScene().clearUINodes();

        getGameScene().setBackgroundColor(Color.web("#2c9cb4"));
        currentLevel = levelManager.getCurrentLevel();

        String goalStr = String.format("Level %d | Start: %d | Target: %d",
                currentLevel.getLevelNumber(),
                currentLevel.getStartValue(),
                currentLevel.getTargetValue());

        goalText = getUIFactoryService().newText(goalStr, Color.YELLOW, 28);
        addUINode(goalText, 50, 50);

        feedbackText = getUIFactoryService().newText("", Color.WHITE, 24);
        addUINode(feedbackText, 450, 50);

        nextLevelButton = new Button("Next Level");
        nextLevelButton.setVisible(false);
        nextLevelButton.setStyle("-fx-font-size: 18px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        nextLevelButton.setOnAction(e -> {
            if (levelManager.hasNextLevel()) {
                levelManager.nextLevel();
                loadLevel();
            }
        });
        addUINode(nextLevelButton, 820, 35);

        drawNumberBar(80);
        drawNumberBar(500);

        spawnSlot(180, "TOP");
        spawnSlot(280, "MIDDLE");
        spawnSlot(380, "BOTTOM");

        var options = currentLevel.getPipeOptions();
        for (int i = 0; i < options.size(); i++) {
            spawnNumberPipe(options.get(i), 100 + (i * 130), 650);
        }
    }

    private void drawNumberBar(double y) {
        entityBuilder().at(OFFSET_X - 25, y).view(new Rectangle(850, 45, Color.web("#95a5a6"))).buildAndAttach();
        for (int i = 0; i <= 19; i++) {
            Text num = new Text(String.valueOf(START_NUM + i));
            num.setFill(Color.WHITE);
            num.setFont(Font.font("Verdana", 14));
            num.setTranslateX(OFFSET_X + (i * UNIT_WIDTH) - 8);
            num.setTranslateY(y + 25);
            addUINode(num);
        }
    }

    private void spawnSlot(double y, String id) {
        entityBuilder()
                .at(OFFSET_X, y)
                .view(new Rectangle(840, 40, Color.web("white", 0.05)))
                .with(new SlotComponent(id))
                .buildAndAttach();
    }

    private void spawnNumberPipe(int val, double x, double y) {
        entityBuilder()
                .at(x, y)
                .view(new NumberPipeView())
                .with(new NumberPipeComponent(val))
                .buildAndAttach();
    }

    public void refreshAllPipes() {
        String[] order = {"TOP", "MIDDLE", "BOTTOM"};
        for (String id : order) {
            var slot = getSlotById(id);
            if (slot != null && slot.isFilled()) {
                double startX = (id.equals("TOP")) ? getXForValue(currentLevel.getStartValue()) : getPipeEndX(getPrevId(id));
                slot.getPipe().snapTo(startX);
            }
        }
    }

    public double getPipeEndX(String slotId) {
        var slot = getSlotById(slotId);
        if (slot == null || !slot.isFilled()) {
            return (slotId.equals("TOP")) ? getXForValue(currentLevel.getStartValue()) : getPipeEndX(getPrevId(slotId));
        }
        int val = slot.getPipe().getValue();
        double pipeX = slot.getPipe().getEntity().getX();
        return (val >= 0) ? pipeX + (val * UNIT_WIDTH) : pipeX;
    }

    public double getXForValue(int value) {
        return OFFSET_X + (value - START_NUM) * UNIT_WIDTH;
    }

    private SlotComponent getSlotById(String id) {
        return getGameWorld().getEntitiesByComponent(SlotComponent.class).stream()
                .map(e -> e.getComponent(SlotComponent.class))
                .filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    private String getPrevId(String id) {
        return id.equals("BOTTOM") ? "MIDDLE" : "TOP";
    }

    // --- UPDATED METHOD ---
    public void applyOperation() {
        double finalX = getPipeEndX("BOTTOM");
        double targetX = getXForValue(currentLevel.getTargetValue());

        // Check if all three slots are filled
        boolean allSlotsFilled = getSlotById("TOP").isFilled() &&
                getSlotById("MIDDLE").isFilled() &&
                getSlotById("BOTTOM").isFilled();

        boolean reachedTarget = Math.abs(finalX - targetX) < 5;

        if (reachedTarget && allSlotsFilled) {
            feedbackText.setText("Correct! 3 Pipes Used.");
            feedbackText.setFill(Color.LIGHTGREEN);

            if (levelManager.hasNextLevel()) {
                nextLevelButton.setVisible(true);
            } else {
                feedbackText.setText("Game Complete!");
            }
        } else if (reachedTarget && !allSlotsFilled) {
            feedbackText.setText("Target hit! But you must use 3 pipes.");
            feedbackText.setFill(Color.ORANGE);
            nextLevelButton.setVisible(false);
        } else {
            feedbackText.setText("");
            nextLevelButton.setVisible(false);
        }
    }

    public static void main(String[] args) { launch(args); }
}
