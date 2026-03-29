import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

import java.awt.*;

import static com.almasb.fxgl.dsl.FXGL.getUIFactoryService;

public class NumberPipeView extends Group {
    NumberPipeComponent pipeComponent;

    Rectangle pipeBody;
    Rectangle pipeStart;
    Rectangle pipeEnd;
    Text text;
    Button invertButton;

    public NumberPipeView() {
        pipeBody = new Rectangle(30, 20, Color.LIGHTBLUE);
        pipeBody.setX(5);
        pipeBody.setY(5);

        pipeStart = new Rectangle(30, 5, Color.GREEN);
        pipeStart.setY(0);

        pipeEnd = new Rectangle(30, 5, Color.RED);
        pipeEnd.setY(25);

        text = new Text();
        text.setFill(Color.BLACK);
        text.setStyle("-fx-font-size: 24;");
        text.setY(23);

        invertButton = getUIFactoryService().newButton("-");
        invertButton.setTranslateY(0);
        invertButton.setPrefWidth(30);
        invertButton.setPrefHeight(30);
        invertButton.setStyle("-fx-font-size: 18px; -fx-background-radius: 10; -fx-background-color: gray");
        invertButton.setOnAction(e -> {
            pipeComponent.invertValue();
        });

        getChildren().addAll(pipeBody, pipeStart, pipeEnd, text, invertButton);
    }

    public void RefreshValue(){
        var value = pipeComponent.getValue();
        var absValue = Math.abs(value);
        var negative = value < 0;
        pipeBody.setWidth(30*(absValue + 1)-10);

        if(negative){
            pipeStart.setX(30*absValue);
            pipeEnd.setX(0);
        }else{
            pipeStart.setX(0);
            pipeEnd.setX(30*absValue);
        }

        text.setText(String.valueOf(value));
        text.setX(8 + 15 * absValue);;

        invertButton.setTranslateX(30 * (absValue + 1));
    }

    public void SetPipeComponent(NumberPipeComponent pipe){
        pipeComponent = pipe;
        RefreshValue();
    }
}
