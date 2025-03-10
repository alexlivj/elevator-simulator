package simulator.elevator.game.scene.script;

import simulator.elevator.Main;
import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.util.Pair;

public class Scene {

    private final StatementLineTree ejectLine;
    private final AbstractLineTree script;
    private boolean ejecting = false;
    
    public Scene(AbstractLineTree script, StatementLineTree ejectLine) {
        this.script = script;
        this.ejectLine = ejectLine;
    }
    
    public boolean render(Main game, float deltaSec, Passenger passenger) {
        AbstractLineTree curr = this.ejecting ? this.ejectLine : this.script;

        if (curr == null)
            return true;
        Pair<OptionConsequence,LineReturn> lineOut = curr.render(game, deltaSec);
        if (lineOut.first != null)
            lineOut.first.modifyPassenger(passenger);
        if (lineOut.second == LineReturn.EJECT)
            this.ejecting = true;
        
        return lineOut.second == LineReturn.FINISH;
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
