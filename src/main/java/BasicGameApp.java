//package com.example.demo1;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.ui.FontType;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    public static final int UNIT_WIDTH = 42;
    public static final int PIPE_HEIGHT = 40;
    public static final int START_NUM = 0;
    public static final int OFFSET_X = 65;
    public static final int OFFSET_Y = 110;

    public LevelManager levelManager = new LevelManager();
    public static BasicGameApp instance;
    private Level currentLevel;

    private Text feedbackText;
    private Text goalText;
    private Button nextLevelButton;
    private StackPane winScreenOverlay;
    private StackPane warningOverlay;
    private Text equationText;

    public BasicGameApp() {
        instance = this;
    }

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

        equationText = getUIFactoryService().newText("", Color.WHITE, 26);
        addUINode(equationText, 50, 85);
        updateEquation();

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

        drawNumberBar(OFFSET_Y, currentLevel.getStartValue());
        drawNumberBar(OFFSET_Y + PIPE_HEIGHT*4, currentLevel.getTargetValue());

        spawnSlot(OFFSET_Y + PIPE_HEIGHT*1, "TOP", "#000000");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT*2, "MIDDLE", "#444444");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT*3, "BOTTOM", "#000000");

        var options = currentLevel.getPipeOptions();
        for (int i = 0; i < options.size(); i++) {
            spawnNumberPipe(options.get(i), 100, OFFSET_Y + PIPE_HEIGHT * 6 + (i * 50));  
        }
    }

    private void drawNumberBar(double y, int goal) {
        entityBuilder().at(OFFSET_X + UNIT_WIDTH * (goal - START_NUM) + NumberPipeView.PIPE_CAP_PADDING_X, y).view(new Rectangle(NumberPipeView.PIPE_CAP_WIDTH, PIPE_HEIGHT, NumberPipeView.PIPECOLOR_CAP)).buildAndAttach();
        entityBuilder().at(OFFSET_X, y + NumberPipeView.PIPE_BODY_PADDING_Y).view(new Rectangle(UNIT_WIDTH*20, NumberPipeView.PIPE_BODY_THICKNESS, Color.web("#95a5a6"))).buildAndAttach();
        for (int i = 0; i < 20; i++) {
            int displayValue = i + START_NUM;
            boolean isGoal = displayValue == goal;
            Text num = new Text(String.valueOf(displayValue));
            if (isGoal) {
                num.setFill(Color.YELLOW);
                num.setFont(Font.font("Cambia", FontWeight.EXTRA_BOLD, 16));
            } else {
                num.setFill(Color.WHITE);
                num.setFont(Font.font("Cambia", 14));
            }
            num.setTranslateX(OFFSET_X + (i * UNIT_WIDTH) + UNIT_WIDTH/2 - (i >= 10 ? 10 : 5));
            num.setTranslateY(y + 25);
            addUINode(num);
        }
    }

    private void spawnSlot(double y, String id, String color) {
        Rectangle slotRect = new Rectangle(UNIT_WIDTH*20, 40, Color.web(color, 0.1));
        SlotComponent slot = new SlotComponent(id, slotRect);
        entityBuilder()
                .at(OFFSET_X, y)
                .view(slotRect)
                .with(slot)
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

    public void updateEquation() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentLevel.getStartValue());

        String[] order = {"TOP", "MIDDLE", "BOTTOM"};
        int runningTotal = currentLevel.getStartValue();

        for (String id : order) {
            var slot = getSlotById(id);
            if (slot != null && slot.isFilled()) {
                int val = slot.getPipe().getValue();
                runningTotal += val;
                if (val >= 0) {
                    sb.append(" + ").append(val);
                } else {
                    sb.append(" - ").append(Math.abs(val));
                }
            }
        }
        sb.append(" = ").append(runningTotal);

        equationText.setText(sb.toString());
    }

    // --- UPDATED METHOD ---
    public void applyOperation() {
        updateEquation();
        double finalX = getPipeEndX("BOTTOM");
        double targetX = getXForValue(currentLevel.getTargetValue());

        // Check if all three slots are filled
        boolean allSlotsFilled = getSlotById("TOP").isFilled() &&
                getSlotById("MIDDLE").isFilled() &&
                getSlotById("BOTTOM").isFilled();

        boolean reachedTarget = Math.abs(finalX - targetX) < 5;

        if (reachedTarget && allSlotsFilled) {
            showWinScreen();
        } else if (reachedTarget && !allSlotsFilled) {
            showWarningScreen();
        } else {
            feedbackText.setText("");
            nextLevelButton.setVisible(false);
        }
    }

    private void showWinScreen() {
        feedbackText.setText("");

        Rectangle overlay = new Rectangle(1000, 750, Color.rgb(0, 0, 0, 0.7));

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 20; -fx-border-color: #27ae60; -fx-border-width: 3; -fx-border-radius: 20;");
        content.setMaxWidth(500);
        content.setMaxHeight(350);

        Text title = new Text("Level Complete!");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 36));
        title.setFill(Color.web("#27ae60"));

        Text subtitle = new Text("You solved Level " + currentLevel.getLevelNumber());
        subtitle.setFont(Font.font("Verdana", 20));
        subtitle.setFill(Color.LIGHTGRAY);

        HBox buttonBox = new HBox(30);
        buttonBox.setAlignment(Pos.CENTER);

        Button backBtn = new Button("Back");
        backBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 30 12 30; -fx-background-radius: 10;");
        backBtn.setOnAction(e -> hideWinScreen(true));

        Button nextBtn = new Button("Next Level");
        nextBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 30 12 30; -fx-background-radius: 10;");
        nextBtn.setOnAction(e -> {
            if (levelManager.hasNextLevel()) {
                hideWinScreen(false);
                levelManager.nextLevel();
                loadLevel();
            }
        });

        buttonBox.getChildren().addAll(backBtn, nextBtn);
        content.getChildren().addAll(title, subtitle, buttonBox);

        winScreenOverlay = new StackPane(overlay, content);
        addUINode(winScreenOverlay, 0, 0);
    }

    private void hideWinScreen(boolean showNextButton) {
        if (winScreenOverlay != null) {
            removeUINode(winScreenOverlay);
            winScreenOverlay = null;
        }
        nextLevelButton.setVisible(showNextButton);
    }

    private void showWarningScreen() {
        feedbackText.setText("");

        Rectangle overlay = new Rectangle(1000, 750, Color.rgb(0, 0, 0, 0.7));

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 20; -fx-border-color: #e67e22; -fx-border-width: 3; -fx-border-radius: 20;");
        content.setMaxWidth(500);
        content.setMaxHeight(350);

        Text title = new Text("Almost!");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 36));
        title.setFill(Color.web("#e67e22"));

        Text subtitle = new Text("You hit the target, but you must use all 3 pipes.");
        subtitle.setFont(Font.font("Verdana", 18));
        subtitle.setFill(Color.LIGHTGRAY);

        Button okayBtn = new Button("Okay");
        okayBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 12 40 12 40; -fx-background-radius: 10;");
        okayBtn.setOnAction(e -> hideWarningScreen());

        content.getChildren().addAll(title, subtitle, okayBtn);

        warningOverlay = new StackPane(overlay, content);
        addUINode(warningOverlay, 0, 0);
    }

    private void hideWarningScreen() {
        if (warningOverlay != null) {
            removeUINode(warningOverlay);
            warningOverlay = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
