//package com.example.demo1;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.MouseEvent;

// Handles pipe drag-and-drop and value flipping
public class NumberPipeComponent extends Component {
    private int baseValue;
    private int sign = 1;
    private double dragOffsetX, dragOffsetY;
    private SlotComponent currentSlot;
    private SlotComponent hoveredSlot;
    private double homeX, homeY;

    public NumberPipeComponent(int value) { this.baseValue = value; }

    @Override
    public void onAdded() {
        // Save original position for resetting
        homeX = entity.getX();
        homeY = entity.getY();

        // Start dragging
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            dragOffsetX = e.getSceneX() - entity.getX();
            dragOffsetY = e.getSceneY() - entity.getY();
            // Remove from current slot if placed
            if (currentSlot != null) {
                currentSlot.setPipe(null);
                currentSlot = null;
            }
            clearHoveredSlot();
            entity.setZIndex(100);
        });

        // Dragging movement
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            entity.setX(e.getSceneX() - dragOffsetX);
            entity.setY(e.getSceneY() - dragOffsetY);
            updateHoveredSlot(e);
        });

        // Drop the pipe
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            entity.setZIndex(0);
            clearHoveredSlot();
            checkDrop(e);
        });

        // Connect to view
        ((NumberPipeView)entity.getViewComponent().getChildren().get(0)).SetPipeComponent(this);
    }

    // Track which slot the pipe is hovering over
    private void updateHoveredSlot(MouseEvent e) {
        var slots = FXGL.getGameWorld().getEntitiesByComponent(SlotComponent.class);
        SlotComponent newHovered = null;

        for (var slotEntity : slots) {
            if (Math.abs(e.getY() - slotEntity.getY()) < BasicGameApp.PIPE_HEIGHT) {
                newHovered = slotEntity.getComponent(SlotComponent.class);
                break;
            }
        }

        if (newHovered != hoveredSlot) {
            clearHoveredSlot();
            hoveredSlot = newHovered;
            if (hoveredSlot != null) {
                hoveredSlot.highlight();
            }
        }
    }

    // Clear hover highlight
    private void clearHoveredSlot() {
        if (hoveredSlot != null) {
            hoveredSlot.unhighlight();
            hoveredSlot = null;
        }
    }

    // Check where to drop the pipe
    private void checkDrop(MouseEvent e) {
        var slots = FXGL.getGameWorld().getEntitiesByComponent(SlotComponent.class);
        SlotComponent bestSlot = null;

        for (var slotEntity : slots) {
            if (Math.abs(e.getY() - slotEntity.getY()) < BasicGameApp.PIPE_HEIGHT) {
                bestSlot = slotEntity.getComponent(SlotComponent.class);
                break;
            }
        }

        if (bestSlot != null) {
            // If slot already has a pipe, send it back home
            if (bestSlot.isFilled()) {
                var overlapPipe = bestSlot.getPipe();
                overlapPipe.entity.setX(overlapPipe.homeX);
                overlapPipe.entity.setY(overlapPipe.homeY);
                bestSlot.setPipe(null);
                overlapPipe.currentSlot = null;
            }
            // Place pipe in slot
            currentSlot = bestSlot;
            currentSlot.setPipe(this);
            BasicGameApp.instance.refreshAllPipes();
            ((NumberPipeView)entity.getViewComponent().getChildren().get(0)).flash();
        } else {
            // Return to original position if not dropped in a slot
            entity.setX(homeX);
            entity.setY(homeY);
        }
        BasicGameApp.instance.applyOperation();
    }

    // Snap pipe to the correct position based on value
    public void snapTo(double targetStartX) {
        if (getValue() >= 0) {
            entity.setX(targetStartX);
        } else {
            entity.setX(targetStartX - (Math.abs(getValue()) * BasicGameApp.UNIT_WIDTH));
        }
        entity.setY(currentSlot.getEntity().getY());
    }

    // Flip between positive and negative
    public void invertValue() {
        sign *= -1;
        ((NumberPipeView)entity.getViewComponent().getChildren().get(0)).RefreshValue();
        if (currentSlot != null) BasicGameApp.instance.refreshAllPipes();
        BasicGameApp.instance.applyOperation();
    }

    // Get current value (positive or negative)
    public int getValue() {
        return baseValue * sign;
    }
}
