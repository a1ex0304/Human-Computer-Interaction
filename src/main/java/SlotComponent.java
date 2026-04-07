//package com.example.demo1;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

// Represents a drop slot where pipes can be placed
public class SlotComponent extends Component {
    private String id;
    private NumberPipeComponent currentSlot;
    private Rectangle slotRect;
    private Paint slotColor;

    public SlotComponent(String id, Rectangle slotRect) {
        this.id = id;
        this.slotRect = slotRect;
        slotColor = slotRect.getFill();
    }

    public String getId() {
        return id;
    }
    public void setPipe(NumberPipeComponent pipe) {
        currentSlot = pipe;
    }
    public NumberPipeComponent getPipe() {
        return currentSlot;
    }
    public boolean isFilled() {
        return currentSlot != null;
    }

    // Highlight the slot when a pipe is dragged over it
    public void highlight() {
        if (slotRect != null /* && !isFilled() */) {
            slotRect.setFill(Color.web("white", 0.3));
        }
    }

    // Remove the highlight
    public void unhighlight() {
        if (slotRect != null) {
            slotRect.setFill(slotColor);
        }
    }
}