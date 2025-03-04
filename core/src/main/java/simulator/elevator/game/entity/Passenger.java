package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.game.RelativeCoordinate;
import simulator.elevator.game.scene.Scene;

public class Passenger extends LinearEntity {
    
    public enum PState {
        ARRIVING,
        WAITING,
        RIDING,
        LEAVING;
    }
    
    private int happiness = 100;
    private PState currentState = PState.ARRIVING;
    private final int startFloor;
    private final int destFloor;
    private final Scene scene;
    
    public Passenger(RelativeCoordinate startPos, int startFloor, int destFloor, Texture texture, Scene scene) {
        super(startPos, texture);
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.scene = scene;
    }
    
    @Override
    public void update(float deltaSec) {
        //TODO do the ai stuff
        
        super.update(deltaSec);
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PState getState() {
        return this.currentState;
    }

}
