import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

public class BasicGameApp extends GameApplication {

    private int startValue = 5;
    private int targetValue = 12;
    private int currentValue = startValue;

    private boolean gameWon = false;

    private Text currentText;
    private Text messageText;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("EquationFlow");
    }

    @Override
    protected void initGame() {
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

        var addButton = getUIFactoryService().newButton("+2");
        addButton.setTranslateX(150);
        addButton.setTranslateY(450);
        addButton.setPrefWidth(120);
        addButton.setPrefHeight(60);
        addButton.setStyle("-fx-font-size: 18px; -fx-background-radius: 10;");
        addButton.setOnAction(e -> applyOperation(2));

        var subButton = getUIFactoryService().newButton("-1");
        subButton.setTranslateX(300);
        subButton.setTranslateY(450);
        subButton.setPrefWidth(120);
        subButton.setPrefHeight(60);
        subButton.setStyle("-fx-font-size: 18px; -fx-background-radius: 10;");
        subButton.setOnAction(e -> applyOperation(-1));

        var resetButton = getUIFactoryService().newButton("Reset");
        resetButton.setTranslateX(450);
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
        addUINode(addButton);
        addUINode(subButton);
        addUINode(resetButton);
    }

    @Override
    protected void initInput() {
        onKeyDown(KeyCode.DIGIT1, () -> applyOperation(2));
        onKeyDown(KeyCode.DIGIT2, () -> applyOperation(-1));
        onKeyDown(KeyCode.R, this::resetGame);
    }

    // Applies a + or - operation to the current value
    public void applyOperation(int value) {
        if (gameWon) {
            return;
        }

        currentValue += value;
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
        updateUI();
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