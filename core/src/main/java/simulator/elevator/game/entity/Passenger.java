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
            return this.value <= p.value;
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
        Integer elevatorCurrFloor = director.getElevatorCurrentFloor();
        switch (this.currentState) {
            case ARRIVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        RelativeCoordinate waitPos = new RelativeCoordinate(getPosition());
                        waitPos.getRelativeVector().x = this.waitX;
                        moveTo(waitPos, speedPixelSec);
                        this.currentStateAction = true;
                    } else {
                        this.currentState = PState.WAITING;
                        this.currentStateAction = false;
                    }
                }
                break;
            case WAITING:
                if (director.isElevatorAtFloor(this.startFloor)) {
                    RelativeCoordinate elevatorSlot = director.requestElevatorEntry(this);
                    if (elevatorSlot != null) {
                        this.currentState = PState.LOADING;
                        moveTo(elevatorSlot, this.speedPixelSec);
                        this.currentStateAction = true;
                    }
                    //TODO some sort of "elevator full" scene, if director commands
                }
                break;
            case LOADING, UNLOADING:
                //TODO write more sophisticated "door slammed in face" code handling
                int toFloor = this.currentState == PState.LOADING ? this.startFloor : this.destFloor;
                if (!director.isElevatorAtFloor(toFloor)) {
                    this.currentStateAction = false;
                    cancelMove();
                    this.currentState = PState.WAITING;
                    //TODO some sort of "wth bro" scene, if director commands
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    currentState = this.currentState == PState.LOADING ? PState.RIDING : PState.LEAVING;
                }
                break;
            case RIDING:
                if (director.isElevatorAtFloor(this.destFloor)) {
                    this.currentState = PState.UNLOADING;
                    moveTo(director.getFloorExit(this.destFloor), this.speedPixelSec);
                    this.currentStateAction = true;
                }
                break;
            case LEAVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        director.clearElevatorSlot(this);
                        moveTo(director.getFloorSpawn(this.destFloor), speedPixelSec);
                        this.currentStateAction = true;
                    } else {
                        //TODO despawn
                    }
                }
                break;
            default:
                break;
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
