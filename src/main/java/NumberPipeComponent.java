import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.MouseEvent;

public class NumberPipeComponent extends Component {

    private BasicGameApp game;
    private int value;
    private double dragOffsetX;
    private double dragOffsetY;
    private SlotComponent currentSlot;
    private NumberPipeView numberPipeView;

    public NumberPipeComponent(int value) {
        this.value = value;
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            dragOffsetX = e.getSceneX() - entity.getX();
            dragOffsetY = e.getSceneY() - entity.getY();
            if (currentSlot == null) {
                return;
            }
            currentSlot.setPipe(null);
            currentSlot = null;
            BasicGameApp.instance.applyOperation();
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            entity.setX(e.getSceneX() - dragOffsetX);
            entity.setY(e.getSceneY() - dragOffsetY);
        });

        entity.getViewComponent().addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            checkDrop();
        });

        numberPipeView = entity.getViewComponent().getChild(0, NumberPipeView.class);
        numberPipeView.SetPipeComponent(this);
    }

    private void checkDrop() {
        // Simple hit-test: find nearest slot and snap if close enough
        var slots = FXGL.getGameWorld().getEntitiesByComponent(SlotComponent.class);

        SlotComponent bestSlot = null;

        for (var slotEntity : slots) {
            var slot = slotEntity.getComponent(SlotComponent.class);
            double minX = slotEntity.getX();
            double minY = slotEntity.getY();
            double maxX = minX + 480;
            double maxY = minY + 30;


            double mouseX = entity.getX() + dragOffsetX;
            double mouseY = entity.getY() + dragOffsetY;
            if (minX < mouseX & mouseX < maxX & minY < mouseY & mouseY < maxY) {
                bestSlot = slot;
                break;
            }
        }

        if (bestSlot == null) {
            return;
        }

        currentSlot = bestSlot;
        // Snap to slot
        var slotEntity = currentSlot.getEntity();
        entity.setX(slotEntity.getX());
        entity.setY(slotEntity.getY());
        currentSlot.setPipe(this);

        // After placing, check equation
        BasicGameApp.instance.applyOperation();
    }

    public void invertValue() {
        value *= -1;
        BasicGameApp.instance.applyOperation();
        numberPipeView.RefreshValue();
    }

    public int getValue() {
        return value;
    }
}
