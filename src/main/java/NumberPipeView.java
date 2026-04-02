//package com.example.demo1;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.animation.FillTransition;
import javafx.util.Duration;

public class NumberPipeView extends Group {
    public static final int PIPE_BODY_THICKNESS = 26;
    public static final int PIPE_BODY_PADDING_Y = (BasicGameApp.PIPE_HEIGHT - PIPE_BODY_THICKNESS)/2;
    public static final int PIPE_CAP_WIDTH = 38;
    public static final int PIPE_CAP_PADDING_X = (BasicGameApp.UNIT_WIDTH - PIPE_CAP_WIDTH)/2;

    public static final Paint PIPECOLOR_VALUE_LABEL_POSITIVE = Color.web("#006eff");
    public static final Paint PIPECOLOR_VALUE_LABEL_NEGATIVE = Color.web("#ff0000");

    public static final Paint PIPECOLOR_BODY = Color.web("#7f8c8d");
    public static final Paint PIPECOLOR_CAP = Color.web("#784212");


    private NumberPipeComponent pipe;
    private Rectangle body = new Rectangle();
    private Rectangle capStart = new Rectangle(PIPE_CAP_WIDTH, 12);
    private Rectangle capEnd = new Rectangle(PIPE_CAP_WIDTH, 12);
    private Text valText = new Text();
    private Button flipBtn = new Button("🔄");
    private StackPane label;

    public NumberPipeView() {
        body.setFill(PIPECOLOR_BODY); body.setHeight(PIPE_BODY_THICKNESS); body.setX(BasicGameApp.UNIT_WIDTH/2); body.setY(PIPE_BODY_PADDING_Y);
        capStart.setFill(PIPECOLOR_CAP);
        capEnd.setFill(PIPECOLOR_CAP); capEnd.setY(BasicGameApp.PIPE_HEIGHT - 12);

        Circle circle = new Circle(16, Color.web("#ffffff"));
        valText.setFont(Font.font("Cambia", FontWeight.EXTRA_BOLD, 16));
        label = new StackPane(circle, valText); label.setTranslateY(BasicGameApp.PIPE_HEIGHT/2 - 16);

        flipBtn.setStyle("-fx-background-radius: 20 ;-fx-background-color: #3498db; -fx-text-fill: white;");
        flipBtn.setPrefSize(32, 32); flipBtn.setTranslateY(BasicGameApp.PIPE_HEIGHT/2 - 16);

        getChildren().addAll(body, capStart, capEnd, label, flipBtn);
        flipBtn.setOnAction(e -> pipe.invertValue());
    }

    public void RefreshValue() {
        int pipeValue = pipe.getValue();
        boolean isNegative = pipeValue < 0;
        double w = Math.abs(pipeValue) * BasicGameApp.UNIT_WIDTH;
        body.setWidth(w);
        valText.setText(isNegative ? String.valueOf(pipeValue) : "+" + String.valueOf(pipeValue));
        valText.setFill(isNegative ? PIPECOLOR_VALUE_LABEL_NEGATIVE : PIPECOLOR_VALUE_LABEL_POSITIVE);

        capStart.setX(PIPE_CAP_PADDING_X + (isNegative ? w : 0));
        capEnd.setX(PIPE_CAP_PADDING_X + (isNegative ? 0 : w));

        label.setTranslateX(w / 2 + BasicGameApp.UNIT_WIDTH - 16);
        flipBtn.setTranslateX(w / 2 - 16);
    }

    public void SetPipeComponent(NumberPipeComponent p) {
        this.pipe = p; RefreshValue();
    }

    public void flash() {
        Color original = (Color) body.getFill();
        Color flashColor = Color.web("#3498db");

        FillTransition ft1 = new FillTransition(Duration.millis(150), body, original, flashColor);
        FillTransition ft2 = new FillTransition(Duration.millis(150), body, flashColor, original);
        FillTransition ft3 = new FillTransition(Duration.millis(150), body, original, flashColor);
        FillTransition ft4 = new FillTransition(Duration.millis(150), body, flashColor, original);

        ft1.setOnFinished(e -> ft2.play());
        ft2.setOnFinished(e -> ft3.play());
        ft3.setOnFinished(e -> ft4.play());

        ft1.play();
    }
}