package simulator.elevator.game.scene;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.scene.line.AbstractLineTree;
import simulator.elevator.game.scene.line.LineReturn;
import simulator.elevator.game.scene.line.OptionConsequence;
import simulator.elevator.game.scene.line.StatementLineTree;
import simulator.elevator.util.Pair;

public class Scene {

    private final Level level;
    private final StatementLineTree ejectLine;
    private final AbstractLineTree script;
    private boolean ejecting = false;
    
    public Scene(Level level, AbstractLineTree script, StatementLineTree ejectLine) {
        this.level = level;
        this.script = script;
        this.ejectLine = ejectLine;
    }
    
    public LineReturn render(Main game, float deltaSec, Passenger passenger) {
        AbstractLineTree curr = this.ejecting ? this.ejectLine : this.script;

        if (curr == null)
            return LineReturn.FINISH;
        Pair<OptionConsequence,LineReturn> lineOut = curr.render(game, level, passenger.getColor(), deltaSec);
        if (lineOut.first != null)
            lineOut.first.modifyPassenger(passenger);
        if (lineOut.second == LineReturn.EJECT)
            this.ejecting = true;
        
        return lineOut.second;
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
