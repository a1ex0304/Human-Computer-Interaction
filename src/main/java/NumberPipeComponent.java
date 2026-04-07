//package com.example.demo1;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.input.MouseEvent;

// Handles pipe drag-and-drop and value flipping
public class NumberPipeComponent extends Component {
    private int baseValue;
    private int sign = 1;
    private double dragOffsetX, dragOffsetY;
    private SlotComponent currentSlot;
    private SlotComponent hoveredSlot;
    private double homeX, homeY;

    public NumberPipeComponent(int value) {
        this.baseValue = value;
    }

    @Override
    public void onAdded() {
        homeX = entity.getX();
        homeY = entity.getY();

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            // Save the board before the player starts moving this pipe
            BasicGameApp.instance.rememberStateForUndo();
            dragOffsetX = e.getSceneX() - entity.getX();
            dragOffsetY = e.getSceneY() - entity.getY();

            if (currentSlot != null) {
                currentSlot.setPipe(null);
                currentSlot = null;
            }

            clearHoveredSlot();
            entity.setZIndex(100);
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            entity.setX(e.getSceneX() - dragOffsetX);
            entity.setY(e.getSceneY() - dragOffsetY);
            updateHoveredSlot(e);
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            entity.setZIndex(0);
            clearHoveredSlot();
            checkDrop(e);
        });

        ((NumberPipeView) entity.getViewComponent().getChildren().get(0)).SetPipeComponent(this);
    }

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

    private void clearHoveredSlot() {
        if (hoveredSlot != null) {
            hoveredSlot.unhighlight();
            hoveredSlot = null;
        }
    }

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
            if (bestSlot.isFilled()) {
                var overlapPipe = bestSlot.getPipe();
                overlapPipe.entity.setX(overlapPipe.homeX);
                overlapPipe.entity.setY(overlapPipe.homeY);
                bestSlot.setPipe(null);
                overlapPipe.currentSlot = null;
            }

            currentSlot = bestSlot;
            currentSlot.setPipe(this);
            BasicGameApp.instance.refreshAllPipes();
            ((NumberPipeView) entity.getViewComponent().getChildren().get(0)).flash();
        } else {
            entity.setX(homeX);
            entity.setY(homeY);
        }

        BasicGameApp.instance.applyOperation();
    }

    public void snapTo(double targetStartX) {
        if (getValue() >= 0) {
            entity.setX(targetStartX);
        } else {
            entity.setX(targetStartX - (Math.abs(getValue()) * BasicGameApp.UNIT_WIDTH));
        }
        entity.setY(currentSlot.getEntity().getY());
    }

    public void invertValue() {
        // Save the current setup before flipping the sign
        BasicGameApp.instance.rememberStateForUndo();
        sign *= -1;
        ((NumberPipeView) entity.getViewComponent().getChildren().get(0)).RefreshValue();
        if (currentSlot != null) {
            BasicGameApp.instance.refreshAllPipes();
        }
        BasicGameApp.instance.applyOperation();
    }

    // Used by undo/redo to put this pipe back into an older saved state
    public void restoreState(int restoredSign, double restoredX, double restoredY, SlotComponent restoredSlot) {
        sign = restoredSign;
        currentSlot = restoredSlot;
        if (currentSlot != null) {
            currentSlot.setPipe(this);
        }
        entity.setX(restoredX);
        entity.setY(restoredY);
        ((NumberPipeView) entity.getViewComponent().getChildren().get(0)).RefreshValue();
    }

    public SlotComponent getCurrentSlot() {
        return currentSlot;
    }

    public int getSign() {
        return sign;
    }

    public double getHomeX() {
        return homeX;
    }

    public double getHomeY() {
        return homeY;
    }

    public int getValue() {
        return baseValue * sign;
    }
}
