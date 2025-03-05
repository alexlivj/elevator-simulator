package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.RelativeCoordinate;

public class Passenger extends LinearEntity {
    
    public enum PState {
        ARRIVING,
        WAITING,
        RIDING,
        LEAVING;
    }
    
    private int happiness = 100;
    private PState currentState = PState.ARRIVING;
    private boolean currentStateAction = false;
    private final int startFloor;
    private final int destFloor;
    private final Scene scene;
    private final int waitX;
    private final int speedPixelSec;
    
    public Passenger(RelativeCoordinate startPos,
                     int startFloor, int destFloor,
                     Texture texture, Scene scene,
                     int waitX, int speedPixelSec) {
        super(startPos, texture);
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.scene = scene;
        this.waitX = waitX;
        this.speedPixelSec = speedPixelSec;
    }
    
    @Override
    public void update(float deltaSec) {
        //TODO do the ai stuff
        if (this.currentState == PState.ARRIVING && !this.isMoving()) {
            if (!this.currentStateAction) {
                RelativeCoordinate pos = new RelativeCoordinate(getPosition());
                pos.getRelativeVector().x = this.waitX;
                moveTo(pos, speedPixelSec);
                this.currentStateAction = true;
            } else {
                this.currentState = PState.WAITING;
                this.currentStateAction = false;
            }
        }
        
        super.update(deltaSec);
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PState getState() {
        return this.currentState;
    }

}
