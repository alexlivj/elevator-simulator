package simulator.elevator.game.entity.passenger;

import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.game.manager.PassengerDirector;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.RelativeCoordinate;

public class Passenger extends LinearEntity {
    
    private final PassengerDirector director;
    
    private float happiness = 100;
    private PassengerState currentState = PassengerState.ARRIVING;
    private boolean currentStateAction = false;
    
    private final int startFloor;
    private final int destFloor;
    private final Scene specialScene;
    private final PassengerPersonality personality;
    
    public Passenger(PassengerDirector director,
                     int startFloor, int destFloor,
                     Texture texture, Scene scene,
                     PassengerPersonality personality) {
        super(director.getFloorSpawn(startFloor), texture);
        this.director = director;
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.specialScene = scene;
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
                            moveTo(waitingSlot, this.personality.speedPixelSec());
                            this.currentStateAction = true;
                        }
                    } else {
                        this.currentState = PassengerState.WAITING;
                        this.currentStateAction = false;
                    }
                }
                break;
            case WAITING:
                if (director.isElevatorAtFloor(this.startFloor)) {
                    RelativeCoordinate elevatorSlot = director.requestElevatorEntry(this);
                    if (elevatorSlot != null) {
                        this.currentState = PassengerState.LOADING;
                        moveTo(elevatorSlot, this.personality.speedPixelSec());
                        this.currentStateAction = true;
                    }
                    //TODO some sort of "elevator full" scene, if director commands
                }
                break;
            case LOADING, UNLOADING:
                boolean isLoading = this.currentState == PassengerState.LOADING;
                int toFloor = isLoading ? this.startFloor : this.destFloor;
                if (!this.director.isElevatorAtFloor(toFloor)) {
                    //TODO write more sophisticated "door slammed in face" checking
                    // ideally, we'd only want the reset to happen if the door intersects with the passsenger
                    this.currentStateAction = false;
                    cancelMove();
                    this.happiness = Math.max(0, this.happiness+PassengerDirector.DOOR_SLAM_PENALTY);
                    this.director.clearElevatorSlot(this);
                    this.currentState = isLoading ? PassengerState.WAITING : PassengerState.RIDING;
                    //TODO some sort of "wth bro" scene, if director commands
                    // maybe we express indignation, and if the director approve, we use personality to select a scene
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    this.currentState = isLoading ? PassengerState.RIDING : PassengerState.LEAVING;
                    if (isLoading)
                        this.director.clearWaitingSlot(this);
                    //TODO if loading, tell the player which floor where to go with a scene
                }
                break;
            case RIDING:
                if (this.director.isElevatorAtFloor(this.destFloor)) {
                    this.currentState = PassengerState.UNLOADING;
                    moveTo(this.director.getFloorExit(this.destFloor), this.personality.speedPixelSec());
                    this.currentStateAction = true;
                }
                break;
            case LEAVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        this.director.handleTip(calculateTip());
                        this.director.clearElevatorSlot(this);
                        moveTo(this.director.getFloorSpawn(this.destFloor), this.personality.speedPixelSec());
                        this.currentStateAction = true;
                    } else {
                        this.director.despawn(this);
                    }
                }
                break;
            default:
                break;
        }
        
        float d = 1-PassengerDirector.HAPPINESS_DECAY_RATE_SEC;
        float mod = this.personality.patience() * PassengerDirector.HAPPINESS_DECAY_MOD[this.currentState.value];
        float decaySec = 1 - mod*d;
        this.happiness *= Math.pow(decaySec,deltaSec);
        
        super.update(deltaSec);
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PassengerState getState() {
        return this.currentState;
    }
    
    public int calculateTip() {
        int tip = Math.round(PassengerDirector.MAX_TIP_CENTS * 100/this.happiness * this.personality.generosity());
        System.out.println("end ride tip: happiness="+this.happiness+", generosity="+this.personality.generosity()+", tip="+tip);
        return tip;
    }

}
