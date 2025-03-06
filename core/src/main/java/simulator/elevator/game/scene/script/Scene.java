package simulator.elevator.game.scene.script;

public class Scene {

    private final NpcLineTree ejectLine;
    private final AbstractLineTree script;
    private boolean ejecting = false;
    
    public Scene(AbstractLineTree script, NpcLineTree ejectLine) {
        this.script = script;
        this.ejectLine = ejectLine;
    }
    
    public boolean render(float deltaSec) {
        AbstractLineTree curr = this.ejecting ? this.ejectLine : this.script;
        
        return curr.render(deltaSec);
    }
    
    public void eject() {
        this.ejecting = true;
    }
    
    public void reset() {
        this.ejecting = false;
    }
}
