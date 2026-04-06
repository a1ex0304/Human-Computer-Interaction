import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
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
import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    // Width of number unit on the bar
    public static final int UNIT_WIDTH = 42;
    // Width of number unit on the bar
    public static final int PIPE_HEIGHT = 40;
    // Starting value on the number bars
    public static final int START_NUM = 0;
    // X and Y offsets to position the board
    public static final int OFFSET_X = 85;
    public static final int OFFSET_Y = 235;

    // Manages level data and switching levels
    public LevelManager levelManager = new LevelManager();
    // Allows other classes access the game
    public static BasicGameApp instance;
    // Holds the current level
    private Level currentLevel;
    // Header UI text
    private Text titleText;
    private Text levelText;
    private Text feedbackText;
    private Text goalText;
    // Button after level completed
    private Button nextLevelButton;
    // Overlay screens
    private StackPane winScreenOverlay;
    private StackPane warningOverlay;
    private Text equationText;

    public BasicGameApp() {
        instance = this;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        // window size and title
        settings.setWidth(1000);
        settings.setHeight(750);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
        // Load level
        loadLevel();
    }
    // Clears entities/UI, draws background and panels
    // Loads/reloads level
    // header text, number bars, slots, and available pipes
    private void loadLevel() {
        // Remove all previous entities and UI
        getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);
        getGameScene().clearUINodes();
        //Get current level
        currentLevel = levelManager.getCurrentLevel();
        // Set the background
        // Puts it behind everything
        entityBuilder()
                .at(0, 0)
                .view(texture("sewer_bg.png", getAppWidth(), getAppHeight()))
                .zIndex(-1000)
                .buildAndAttach();
        // Header panel
        Rectangle headerPanel = new Rectangle(720, 160);
        headerPanel.setArcWidth(30);
        headerPanel.setArcHeight(30);
        headerPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        headerPanel.setStroke(Color.rgb(255, 255, 255, 0.25));

        addUINode(headerPanel, 140, 35);

        // Main board panel
        Rectangle boardPanel = new Rectangle(900, 230);
        boardPanel.setArcWidth(30);
        boardPanel.setArcHeight(30);
        boardPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        boardPanel.setStroke(Color.rgb(255, 255, 255, 0.25));
        // Make panels ignore mouse
        headerPanel.setMouseTransparent(true);
        boardPanel.setMouseTransparent(true);

        addUINode(boardPanel, 50, 220);

        // Pipe tray panel
        Rectangle trayPanel = new Rectangle(900, 190);
        trayPanel.setArcWidth(30);
        trayPanel.setArcHeight(30);
        trayPanel.setFill(Color.rgb(0, 0, 0, 0.35));
        trayPanel.setStroke(Color.rgb(255, 255, 255, 0.25));
        trayPanel.setMouseTransparent(true);

        addUINode(trayPanel, 50, 500);

        // Label for the available pipe
        Text trayLabel = new Text("Available Pipes");
        trayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        trayLabel.setFill(Color.WHITE);
        trayLabel.setMouseTransparent(true);

        addUINode(trayLabel, 70, 530);

        // Get current level again
        currentLevel = levelManager.getCurrentLevel();

        // Header text
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

        // Equation text updates live
        equationText = new Text("");
        equationText.setFont(Font.font("Arial", 22));
        equationText.setFill(Color.WHITE);
        addUINode(equationText, 150, 160);

        // Feedback text
        feedbackText = new Text("");
        feedbackText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        feedbackText.setFill(Color.web("#d6f5ff"));
        addUINode(feedbackText, 0, 185);


        updateEquation();

        // Next level button
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
        // Draw the top bar for starting and ending value
        drawNumberBar(OFFSET_Y, currentLevel.getStartValue());
        drawNumberBar(OFFSET_Y + PIPE_HEIGHT*4, currentLevel.getTargetValue());
        // Create the 3 valid drop slots
        spawnSlot(OFFSET_Y + PIPE_HEIGHT*1, "TOP", "#000000");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT*2, "MIDDLE", "#444444");
        spawnSlot(OFFSET_Y + PIPE_HEIGHT*3, "BOTTOM", "#000000");

        // Available pipe options
        var options = currentLevel.getPipeOptions();

        // Layout
        double startX = 110;
        double startY = 550;
        double horizontalGap = 230;
        double verticalGap = 75;
        int pipesPerRow = 3;

        // grid-like tray layout
        for (int i = 0; i < options.size(); i++) {
            int col = i % pipesPerRow;
            int row = i / pipesPerRow;

            double x = startX + (col * horizontalGap);
            double y = startY + (row * verticalGap);

            spawnNumberPipe(options.get(i), x, y);
        }
    }

    // Draws a number bar
    // Highlights the goal number
    private void drawNumberBar(double y, int goal) {

        // Goal marker
        entityBuilder().at(OFFSET_X + UNIT_WIDTH * (goal - START_NUM) + NumberPipeView.PIPE_CAP_PADDING_X, y).view(new Rectangle(NumberPipeView.PIPE_CAP_WIDTH, PIPE_HEIGHT, NumberPipeView.PIPECOLOR_CAP)).buildAndAttach();
        // Main bar
        entityBuilder().at(OFFSET_X, y + NumberPipeView.PIPE_BODY_PADDING_Y).view(new Rectangle(UNIT_WIDTH*20, NumberPipeView.PIPE_BODY_THICKNESS, Color.web("#95a5a6"))).buildAndAttach();

        // Draw numbers 0–19
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

    // valid slot where a pipe can be dropped
    private void spawnSlot(double y, String id, String color) {
        Rectangle slotRect = new Rectangle(UNIT_WIDTH*20, 40, Color.web(color, 0.1));
        SlotComponent slot = new SlotComponent(id, slotRect);
        entityBuilder()
                .at(OFFSET_X, y)
                .view(slotRect)
                .with(slot)
                .buildAndAttach();
    }

    // draggable number pipe
    private void spawnNumberPipe(int val, double x, double y) {
        entityBuilder()
                .at(x, y)
                .view(new NumberPipeView())
                .with(new NumberPipeComponent(val))
                .buildAndAttach();
    }

    // Returns the X position to center a Text node
    private double centerX(Text text) {
        return (getAppWidth() - text.getLayoutBounds().getWidth()) / 2;
    }

    //snaps pipes so they stay connected properly
    //from top to bottom
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

    //Returns the X position current pipe chain ends
    public double getPipeEndX(String slotId) {
        var slot = getSlotById(slotId);
        if (slot == null || !slot.isFilled()) {
            return (slotId.equals("TOP")) ? getXForValue(currentLevel.getStartValue()) : getPipeEndX(getPrevId(slotId));
        }
        int val = slot.getPipe().getValue();
        double pipeX = slot.getPipe().getEntity().getX();
        return (val >= 0) ? pipeX + (val * UNIT_WIDTH) : pipeX;
    }

    // Converts a number value into its X coordinate
    public double getXForValue(int value) {
        return OFFSET_X + (value - START_NUM) * UNIT_WIDTH;
    }

    // Finds a slot component by its ID
    private SlotComponent getSlotById(String id) {
        return getGameWorld().getEntitiesByComponent(SlotComponent.class).stream()
                .map(e -> e.getComponent(SlotComponent.class))
                .filter(s -> s.getId().equals(id)).findFirst().orElse(null);
    }

    //Returns the previous slot
    private String getPrevId(String id) {
        return id.equals("BOTTOM") ? "MIDDLE" : "TOP";
    }

    // Updates the live equation
    // currently placed pipes
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

    //Updates the equation
    //user reached the target
    // win/warning/feedback
    public void applyOperation() {
        updateEquation();
        double finalX = getPipeEndX("BOTTOM");
        double targetX = getXForValue(currentLevel.getTargetValue());

        //whether all 3 slots used
        boolean allSlotsFilled = getSlotById("TOP").isFilled() &&
                getSlotById("MIDDLE").isFilled() &&
                getSlotById("BOTTOM").isFilled();

        // Check final pipe chain ends target value

        boolean reachedTarget = Math.abs(finalX - targetX) < 5;

        if (reachedTarget && allSlotsFilled) {
            showWinScreen();
        } else if (reachedTarget && !allSlotsFilled) {
            showWarningScreen();
        } else {
            int currentTotal = currentLevel.getStartValue();

            String[] order = {"TOP", "MIDDLE", "BOTTOM"};
            for (String id : order) {
                var slot = getSlotById(id);
                if (slot != null && slot.isFilled()) {
                    currentTotal += slot.getPipe().getValue();
                }
            }

            int diff = currentTotal - currentLevel.getTargetValue();

            //Feedback when the answer is incorrect
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
        }
    }

    // Shows the success overlay
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

    // Hides the success overlay
    //"Next Level" button
    private void hideWinScreen(boolean showNextButton) {
        if (winScreenOverlay != null) {
            removeUINode(winScreenOverlay);
            winScreenOverlay = null;
        }
        nextLevelButton.setVisible(showNextButton);
    }

    // Shows the warning overlay and when all 3 pipes are not used
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

    // Hides the warning overlay
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
