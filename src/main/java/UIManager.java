package com.example.demo1;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class UIManager {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PIPE_MAX = 20;

    //Pipe dimensions
    private static final double TOP_PIPE_Y = 60;
    private static final double BOT_PIPE_Y = 300;
    private static final double PIPE_LEFT = 30;
    private static final double PIPE_RIGHT = 770;
    private static final double PIPE_HEIGHT = 48;
    private static final double PIPE_INNER_H = 28;

    private BasicGameApp game;

    //UI nodes
    private Canvas pipeCanvas;
    private GraphicsContext gc;
    private Text messageText;
    private Text currentValLabel;

    public UIManager(BasicGameApp game) {
        this.game = game;
    }

    public void build(int startValue, int targetValue, int currentValue) {
        Canvas bg = new Canvas(WIDTH, HEIGHT);
        GraphicsContext bgGc = bg.getGraphicsContext2D();
        drawBackground(bgGc);
        FXGL.addUINode(bg, 0, 0);

        pipeCanvas = new Canvas(WIDTH, HEIGHT);
        gc = pipeCanvas.getGraphicsContext2D();
        drawPipes(gc, startValue, targetValue, currentValue);
        FXGL.addUINode(pipeCanvas, 0, 0);

        drawTiles();

        drawMessageBar(startValue, targetValue);

        drawResetButton();
    }

    //Background
    private void drawBackground(GraphicsContext g) {
        // Sky/wall teal
        g.setFill(Color.web("#3ec6c6"));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        //Brick pattern
        g.setFill(Color.web("#38b8b8"));
        g.setStroke(Color.web("#2da0a0"));
        g.setLineWidth(1.5);
        int brickW = 80, brickH = 40;
        for (int row = 0; row < HEIGHT / brickH + 1; row++) {
            for (int col = 0; col < WIDTH / brickW + 2; col++) {
                int offset = (row % 2 == 0) ? 0 : brickW / 2;
                double x = col * brickW - offset;
                double y = row * brickH;
                g.strokeRect(x, y, brickW, brickH);
            }
        }

        //Dark border frame
        g.setStroke(Color.web("#1a7a8a"));
        g.setLineWidth(12);
        g.strokeRect(6, 6, WIDTH - 12, HEIGHT - 12);

        //Ladder in center
        drawLadder(g, 370, 140, 160);
    }

    private void drawLadder(GraphicsContext g, double x, double y, double height) {
        g.setStroke(Color.web("#c8a96e"));
        g.setLineWidth(5);
        //Side rails
        g.strokeLine(x, y, x, y + height);
        g.strokeLine(x + 40, y, x + 40, y + height);
        //Rungs
        g.setLineWidth(3);
        for (int i = 0; i <= 5; i++) {
            double ry = y + i * (height / 5.0);
            g.strokeLine(x, ry, x + 40, ry);
        }
    }

    //Pipes
    private void drawPipes(GraphicsContext g, int startValue, int targetValue, int currentValue) {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        drawPipe(g, TOP_PIPE_Y, currentValue, true);
        drawPipe(g, BOT_PIPE_Y, targetValue, false);
    }

    //Pipe Designs
    private void drawPipe(GraphicsContext g, double pipeY, int markedValue, boolean isTop) {
        double cx = PIPE_LEFT;
        double cy = pipeY;
        double pw = PIPE_RIGHT - PIPE_LEFT;

        //Body shadow
        g.setFill(Color.web("#555555", 0.3));
        g.fillRoundRect(cx + 4, cy + 6, pw, PIPE_HEIGHT, 20, 20);

        //Metallic gray on Pipes
        g.setFill(Color.web("#9e9e9e"));
        g.fillRoundRect(cx, cy, pw, PIPE_HEIGHT, 20, 20);

        g.setFill(Color.web("#c8c8c8"));
        g.fillRoundRect(cx, cy, pw, PIPE_HEIGHT / 2, 20, 20);

        //Pipe inner channel
        g.setFill(Color.web("#707070"));
        double innerY = cy + (PIPE_HEIGHT - PIPE_INNER_H) / 2;
        g.fillRoundRect(cx + 10, innerY, pw - 20, PIPE_INNER_H, 10, 10);

        //Tick marks and numbers
        g.setStroke(Color.WHITE);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        g.setLineWidth(1.5);

        for (int i = 1; i <= PIPE_MAX; i++) {
            double tickX = cx + (i / (double) PIPE_MAX) * pw;
            double tickY = cy + 2;
            g.strokeLine(tickX, tickY, tickX, tickY + 10);
            g.fillText(String.valueOf(i), tickX - 4, tickY - 2);
        }

        //Marker (valve) at marked position
        double markerX = cx + (markedValue / (double) PIPE_MAX) * pw;
        double markerY = cy + PIPE_HEIGHT / 2;

        //Marker body
        g.setFill(isTop ? Color.web("#cc3333") : Color.web("#2ecc71"));
        g.fillOval(markerX - 14, markerY - 14, 28, 28);
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeOval(markerX - 14, markerY - 14, 28, 28);

        //Marker label
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String label = String.valueOf(markedValue);
        g.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        g.fillText(label, markerX, markerY + 5);

        //End caps
        g.setFill(Color.web("#777777"));
        g.fillOval(cx - 10, cy + 4, 22, PIPE_HEIGHT - 8);
        g.fillOval(PIPE_RIGHT - 12, cy + 4, 22, PIPE_HEIGHT - 8);
        g.setStroke(Color.web("#555555"));
        g.setLineWidth(2);
        g.strokeOval(cx - 10, cy + 4, 22, PIPE_HEIGHT - 8);
        g.strokeOval(PIPE_RIGHT - 12, cy + 4, 22, PIPE_HEIGHT - 8);
    }

    //Tiles
    private void drawTiles() {
        HBox tileRow = new HBox(12);
        tileRow.setAlignment(Pos.CENTER);
        tileRow.setLayoutX(0);
        tileRow.setLayoutY(420);
        tileRow.setPrefWidth(WIDTH);

        tileRow.getChildren().add(makeTile("+2", () -> game.applyOperation(2)));
        tileRow.getChildren().add(makeTile("-1", () -> game.applyOperation(-1)));

        FXGL.addUINode(tileRow, 0, 0);
    }

    private StackPane makeTile(String label, Runnable action) {
        //Brick-colored backing
        Rectangle backing = new Rectangle(80, 70);
        backing.setFill(Color.web("#c8845a"));
        backing.setArcWidth(8);
        backing.setArcHeight(8);
        backing.setStroke(Color.web("#8b5c3e"));
        backing.setStrokeWidth(3);

        //Yellow circle
        Circle circle = new Circle(28);
        circle.setFill(Color.web("#f0d040"));
        circle.setStroke(Color.web("#c8a800"));
        circle.setStrokeWidth(3);

        //Label
        Text t = new Text(label);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        t.setFill(Color.web("#3a3a00"));

        StackPane tile = new StackPane(backing, circle, t);
        tile.setOnMouseClicked(e -> action.run());
        tile.setStyle("-fx-cursor: hand;");

        //Hover effect
        tile.setOnMouseEntered(e -> circle.setFill(Color.web("#ffe060")));
        tile.setOnMouseExited(e -> circle.setFill(Color.web("#f0d040")));

        return tile;
    }

    //Message Bar
    private void drawMessageBar(int startValue, int targetValue) {
        Rectangle bar = new Rectangle(WIDTH - 20, 80);
        bar.setFill(Color.WHITE);
        bar.setArcWidth(16);
        bar.setArcHeight(16);
        bar.setStroke(Color.web("#cccccc"));
        bar.setStrokeWidth(2);

        messageText = new Text("Get from " + startValue + " to " + targetValue + " using +2 and -1!");
        messageText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        messageText.setFill(Color.web("#333333"));

        StackPane bar2 = new StackPane(bar, messageText);
        bar2.setAlignment(Pos.CENTER_LEFT);
        StackPane.setAlignment(messageText, Pos.CENTER_LEFT);
        messageText.setTranslateX(20);

        FXGL.addUINode(bar2, 10, 505);
    }

    //Reset Button
    private void drawResetButton() {
        Button btn = new Button("↺");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        btn.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-border-radius: 50%;" +
                        "-fx-min-width: 44px;" +
                        "-fx-min-height: 44px;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnAction(e -> game.resetGame());
        FXGL.addUINode(btn, 745, 30);
    }

    //Called by BasicGameApp on state change
    public void update(int currentValue, int targetValue) {

        //Redraw pipe canvas
        drawPipes(gc, 0, targetValue, currentValue);
    }

    public void showWin() {
        messageText.setText("You reached the target! Press R to play again.");
        messageText.setFill(Color.web("#27ae60"));
    }

    public void showTooHigh() {
        messageText.setText("Too high! Press R or click ↺ to reset.");
        messageText.setFill(Color.web("#c0392b"));
    }

    public void showPlaying(int startValue, int targetValue) {
        messageText.setText("Get from " + startValue + " to " + targetValue + " using +2 and -1!");
        messageText.setFill(Color.web("#333333"));
    }
}
