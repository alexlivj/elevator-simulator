package simulator.elevator.game.entity;

import simulator.elevator.game.RelativeCoordinate;

public class Elevator extends Entity {
    
    private int durability = 100;
    private boolean openDoor = true;
    
    public Elevator(RelativeCoordinate pos) {
        super(pos, null); //TODO constant for elevator texture
    }
    
}
