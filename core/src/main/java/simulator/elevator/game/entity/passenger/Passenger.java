package simulator.elevator.game.entity.passenger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.game.manager.GameStateManager;
import simulator.elevator.game.manager.PassengerCoordinator;
import simulator.elevator.game.manager.SceneDirector;
import simulator.elevator.game.scene.PortraitType;
import simulator.elevator.game.scene.SceneType;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.line.StatementLineTree;
import simulator.elevator.util.RelativeCoordinate;

public class Passenger extends AbstractEntity {

    private final PassengerCoordinator coordinator;
    private final SceneDirector director;
    private final Level level;
    
    private float happiness = 100;
    private PassengerState currentState = PassengerState.ARRIVING;
    private boolean currentStateAction = false;
    
    private final int startFloor;
    private final int destFloor;
    private final Color color;
    private final StarRole starRole;
    private final PassengerPersonality personality;
    
    public Passenger(int startFloor, int destFloor,
                     Color color, Texture texture,
                     PassengerPersonality personality, StarRole starRole) {
        super(PassengerCoordinator.getInstance().getFloorSpawn(startFloor), texture);
        this.coordinator = PassengerCoordinator.getInstance();
        this.director = SceneDirector.getInstance();
        this.level = this.director.getLevel();
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
                            // this should never happen if the coordinator is doing its job
                            // but just in case....
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
                        SceneDirector.getInstance().requestScene(this, SceneType.GREETING);
                    } else if (!this.coordinator.arePeopleUnloading()) {
                        SceneDirector.getInstance().requestScene(this, SceneType.ELEVATOR_FULL);
                    }
                } else if (coordinator.isElevatorOpen() 
                        && coordinator.getElevatorClosestFloorDistance().first == this.startFloor) {
                    SceneDirector.getInstance().requestScene(this, SceneType.TOO_FAR);
                }
                break;
            case LOADING, UNLOADING:
                boolean isLoading = this.currentState == PassengerState.LOADING;
                int toFloor = isLoading ? this.startFloor : this.destFloor;
                
                float centerX = getPosition().getAbsoluteVector().x+this.level.PASSENGER_WIDTH_PIXEL/2;
                float doorDistance = Math.abs(centerX - this.level.DOOR_X_PIXEL);
                
                if (!this.coordinator.isElevatorAtFloor(toFloor)
                        && (!isLoading 
                                || this.level.PASSENGER_WIDTH_PIXEL > doorDistance)) {
                    // the elevator door was closed in the passenger's face
                    this.currentStateAction = false;
                    cancelMove();
                    this.currentState = isLoading ? PassengerState.WAITING : PassengerState.RIDING;
                    
                    float penalty = this.level.DOOR_SLAM_PENALTY * 1-this.personality.patience();
                    this.happiness = Math.max(0, this.happiness+penalty);
                    
                    // notify the managers
                    this.coordinator.clearElevatorSlot(this);
                    if (this.starRole != null)
                        this.director.ejectPassengerCurrentScene(this);
                    SceneDirector.getInstance().requestScene(this, SceneType.DOOR_SLAM);
                } else if (!this.isMoving()) {
                    this.currentStateAction = false;
                    if (isLoading) {
                        StatementLineTree requestFloor = new StatementLineTree(
                                PortraitType.NPC_NEUTRAL, null, "Floor "+(this.destFloor+1)+", please.", null);
                        this.director.queueInterrupt(this.color, requestFloor);
                        this.coordinator.clearWaitingSlot(this);
                        this.currentState = PassengerState.RIDING;
                    } else {
                        GameStateManager.getInstance().giveBonusTime();
                        this.currentState = PassengerState.LEAVING;
                        this.director.ejectPassengerCurrentScene(this);
                    }
                }
                break;
            case RIDING:
                if (this.coordinator.isElevatorAtFloor(this.destFloor)) {
                    this.currentState = PassengerState.UNLOADING;
                    moveTo(this.coordinator.getFloorExit(this.destFloor), this.personality.speedPixelSec());
                    this.currentStateAction = true;
                    SceneDirector.getInstance().requestScene(this, SceneType.GIVING_TIP);
                }
                break;
            case LEAVING:
                if (!this.isMoving()) {
                    if (!this.currentStateAction) {
                        this.coordinator.handleTip(calculateTip());
                        this.coordinator.clearElevatorSlot(this);
                        moveTo(this.coordinator.getFloorSpawn(this.destFloor),
                               this.personality.speedPixelSec());
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
        
        float oldHappiness = this.happiness;
        float d = 1-this.level.HAPPINESS_DECAY_RATE_SEC;
        float mod = (1-this.personality.patience()) * this.level.HAPPINESS_DECAY_MOD.get(this.currentState);
        float decaySec = Math.max(0, 1 - mod*d);
        this.happiness *= Math.pow(decaySec,deltaSec);
        if (oldHappiness >= 50 && this.happiness < 50 && this.currentState == PassengerState.RIDING)
            SceneDirector.getInstance().requestScene(this, SceneType.UNHAPPINESS_RIDING);
        
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
        float givingMood = this.happiness/100f * this.personality.generosity();
        int tip = Math.round(this.level.MAX_TIP_CENTS * givingMood);
        return tip;
    }

}
