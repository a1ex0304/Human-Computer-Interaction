package com.example.demo1;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
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
    public static final int START_NUM = 0;
    public static final int OFFSET_X = 65;

    public LevelManager levelManager = new LevelManager();
    public static BasicGameApp instance;
    private Level currentLevel;

    private Text feedbackText;
    private Text goalText;
    private Button nextLevelButton;
    private StackPane winScreenOverlay;
    private StackPane warningOverlay;

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
