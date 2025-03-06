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
        
        public final int value;
        
        private PState(int value) {
            this.value = value;
        }
        
        public boolean isBeforeOrAt(PState p) {
            return this.value <= p.value;
        }
    }
    
    public record Personality (int speedPixelSec, float patience, float generosity) {}
    
    private final PassengerDirector director;
    
    private float happiness = 100;
    private PState currentState = PState.ARRIVING;
    private boolean currentStateAction = false;
    
    private final int startFloor;
    private final int destFloor;
    private final Scene scene;
    private final Personality personality;
    
    public Passenger(PassengerDirector director,
                     int startFloor, int destFloor,
                     Texture texture, Scene scene,
                     Personality personality) {
        super(director.getFloorSpawn(startFloor), texture);
        this.director = director;
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.scene = scene;
        this.personality = personality;
    }
    
    @Override
    public void update(float deltaSec) {
        switch (this.currentState) {
            case ARRIVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        RelativeCoordinate waitingSlot = director.requestWaitingSlot(this);
                        if (waitingSlot == null) {
                            // this should never happen, but just in case....
                            director.despawn(this);
                        } else {
                            moveTo(waitingSlot, this.personality.speedPixelSec);
                            this.currentStateAction = true;
                        }
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
                        moveTo(elevatorSlot, this.personality.speedPixelSec);
                        this.currentStateAction = true;
                    }
                    //TODO some sort of "elevator full" scene, if director commands
                }
                break;
            case LOADING, UNLOADING:
                boolean isLoading = this.currentState == PState.LOADING;
                int toFloor = isLoading ? this.startFloor : this.destFloor;
                if (!this.director.isElevatorAtFloor(toFloor)) {
                    //TODO write more sophisticated "door slammed in face" checking
                    // ideally, we'd only want the reset to happen if the door intersects with the passsenger
                    this.currentStateAction = false;
                    cancelMove();
                    this.director.clearElevatorSlot(this);
                    this.currentState = isLoading ? PState.WAITING : PState.RIDING;
                    //TODO some sort of "wth bro" scene, if director commands
                    // maybe we express indignation, and if the director approve, we use personality to select a scene
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    this.currentState = isLoading ? PState.RIDING : PState.LEAVING;
                    if (isLoading)
                        this.director.clearWaitingSlot(this);
                    //TODO if loading, tell the player which floor where to go with a scene
                }
                break;
            case RIDING:
                if (this.director.isElevatorAtFloor(this.destFloor)) {
                    this.currentState = PState.UNLOADING;
                    moveTo(this.director.getFloorExit(this.destFloor), this.personality.speedPixelSec);
                    this.currentStateAction = true;
                }
                break;
            case LEAVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        this.director.handleTip(calculateTip());
                        this.director.clearElevatorSlot(this);
                        moveTo(this.director.getFloorSpawn(this.destFloor), this.personality.speedPixelSec);
                        this.currentStateAction = true;
                    } else {
                        this.director.despawn(this);
                    }
                }
                break;
            default:
                break;
        }
        
        // decay happiness
        float d = 1-PassengerDirector.HAPPINESS_DECAY_RATE_SEC;
        float mod = this.personality.patience * PassengerDirector.HAPPINESS_DECAY_MOD[this.currentState.value];
        float decaySec = 1 - mod*d;
        this.happiness *= Math.pow(decaySec,deltaSec);
        
        super.update(deltaSec);
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PState getState() {
        return this.currentState;
    }
    
    public int calculateTip() {
        int tip = Math.round(PassengerDirector.MAX_TIP_CENTS * 100/this.happiness * this.personality.generosity);
        System.out.println("end ride tip: happiness="+this.happiness+", generosity="+this.personality.generosity+", tip="+tip);
        return tip;
    }

}
