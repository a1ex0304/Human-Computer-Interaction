package com.example.demo1;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.MouseEvent;

public class NumberPipeComponent extends Component {
    private int baseValue;
    private int sign = 1;
    private double dragOffsetX, dragOffsetY;
    private SlotComponent currentSlot;
    private double homeX, homeY;

    public NumberPipeComponent(int value) { this.baseValue = value; }

    @Override
    public void onAdded() {
        homeX = entity.getX();
        homeY = entity.getY();

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            dragOffsetX = e.getSceneX() - entity.getX();
            dragOffsetY = e.getSceneY() - entity.getY();
            if (currentSlot != null) {
                currentSlot.setPipe(null);
                currentSlot = null;
            }
            entity.setZIndex(100);
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            entity.setX(e.getSceneX() - dragOffsetX);
            entity.setY(e.getSceneY() - dragOffsetY);
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            entity.setZIndex(0);
            checkDrop();
        });

        ((NumberPipeView)entity.getViewComponent().getChildren().get(0)).SetPipeComponent(this);
    }

    private void checkDrop() {
        var slots = FXGL.getGameWorld().getEntitiesByComponent(SlotComponent.class);
        SlotComponent bestSlot = null;

        for (var slotEntity : slots) {
            if (Math.abs(entity.getY() - slotEntity.getY()) < 50) {
                bestSlot = slotEntity.getComponent(SlotComponent.class);
                break;
            }
        }

        if (bestSlot != null && !bestSlot.isFilled()) {
            currentSlot = bestSlot;
            currentSlot.setPipe(this);
            BasicGameApp.instance.refreshAllPipes();
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
        sign *= -1;
        ((NumberPipeView)entity.getViewComponent().getChildren().get(0)).RefreshValue();
        if (currentSlot != null) BasicGameApp.instance.refreshAllPipes();
        BasicGameApp.instance.applyOperation();
    }

    public int getValue() {
        return baseValue * sign;
    }
    }
