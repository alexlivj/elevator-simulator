package simulator.elevator.game.entity.passenger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.Main;
import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.game.manager.PassengerCoordinator;
import simulator.elevator.game.manager.SceneDirector;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.script.StatementLineTree;
import simulator.elevator.game.scene.script.SceneType;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

public class Passenger extends AbstractEntity {

    private static final Texture DEF_TEXTURE = TextureUtility.doubleTextureSize("passenger.png");
    public static final float HAPPINESS_DECAY_RATE_SEC = 0.99f;
    public static final float[] HAPPINESS_DECAY_MOD = new float[6];
    static {
        HAPPINESS_DECAY_MOD[PassengerState.ARRIVING.value] = 0f;
        HAPPINESS_DECAY_MOD[PassengerState.WAITING.value] = 1f;
        HAPPINESS_DECAY_MOD[PassengerState.LOADING.value] = 1.5f;
        HAPPINESS_DECAY_MOD[PassengerState.RIDING.value] = 0.5f;
        HAPPINESS_DECAY_MOD[PassengerState.UNLOADING.value] = 1.5f;
        HAPPINESS_DECAY_MOD[PassengerState.LEAVING.value] = 0f;
    }
    public static final float DOOR_SLAM_PENALTY = -50;
    public static final int MAX_TIP_CENTS = 20;

    private final PassengerCoordinator coordinator;
    private final SceneDirector director;
    
    private float happiness = 100;
    private PassengerState currentState = PassengerState.ARRIVING;
    private boolean currentStateAction = false;
    
    private final int startFloor;
    private final int destFloor;
    private final Color color;
    private final StarRole starRole;
    private final PassengerPersonality personality;
    
    public Passenger(int startFloor, int destFloor,
                     Color color, 
                     PassengerPersonality personality, StarRole starRole) {
        super(PassengerCoordinator.getInstance().getFloorSpawn(startFloor), Passenger.DEF_TEXTURE);
        this.coordinator = PassengerCoordinator.getInstance();
        this.director = SceneDirector.getInstance();
        this.startFloor = startFloor;
        this.destFloor = destFloor;
        this.color = color;
        this.starRole = starRole;
        this.personality = personality;
    }
    
    @Override
    public void update(float deltaSec) {
        PassengerState oldState = this.currentState;
        switch (this.currentState) {
            case ARRIVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        RelativeCoordinate waitingSlot = coordinator.requestWaitingSlot(this);
                        if (waitingSlot == null) {
                            // this should never happen, but just in case....
                            coordinator.despawn(this);
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
                if (coordinator.isElevatorAtFloor(this.startFloor)) {
                    RelativeCoordinate elevatorSlot = coordinator.requestElevatorEntry(this);
                    if (elevatorSlot != null) {
                        this.currentState = PassengerState.LOADING;
                        moveTo(elevatorSlot, this.personality.speedPixelSec());
                        this.currentStateAction = true;
                    } else if (!this.coordinator.arePeopleUnloading()) {
                        SceneDirector.getInstance().requestScene(this, SceneType.ELEVATOR_FULL);
                    }
                }
                break;
            case LOADING, UNLOADING:
                boolean isLoading = this.currentState == PassengerState.LOADING;
                int toFloor = isLoading ? this.startFloor : this.destFloor;
                if (!this.coordinator.isElevatorAtFloor(toFloor)) {
                    //TODO write more sophisticated "door slammed in face" checking
                    // ideally, we'd only want the reset to happen if the door intersects with the passsenger
                    this.currentStateAction = false;
                    cancelMove();
                    float penalty = Passenger.DOOR_SLAM_PENALTY * 1-this.personality.patience();
                    this.happiness = Math.max(0, this.happiness+penalty);
                    this.coordinator.clearElevatorSlot(this);
                    if (this.starRole != null)
                        this.director.ejectPassengerCurrentScene(this);
                    //TODO some sort of "wth bro" scene, if director commands
                    // maybe we express indignation, and if the director approve, we use personality to select a scene
                    this.currentState = isLoading ? PassengerState.WAITING : PassengerState.RIDING;
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    if (isLoading) {
                        StatementLineTree requestFloor = new StatementLineTree(
                                null, null, "Floor "+(this.destFloor+1)+", please.", null);
                        this.director.queueInterrupt(requestFloor);
                        this.coordinator.clearWaitingSlot(this);
                        this.currentState = PassengerState.RIDING;
                    } else {
                        this.currentState = PassengerState.LEAVING;
                    }
                }
                break;
            case RIDING:
                if (this.coordinator.isElevatorAtFloor(this.destFloor)) {
                    this.currentState = PassengerState.UNLOADING;
                    moveTo(this.coordinator.getFloorExit(this.destFloor), this.personality.speedPixelSec());
                    this.currentStateAction = true;
                }
                break;
            case LEAVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        this.coordinator.handleTip(calculateTip());
                        this.coordinator.clearElevatorSlot(this);
                        moveTo(this.coordinator.getFloorSpawn(this.destFloor), this.personality.speedPixelSec());
                        this.currentStateAction = true;
                    } else {
                        this.director.removePassengerScenes(this);
                        this.coordinator.despawn(this);
                    }
                }
                break;
            default:
                break;
        }
        
        if (this.starRole != null 
                && oldState != this.currentState 
                && oldState.isBeforeOrAt(this.currentState)) {
            this.director.readyStarScene(currentState);
        }
        
        float d = 1-Passenger.HAPPINESS_DECAY_RATE_SEC;
        float mod = this.personality.patience() * Passenger.HAPPINESS_DECAY_MOD[this.currentState.value];
        float decaySec = 1 - mod*d;
        this.happiness *= Math.pow(decaySec,deltaSec);
        
        super.update(deltaSec);
    }
    
    @Override
    public void render(Main game) {
        game.batch.setColor(this.color);
        super.render(game);
        game.batch.setColor(Color.WHITE);
    }
    
    public Color getColor() {
        return this.color;
    }
    
    public int getStartFloor() {
        return this.startFloor;
    }
    
    public PassengerPersonality getPersonality() {
        return this.personality;
    }
    
    public PassengerState getState() {
        return this.currentState;
    }
    
    public float getHappiness() {
        return this.happiness;
    }
    
    public void modHappiness(float mod) {
        this.happiness *= mod;
    }
    
    public int calculateTip() {
        float givingMood = 100/this.happiness * this.personality.generosity();
        int tip = Math.round(Passenger.MAX_TIP_CENTS * givingMood);
        return tip;
    }

}
