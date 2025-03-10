package simulator.elevator.game.scene.script;

import simulator.elevator.Main;

public class Scene {

    private final StatementLineTree ejectLine;
    private final AbstractLineTree script;
    private boolean ejecting = false;
    
    public Scene(AbstractLineTree script, StatementLineTree ejectLine) {
        this.script = script;
        this.ejectLine = ejectLine;
    }
    
    public boolean render(Main game, float deltaSec) {
        AbstractLineTree curr = this.ejecting ? this.ejectLine : this.script;

        if (curr == null)
            return true;
        return curr.render(game, deltaSec);
    }
    
    public void eject() {
        this.ejecting = true;
    }
    
    public void reset() {
        this.script.reset();
        if (this.ejectLine != null) 
            this.ejectLine.reset();
        this.ejecting = false;
    }
}
