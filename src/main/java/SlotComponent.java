package com.example.demo1;

import com.almasb.fxgl.entity.component.Component;

public class SlotComponent extends Component {
    private String id;
    private NumberPipeComponent currentPipe;

    public SlotComponent(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setPipe(NumberPipeComponent pipe) {
        currentPipe = pipe;
    }
    public NumberPipeComponent getPipe() {
        return currentPipe;
    }
    public boolean isFilled() {
        return currentPipe != null;
    }
}