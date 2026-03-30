package com.example.demo1;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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
    private NumberPipeComponent pipe;
    private Rectangle body = new Rectangle();
    private Rectangle capL = new Rectangle(12, 40);
    private Rectangle capR = new Rectangle(12, 40);
    private Text valText = new Text();
    private Button flipBtn = new Button("🔄");

    public NumberPipeView() {
        body.setFill(Color.web("#7f8c8d")); body.setHeight(26); body.setY(7);
        capL.setFill(Color.web("#784212")); capR.setFill(Color.web("#784212"));

        Circle circle = new Circle(16, Color.web("#f1c40f"));
        valText.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        StackPane label = new StackPane(circle, valText);

        flipBtn.setStyle("-fx-background-radius: 20; -fx-background-color: #3498db; -fx-text-fill: white;");
        flipBtn.setPrefSize(30, 30);
        flipBtn.setTranslateY(-35);

        getChildren().addAll(body, capL, capR, label, flipBtn);
        flipBtn.setOnAction(e -> pipe.invertValue());
    }

    public void RefreshValue() {
        double w = Math.abs(pipe.getValue()) * BasicGameApp.UNIT_WIDTH;
        body.setWidth(w);
        capR.setX(w - 6);
        valText.setText(String.valueOf(Math.abs(pipe.getValue())));

        getChildren().get(3).setTranslateX(w / 2);
        getChildren().get(3).setTranslateY(20);
        flipBtn.setTranslateX(w / 2 - 15);
    }

    public void SetPipeComponent(NumberPipeComponent p) {
        this.pipe = p; RefreshValue();
    }

    public void flash() {
        Color original = (Color) body.getFill();
        Color flashColor = Color.web("#2ecc71");

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