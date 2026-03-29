import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGL.*;

import java.util.List;

public class BasicGameApp extends GameApplication {

    public LevelManager levelManager = new LevelManager();
    private Level currentLevel;

    public static BasicGameApp instance;

    private int startValue;
    private int targetValue;
    private int currentValue;

    private boolean gameWon = false;

    private Text currentText;
    private Text messageText;
    private Entity slot1;
    private Entity slot2;
    private Entity slot3;

    public BasicGameApp(){
        instance = this;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(800);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
        currentLevel = levelManager.getCurrentLevel();
        loadLevel(currentLevel);
    
        Text title = new Text("EquationFlow");
        title.setTranslateX(300);
        title.setTranslateY(50);
        title.setStyle("-fx-font-size: 30px; -fx-fill: black;");

        Text startText = new Text("Start Value: " + startValue);
        startText.setTranslateX(100);
        startText.setTranslateY(150);
        startText.setStyle("-fx-font-size: 24px; -fx-fill: black;");

        Text targetText = new Text("Target Value: " + targetValue);
        targetText.setTranslateX(100);
        targetText.setTranslateY(200);
        targetText.setStyle("-fx-font-size: 24px; -fx-fill: black;");

        currentText = new Text("Current Value: " + currentValue);
        currentText.setTranslateX(100);
        currentText.setTranslateY(250);
        currentText.setStyle("-fx-font-size: 24px; -fx-fill: black;");

        messageText = new Text("");
        messageText.setTranslateX(100);
        messageText.setTranslateY(320);
        messageText.setStyle("-fx-font-size: 24px; -fx-fill: green;");

        slot1 = FXGL.entityBuilder()
                .at(150, 250)
                .view(new Rectangle(480, 30, Color.LIGHTGRAY))
                .with(new SlotComponent("TOP"))
                .buildAndAttach();

        slot2 = FXGL.entityBuilder()
                .at(150, 280)
                .view(new Rectangle(480, 30, Color.GRAY))
                .with(new SlotComponent("MIDDLE"))
                .buildAndAttach();

        slot3 = FXGL.entityBuilder()
                .at(150, 310)
                .view(new Rectangle(480, 30, Color.DARKGRAY))
                .with(new SlotComponent("BOTTOM"))
                .buildAndAttach();
        
        var pipeOptions = currentLevel.getPipeOptions();
        for (int i = 0; i < pipeOptions.size(); i++) {
            spawnNumberPipe(pipeOptions.get(i), 150, 400 + i*50);
        }

        var resetButton = getUIFactoryService().newButton("Reset");
        resetButton.setTranslateX(650);
        resetButton.setTranslateY(450);
        resetButton.setPrefWidth(140);
        resetButton.setPrefHeight(60);
        resetButton.setStyle("-fx-font-size: 16px; -fx-background-radius: 10;");
        resetButton.setOnAction(e -> resetGame());
        
        addUINode(title);
        addUINode(startText);
        addUINode(targetText);
        addUINode(currentText);
        addUINode(messageText);
        addUINode(resetButton);
    }

    private void spawnNumberPipe(int value, double x, double y) {
        Entity test = FXGL.entityBuilder().
                at(x, y).
                view(new NumberPipeView()).
                with(new NumberPipeComponent(value)).
                buildAndAttach();
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.DIGIT1, () -> applyOperation());
        onKeyDown(KeyCode.DIGIT2, () -> applyOperation());
        onKeyDown(KeyCode.R, this::resetGame);
    }

    private void loadLevel(Level level) {
        currentLevel = level;
        startValue = level.getStartValue();
        targetValue = level.getTargetValue();
        currentValue = startValue;
        gameWon = false;
    }

    // Applies a + or - operation to the current value
    public void applyOperation() {
        if (gameWon) {
            return;
        }
        currentValue = startValue;

        // 1st slot
        var slot1Comp = slot1.getComponent(SlotComponent.class);
        if (!slot1Comp.isFilled()) {
            updateUI();
            return;
        }
        var slot1Value = slot1Comp.getValue();
        if (slot1Value < 0){
            slot1Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1 + slot1Value) * 30);
        }else{
            slot1Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1) * 30);
        }
        currentValue += slot1Value;

        // 2nd slot
        var slot2Comp = slot2.getComponent(SlotComponent.class);
        if (!slot2Comp.isFilled()) {
            updateUI();
            return;
        }
        var slot2Value = slot2Comp.getValue();
        if (slot2Value < 0){
            slot2Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1 + slot2Value) * 30);
        }else{
            slot2Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1) * 30);
        }
        currentValue += slot2Value;

        // 3rd slot
        var slot3Comp = slot3.getComponent(SlotComponent.class);
        if (!slot3Comp.isFilled()) {
            updateUI();
            return;
        }
        var slot3Value = slot3Comp.getValue();
        if (slot3Value < 0){
            slot3Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1 + slot3Value) * 30);
        }else{
            slot3Comp.getPipe().getEntity().setX(slot1.getX() + (currentValue - 1) * 30);
        }
        currentValue += slot3Value;

        //currentValue += value;
        updateUI();
        checkGameState();
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void resetGame() {
        currentValue = startValue;
        gameWon = false;
        messageText.setText("");
        resetPipePositions();
        updateUI();
    }

    public void resetPipePositions(){
        List<Entity> pipes = FXGL.getGameWorld().getEntitiesByComponent(NumberPipeComponent.class);
        for (Entity pipeEntity : pipes) {
            NumberPipeComponent pipe = pipeEntity.getComponent(NumberPipeComponent.class);
            pipe.reset();
        }
    }

    private void updateUI() {
        currentText.setText("Current Value: " + currentValue);
    }

    private void checkGameState() {
        if (currentValue == targetValue) {
            gameWon = true;
            messageText.setStyle("-fx-font-size: 24px; -fx-fill: green;");
            messageText.setText("You win!");
            System.out.println("You win!");
        } else if (currentValue > targetValue) {
            messageText.setStyle("-fx-font-size: 24px; -fx-fill: red;");
            messageText.setText("Too high! Press Reset");
        } else {
            messageText.setStyle("-fx-font-size: 24px; -fx-fill: green;");
            messageText.setText("");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}