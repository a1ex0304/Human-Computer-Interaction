import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    public static final int UNIT_WIDTH = 42;
    public static final int PIPE_HEIGHT = 40;
    public static final int START_NUM = 0;
    public static final int OFFSET_X = 85;
    public static final int OFFSET_Y = 235;

    public LevelManager levelManager = new LevelManager();
    public static BasicGameApp instance;

    private Level currentLevel;
    private Text titleText;
    private Text levelText;
    private Text feedbackText;
    private Text goalText;
    private Text equationText;

    private Button nextLevelButton;
    private Button undoButton;
    private Button redoButton;

    private StackPane winScreenOverlay;
    private StackPane warningOverlay;

    private final Deque<GameState> undoStack = new ArrayDeque<>();
    private final Deque<GameState> redoStack = new ArrayDeque<>();
    private boolean restoringState = false;

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
        undoStack.clear();
        redoStack.clear();
        restoringState = false;

        currentLevel = levelManager.getCurrentLevel();

        entityBuilder()
                .at(0, 0)
                .view(texture("sewer_bg.png", getAppWidth(), getAppHeight()))
                .zIndex(-1000)
                .buildAndAttach();

        Rectangle headerPanel = new Rectangle(720, 160);
        headerPanel.setArcWidth(30);
        headerPanel.setArcHeight(30);
        headerPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        headerPanel.setStroke(Color.rgb(255, 255, 255, 0.25));
        headerPanel.setMouseTransparent(true);
        addUINode(headerPanel, 140, 35);

        Rectangle boardPanel = new Rectangle(900, 230);
        boardPanel.setArcWidth(30);
        boardPanel.setArcHeight(30);
        boardPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        boardPanel.setStroke(Color.rgb(255, 255, 255, 0.25));
        boardPanel.setMouseTransparent(true);
        addUINode(boardPanel, 50, 220);

        Rectangle trayPanel = new Rectangle(900, 190);
        trayPanel.setArcWidth(30);
        trayPanel.setArcHeight(30);
        trayPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        trayPanel.setStroke(Color.rgb(255, 255, 255, 0.25));
        trayPanel.setMouseTransparent(true);
        addUINode(trayPanel, 50, 500);

        Text trayLabel = new Text("Available Pipes");
        trayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        trayLabel.setFill(Color.WHITE);
        trayLabel.setMouseTransparent(true);
        addUINode(trayLabel, 70, 530);

        titleText = new Text("EquationFlow");
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        titleText.setFill(Color.WHITE);
        addUINode(titleText, centerX(titleText), 70);

        levelText = new Text("Level " + currentLevel.getLevelNumber());
        levelText.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 22));
        levelText.setFill(Color.web("#ffe66d"));
        addUINode(levelText, centerX(levelText), 100);

        goalText = new Text("Start: " + currentLevel.getStartValue() + "    Target: " + currentLevel.getTargetValue());
        goalText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        goalText.setFill(Color.WHITE);
        addUINode(goalText, centerX(goalText), 130);

        equationText = new Text("");
        equationText.setFont(Font.font("Arial", 22));
        equationText.setFill(Color.WHITE);
        addUINode(equationText, 150, 160);

        feedbackText = new Text("");
        feedbackText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        feedbackText.setFill(Color.web("#d6f5ff"));
        addUINode(feedbackText, 0, 185);

        createTopRightButtons();

        drawNumberBar(OFFSET_Y, currentLevel.getStartValue());
        drawNumberBar(OFFSET_Y + PIPE_HEIGHT * 4, currentLevel.getTargetValue());

        spawnSlot(OFFSET_Y + PIPE_HEIGHT, "TOP", "#000000");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT * 2, "MIDDLE", "#444444");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT * 3, "BOTTOM", "#000000");

        var options = currentLevel.getPipeOptions();
        double startX = 110;
        double startY = 550;
        double horizontalGap = 230;
        double verticalGap = 75;
        int pipesPerRow = 3;

        for (int i = 0; i < options.size(); i++) {
            int col = i % pipesPerRow;
            int row = i / pipesPerRow;
            double x = startX + (col * horizontalGap);
            double y = startY + (row * verticalGap);
            spawnNumberPipe(options.get(i), x, y);
        }

        updateEquation();
        applyOperation();
        rememberInitialState();
    }

    private void createTopRightButtons() {
        String glassButtonStyle = "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-color: rgba(44, 62, 80, 0.92);"
                + "-fx-text-fill: white;"
                + "-fx-border-color: rgba(255,255,255,0.28);"
                + "-fx-border-width: 1.2;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-padding: 10 18 10 18;"
                + "-fx-cursor: hand;";

        undoButton = new Button("↶ Undo");
        undoButton.setStyle(glassButtonStyle);
        undoButton.setOnAction(e -> undo());

        redoButton = new Button("↷ Redo");
        redoButton.setStyle(glassButtonStyle);
        redoButton.setOnAction(e -> redo());

        nextLevelButton = new Button("Next Level");
        nextLevelButton.setVisible(false);
        nextLevelButton.setStyle("-fx-font-size: 17px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-color: #27ae60;"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-padding: 10 18 10 18;"
                + "-fx-cursor: hand;");
        nextLevelButton.setOnAction(e -> {
            if (levelManager.hasNextLevel()) {
                levelManager.nextLevel();
                loadLevel();
            }
        });

        HBox buttonRow = new HBox(12, undoButton, redoButton, nextLevelButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        addUINode(buttonRow, 610, 55);
        updateUndoRedoButtons();
    }

    private void drawNumberBar(double y, int goal) {
        entityBuilder()
                .at(OFFSET_X + UNIT_WIDTH * (goal - START_NUM) + NumberPipeView.PIPE_CAP_PADDING_X, y)
                .view(new Rectangle(NumberPipeView.PIPE_CAP_WIDTH, PIPE_HEIGHT, NumberPipeView.PIPECOLOR_CAP))
                .buildAndAttach();

        entityBuilder()
                .at(OFFSET_X, y + NumberPipeView.PIPE_BODY_PADDING_Y)
                .view(new Rectangle(UNIT_WIDTH * 20, NumberPipeView.PIPE_BODY_THICKNESS, Color.web("#95a5a6")))
                .buildAndAttach();

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
            num.setTranslateX(OFFSET_X + (i * UNIT_WIDTH) + UNIT_WIDTH / 2 - (i >= 10 ? 10 : 5));
            num.setTranslateY(y + 25);
            addUINode(num);
        }
    }

    private void spawnSlot(double y, String id, String color) {
        Rectangle slotRect = new Rectangle(UNIT_WIDTH * 20, 40, Color.web(color, 0.1));
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

    private double centerX(Text text) {
        return (getAppWidth() - text.getLayoutBounds().getWidth()) / 2;
    }

    public void refreshAllPipes() {
        String[] order = {"TOP", "MIDDLE", "BOTTOM"};
        for (String id : order) {
            var slot = getSlotById(id);
            if (slot != null && slot.isFilled()) {
                double startX = id.equals("TOP")
                        ? getXForValue(currentLevel.getStartValue())
                        : getPipeEndX(getPrevId(id));
                slot.getPipe().snapTo(startX);
            }
        }
    }

    public double getPipeEndX(String slotId) {
        var slot = getSlotById(slotId);
        if (slot == null || !slot.isFilled()) {
            return slotId.equals("TOP") ? getXForValue(currentLevel.getStartValue()) : getPipeEndX(getPrevId(slotId));
        }
        int val = slot.getPipe().getValue();
        double pipeX = slot.getPipe().getEntity().getX();
        return val >= 0 ? pipeX + (val * UNIT_WIDTH) : pipeX;
    }

    public double getXForValue(int value) {
        return OFFSET_X + (value - START_NUM) * UNIT_WIDTH;
    }

    public SlotComponent getSlotById(String id) {
        return getGameWorld().getEntitiesByComponent(SlotComponent.class).stream()
                .map(e -> e.getComponent(SlotComponent.class))
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
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
        equationText.setTranslateX(centerX(equationText));
    }

    public void applyOperation() {
        hideWinScreen(false);
        hideWarningScreen();

        updateEquation();
        double finalX = getPipeEndX("BOTTOM");
        double targetX = getXForValue(currentLevel.getTargetValue());

        boolean allSlotsFilled = getSlotById("TOP").isFilled()
                && getSlotById("MIDDLE").isFilled()
                && getSlotById("BOTTOM").isFilled();

        boolean reachedTarget = Math.abs(finalX - targetX) < 5;

        if (reachedTarget && allSlotsFilled) {
            showWinScreen();
            return;
        }

        if (reachedTarget) {
            showWarningScreen();
            return;
        }

        int currentTotal = currentLevel.getStartValue();
        String[] order = {"TOP", "MIDDLE", "BOTTOM"};
        for (String id : order) {
            var slot = getSlotById(id);
            if (slot != null && slot.isFilled()) {
                currentTotal += slot.getPipe().getValue();
            }
        }

        int diff = currentTotal - currentLevel.getTargetValue();
        if (diff > 0) {
            feedbackText.setText("Too high by " + diff);
            feedbackText.setFill(Color.web("#ff8a8a"));
        } else if (diff < 0) {
            feedbackText.setText("Too low by " + Math.abs(diff));
            feedbackText.setFill(Color.web("#9ad6ff"));
        } else {
            feedbackText.setText("");
        }

        feedbackText.setX(centerX(feedbackText));
        nextLevelButton.setVisible(false);
        updateUndoRedoButtons();
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
        if (nextLevelButton != null) {
            nextLevelButton.setVisible(showNextButton);
        }
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

    public void rememberInitialState() {
        undoStack.clear();
        redoStack.clear();
        undoStack.push(captureCurrentState());
        updateUndoRedoButtons();
    }

    public void rememberStateForUndo() {
        if (restoringState) {
            return;
        }

        GameState currentSnapshot = captureCurrentState();
        if (undoStack.isEmpty() || !currentSnapshot.sameAs(undoStack.peek())) {
            undoStack.push(currentSnapshot);
        }
        redoStack.clear();
        updateUndoRedoButtons();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }

        GameState currentSnapshot = captureCurrentState();
        GameState previousSnapshot = undoStack.pop();

        if (!currentSnapshot.sameAs(previousSnapshot)) {
            redoStack.push(currentSnapshot);
            restoreState(previousSnapshot);
        }

        updateUndoRedoButtons();
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }

        GameState currentSnapshot = captureCurrentState();
        GameState redoSnapshot = redoStack.pop();

        if (!currentSnapshot.sameAs(redoSnapshot)) {
            undoStack.push(currentSnapshot);
            restoreState(redoSnapshot);
        }

        updateUndoRedoButtons();
    }

    private void restoreState(GameState state) {
        restoringState = true;

        hideWinScreen(false);
        hideWarningScreen();
        feedbackText.setText("");
        nextLevelButton.setVisible(false);

        for (var slotEntity : getGameWorld().getEntitiesByComponent(SlotComponent.class)) {
            slotEntity.getComponent(SlotComponent.class).setPipe(null);
        }

        List<NumberPipeComponent> pipes = getAllPipes();
        for (int i = 0; i < pipes.size(); i++) {
            PipeSnapshot pipeState = state.pipes.get(i);
            NumberPipeComponent pipe = pipes.get(i);
            SlotComponent restoredSlot = pipeState.slotId == null ? null : getSlotById(pipeState.slotId);
            pipe.restoreState(pipeState.sign, pipeState.x, pipeState.y, restoredSlot);
        }

        refreshAllPipes();
        restoringState = false;
        applyOperation();
    }

    private GameState captureCurrentState() {
        List<PipeSnapshot> pipeSnapshots = new ArrayList<>();
        for (NumberPipeComponent pipe : getAllPipes()) {
            SlotComponent slot = pipe.getCurrentSlot();
            pipeSnapshots.add(new PipeSnapshot(
                    pipe.getSign(),
                    pipe.getEntity().getX(),
                    pipe.getEntity().getY(),
                    slot == null ? null : slot.getId()
            ));
        }
        return new GameState(pipeSnapshots);
    }

    private List<NumberPipeComponent> getAllPipes() {
        return getGameWorld().getEntitiesByComponent(NumberPipeComponent.class).stream()
                .map(e -> e.getComponent(NumberPipeComponent.class))
                .sorted(Comparator
                        .comparingDouble(NumberPipeComponent::getHomeY)
                        .thenComparingDouble(NumberPipeComponent::getHomeX))
                .toList();
    }

    private void updateUndoRedoButtons() {
        if (undoButton != null) {
            boolean canUndo = !undoStack.isEmpty() && !captureCurrentState().sameAs(undoStack.peek());
            undoButton.setDisable(!canUndo);
            undoButton.setOpacity(canUndo ? 1.0 : 0.45);
        }

        if (redoButton != null) {
            boolean canRedo = !redoStack.isEmpty();
            redoButton.setDisable(!canRedo);
            redoButton.setOpacity(canRedo ? 1.0 : 0.45);
        }
    }

    private static class PipeSnapshot {
        private final int sign;
        private final double x;
        private final double y;
        private final String slotId;

        private PipeSnapshot(int sign, double x, double y, String slotId) {
            this.sign = sign;
            this.x = x;
            this.y = y;
            this.slotId = slotId;
        }

        private boolean sameAs(PipeSnapshot other) {
            if (other == null) {
                return false;
            }
            return sign == other.sign
                    && Math.abs(x - other.x) < 0.01
                    && Math.abs(y - other.y) < 0.01
                    && ((slotId == null && other.slotId == null) || (slotId != null && slotId.equals(other.slotId)));
        }
    }

    private static class GameState {
        private final List<PipeSnapshot> pipes;

        private GameState(List<PipeSnapshot> pipes) {
            this.pipes = pipes;
        }

        private boolean sameAs(GameState other) {
            if (other == null || pipes.size() != other.pipes.size()) {
                return false;
            }
            for (int i = 0; i < pipes.size(); i++) {
                if (!pipes.get(i).sameAs(other.pipes.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
