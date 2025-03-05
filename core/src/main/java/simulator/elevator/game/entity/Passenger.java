package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.game.PassengerDirector;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.RelativeCoordinate;

public class Passenger extends LinearEntity {
    
    public enum PState implements Comparable<PState> {
        ARRIVING(0),
        WAITING(1),
        LOADING(2),
        RIDING(3),
        UNLOADING(4),
        LEAVING(5);
        
        private final Integer value;
        
        private PState(Integer value) {
            this.value = value;
        }
        
        public boolean isBeforeOrAt(PState p) {
            return this.value < p.value;
        }
    }
    
    private final PassengerDirector director;
    
    private int happiness = 100;
    private PState currentState = PState.ARRIVING;
    private boolean currentStateAction = false;
    private final int startFloor;
    private final int destFloor;
    private final Scene scene;
    
    private final int waitX;
    private final int speedPixelSec;
    private float patience;
    private float generosity;
    
    public Passenger(PassengerDirector director,
                     int startFloor, int destFloor,
                     Texture texture, Scene scene,
                     int waitX, int speedPixelSec) {
        super(director.getFloorSpawn(startFloor), texture);
        this.director = director;
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
