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
        
        private final int value;
        private static final float[] HAPPINESS_DECAY_MOD = new float[6];
        static {
            HAPPINESS_DECAY_MOD[ARRIVING.value] = 0f;
            HAPPINESS_DECAY_MOD[WAITING.value] = 1f;
            HAPPINESS_DECAY_MOD[LOADING.value] = 1.5f;
            HAPPINESS_DECAY_MOD[RIDING.value] = 0.5f;
            HAPPINESS_DECAY_MOD[UNLOADING.value] = 1.5f;
            HAPPINESS_DECAY_MOD[LEAVING.value] = 0f;
        }
        
        private PState(int value) {
            this.value = value;
        }
        
        public boolean isBeforeOrAt(PState p) {
            return this.value <= p.value;
        }
        
        public float modHappiness(float deltaSec, float happiness) {
            float d = 1-PassengerDirector.HAPPINESS_DECAY_RATE_SEC;
            float decaySec = 1 - HAPPINESS_DECAY_MOD[this.value]*d;
            return (float) (happiness * Math.pow(decaySec,deltaSec));
        }
    }
    
    private final PassengerDirector director;
    
    private float happiness = 100;
    private PState currentState = PState.ARRIVING;
    private boolean currentStateAction = false;
    private final int startFloor;
    private final int destFloor;
    private final Scene scene;
    
    private final int speedPixelSec;
    private float patience;
    private float generosity;
    
    public Passenger(PassengerDirector director,
                     int startFloor, int destFloor,
                     Texture texture, Scene scene,
                     int speedPixelSec) {
        super(director.getFloorSpawn(startFloor), texture);
        this.director = director;
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.scene = scene;
        this.speedPixelSec = speedPixelSec;
    }
    
    @Override
    public void update(float deltaSec) {
        switch (this.currentState) {
            case ARRIVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        RelativeCoordinate waitingSlot = director.requestWaitingSlot(this);
                        if (waitingSlot == null) {
                            //TODO do this better. this feels bandaidy
                            director.despawn(this);
                        } else {
                            moveTo(waitingSlot, speedPixelSec);
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
                        moveTo(elevatorSlot, this.speedPixelSec);
                        this.currentStateAction = true;
                    }
                    //TODO some sort of "elevator full" scene, if director commands
                }
                break;
            case LOADING, UNLOADING:
                boolean isLoading = this.currentState == PState.LOADING;
                int toFloor = isLoading ? this.startFloor : this.destFloor;
                //TODO write more sophisticated "door slammed in face" code handling
                if (!director.isElevatorAtFloor(toFloor)) {
                    this.currentStateAction = false;
                    cancelMove();
                    director.clearElevatorSlot(this);
                    this.currentState = isLoading ? PState.WAITING : PState.RIDING;
                    //TODO some sort of "wth bro" scene, if director commands
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    currentState = isLoading ? PState.RIDING : PState.LEAVING;
                    if (isLoading)
                        director.clearWaitingSlot(this);
                    //TODO if loading, tell the player which floor where to go with a scene
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
                        System.out.println("end ride: happiness="+this.happiness);
                        director.clearElevatorSlot(this);
                        moveTo(director.getFloorSpawn(this.destFloor), speedPixelSec);
                        this.currentStateAction = true;
                    } else {
                        director.despawn(this);
                    }
                }
                break;
            default:
                break;
        }
        this.happiness = this.currentState.modHappiness(deltaSec, this.happiness);
        
        super.update(deltaSec);
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PState getState() {
        return this.currentState;
    }

}
