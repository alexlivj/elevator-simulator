package simulator.elevator.game.scene.script;

import simulator.elevator.game.entity.passenger.Passenger;

public interface OptionConsequence {
    public void modifyPassenger(Passenger passenger);
}
