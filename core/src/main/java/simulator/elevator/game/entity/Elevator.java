package simulator.elevator.game.entity;

import com.badlogic.gdx.math.Vector2;

public class Elevator extends Entity {
    
    private int durability = 100;
    private boolean openDoor = true;
    
    public Elevator(Vector2 pos) {
        super(pos);
    }
    
}
