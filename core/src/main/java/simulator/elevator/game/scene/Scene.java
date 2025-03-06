package simulator.elevator.game.scene;

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
    
    public boolean render(float deltaSec) {
        Line curr = this.ejecting ? this.ejectLine : this.script.get(this.index);
        
        if (curr.render(deltaSec))
            this.index++;
        
        return this.index >= this.script.size();
    }
    
    public void eject() {
        this.ejecting = true;
    }
    
    public void reset() {
        this.index = 0;
        this.ejecting = false;
    }
}
