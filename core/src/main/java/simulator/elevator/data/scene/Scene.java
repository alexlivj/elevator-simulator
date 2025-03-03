package simulator.elevator.data.scene;

import java.util.List;

public class Scene {

    private final Line ejectLine;
    private final List<Line> script; 
    private int index;
    private boolean ejecting = false;
    
    public Scene(List<Line> script, Line ejectLine) {
        this.script = script;
        this.ejectLine = ejectLine;
    }
    
    public boolean render(float delta) {
        Line curr;
        if (ejecting)
            curr = ejectLine;
        else
            curr = script.get(index);
        boolean finished = false;
        return finished;
    }
    
    public void eject() {
    }
    
    public void reset() {
    }
}
