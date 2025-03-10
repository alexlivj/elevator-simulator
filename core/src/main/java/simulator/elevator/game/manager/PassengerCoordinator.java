package simulator.elevator.game.manager;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.elevator.Level;
import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.util.RandomUtility;
import simulator.elevator.util.RelativeCoordinate;

public class PassengerCoordinator {
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private boolean firstPassenger = true;
    
    private final Passenger[][] waitingSlots= 
            new Passenger[getLevel().FLOOR_SPAWNS.size()][getLevel().MAX_PASSENGERS_FLOOR];
    private final Passenger[] elevatorSlots = new Passenger[getLevel().MAX_PASSENGERS_ELEVATOR];
    private final Passenger[] colorSlots = new Passenger[getLevel().MAX_PASSENGERS_WORLD];
    
    private static PassengerCoordinator instance;
    public static PassengerCoordinator getInstance() {
        if (instance == null)
            instance = new PassengerCoordinator();
        return instance;
    }
    
    private PassengerCoordinator() {
    }
    
    public void reset() {
        this.activePassengers.clear();
        
        for (int i=0; i<this.waitingSlots.length; i++)
            for (int j=0; j<this.waitingSlots[i].length; j++)
                this.waitingSlots[i][j] = null;

        for (int i=0; i<this.elevatorSlots.length; i++)
            this.elevatorSlots[i] = null;

        for (int i=0; i<this.colorSlots.length; i++)
            this.colorSlots[i] = null;
        
        this.firstPassenger = true;
    }
    
    public Level getLevel() {
        return GameStateManager.getInstance().getLevel();
    }
    
    public AbstractEntity managePassengers(float deltaSec) {
        AbstractEntity newPassenger = spawnPassengers(deltaSec);
        //TODO (?) if elevator is not at floor, shuffle waiting passengers to front of line
        return newPassenger;
    }
    
    public AbstractEntity spawnPassengers(float deltaSec) {
        Passenger newPassenger = null;
        
        if (this.activePassengers.size() < getLevel().MAX_PASSENGERS_WORLD
                && (this.firstPassenger 
                        || Math.random() < getLevel().SPAWN_OCCURRENCE_SEC * deltaSec)) {
            StarRole starRole = null;
            if (Math.random() < getLevel().SCENE_OCCURRENCE_SPAWN)
                starRole = SceneDirector.getInstance().requestStarScene();
            
            int leastBusyFloor = 0;
            if (!this.firstPassenger) {
                Map<Integer,Integer> floorNumWaiting = new HashMap<Integer,Integer>();
                for (int i=0; i<getLevel().FLOOR_SPAWNS.size(); i++)
                    floorNumWaiting.put(i, 0);
                for (Passenger p : this.activePassengers) {
                    if (p.getState().isBeforeOrAt(PassengerState.LOADING)) {
                        int startFloor = p.getStartFloor();
                        int numWaiting = floorNumWaiting.get(startFloor);
                        floorNumWaiting.put(startFloor, numWaiting+1);
                    }
                }
    
                leastBusyFloor = RandomUtility.getRandomIntRange(0,getLevel().FLOOR_SPAWNS.size()-1);
                boolean allFloorsFull = true;
                for (Integer floor : floorNumWaiting.keySet()) {
                    boolean maxWaiting =
                            getLevel().MAX_PASSENGERS_FLOOR <= floorNumWaiting.get(leastBusyFloor);
                    allFloorsFull = allFloorsFull && maxWaiting;
                    int numWaiting = maxWaiting ? Integer.MAX_VALUE : floorNumWaiting.get(leastBusyFloor); 
                    if (floorNumWaiting.get(floor) < numWaiting)
                        leastBusyFloor = floor;
                }
                if (allFloorsFull)
                    return null;
            }
            
            int randomDestFloor = RandomUtility.getRandomIntRange(0,getLevel().FLOOR_SPAWNS.size()-2);
            if (randomDestFloor >= leastBusyFloor)
                randomDestFloor++;
            
            Color color = new Color(Color.WHITE); // randomized later
            int randomTextureIndex =
                    RandomUtility.getRandomIntRange(0, getLevel().PASSENGER_TEXTURES.size()-1);
            Texture texture = getLevel().PASSENGER_TEXTURES.get(randomTextureIndex);
            
            int speed = RandomUtility.getRandomIntRange(getLevel().MIN_SPEED_PIXEL_SEC,
                    getLevel().MAX_SPEED_PIXEL_SEC);
            float patience = RandomUtility.getRandomRange(0f, 1f);
            float generosity = RandomUtility.getRandomRange(0f, 1f);
            PassengerPersonality personality = new PassengerPersonality(speed,patience,generosity);
            if (starRole != null)
                personality = starRole.requirements().bindPersonality(personality);
            
            newPassenger = new Passenger(leastBusyFloor, randomDestFloor,
                                         color, texture,
                                         personality, starRole);
            
            //TODO I should refactor how this works. this is not ideal!
            Color randomColor = requestColorSlot(newPassenger);
            if (randomColor != null)
                color.set(randomColor);
            if (starRole != null) {
                SceneDirector.getInstance().offerStarPassenger(newPassenger);
                System.out.println("spawning star role! "+randomColor+" on "+leastBusyFloor);
            }
            
            this.activePassengers.add(newPassenger);
            this.firstPassenger = false;
        }
        
        return newPassenger;
    }
    
    public Integer getElevatorCurrentFloor() {
        Integer floor = null;
        if (GameStateManager.getInstance().getElevator().isDoorOpen()) {
            Vector2 elevatorRelPos = GameStateManager.getInstance().getElevator().getRelativePosition();
            int closestFloor = 0;
            int distance = Integer.MAX_VALUE;
            for (int i=0; i<getLevel().FLOOR_SPAWNS.size(); i++) {
                float floorRelY = getLevel().FLOOR_SPAWNS.get(i).getRelativeVector().y;
                int diffLen = Math.round(Math.abs(elevatorRelPos.y-floorRelY));
                if (diffLen < distance) {
                    closestFloor = i;
                    distance = diffLen;
                }
            }
            if (distance < getLevel().ELEVATOR_FLOOR_BUFFER_PIXEL)
                floor = closestFloor;
        }
        return floor;
    }
    
    public boolean isElevatorAtFloor(int floor) {
        Integer currFloor = getElevatorCurrentFloor();
        if (currFloor != null && currFloor == floor)
            return true;
        return false;
    }
    
    public boolean arePeopleUnloading() {
        boolean hasUnloading = false;
        for (Passenger p : this.activePassengers)
            hasUnloading = hasUnloading || p.getState() == PassengerState.UNLOADING;
        return hasUnloading;
    }
    
    public RelativeCoordinate requestElevatorEntry(Passenger passenger) {
        RelativeCoordinate slotPos = null;
        
        int slot = -1;
        for (int i=0; i<this.elevatorSlots.length; i++) {
            if (this.elevatorSlots[i] == null) {
                slot = i;
                break;
            }
        }
        
        if (slot >= 0) {
            int rideBuffer = slot * getLevel().PASSENGER_WIDTH_PIXEL;
            int rideXPos = getLevel().DOOR_X_PIXEL + getLevel().RIDE_X_OFFSET_PIXEL + rideBuffer;
            slotPos = new RelativeCoordinate(GameStateManager.getInstance().getElevator().getPosition(),
                                             new Vector2(rideXPos,0));
            this.elevatorSlots[slot] = passenger;
        }
        
        return slotPos;
    }
    
    public RelativeCoordinate requestWaitingSlot(Passenger passenger) {
        RelativeCoordinate slotPos = null;
        
        int floor = passenger.getStartFloor();
        int slot = -1;
        for (int i=0; i<this.waitingSlots[floor].length; i++) {
            if (this.waitingSlots[floor][i] == null) {
                slot = i;
                break;
            }
        }
        
        int[] numWaiting = new int[this.waitingSlots.length];
        for (int i=0; i<this.waitingSlots.length; i++)
            for (int j=0; j<this.waitingSlots[i].length; j++)
                if (this.waitingSlots[i][j] != null)
                    numWaiting[i]++;

        if (slot >= 0) {
            slotPos = new RelativeCoordinate(passenger.getPosition());
            int waitBuffer = slot * getLevel().PASSENGER_WIDTH_PIXEL;
            int waitXPos = getLevel().DOOR_X_PIXEL + getLevel().WAIT_X_OFFSET_PIXEL - waitBuffer;
            slotPos.getRelativeVector().x = waitXPos;
            this.waitingSlots[floor][slot] = passenger;
        }
        
        return slotPos;
    }
    
    public Color requestColorSlot(Passenger passenger) {
        Color color = null;
        
        int slot = -1;
        for (int i=0; i<this.colorSlots.length; i++) {
            if (this.colorSlots[i] == null) {
                slot = i;
                break;
            }
        }
        
        if (slot >= 0) {
            color = getLevel().POSSIBLE_PASSENGER_COLORS[slot];
            this.colorSlots[slot] = passenger;
        }
        
        return color;
    }
    
    public void clearElevatorSlot(Passenger passenger) {
        for (int i=0; i<this.elevatorSlots.length; i++)
            if (this.elevatorSlots[i] == passenger)
                this.elevatorSlots[i] = null;
    }
    
    public void clearWaitingSlot(Passenger passenger) {
        for (int i=0; i<this.waitingSlots.length; i++)
            for (int j=0; j<this.waitingSlots[i].length; j++)
                if (this.waitingSlots[i][j] == passenger)
                    this.waitingSlots[i][j] = null;
    }
    
    public void clearColorSlot(Passenger passenger) {
        for (int i=0; i<this.colorSlots.length; i++)
            if (this.colorSlots[i] == passenger)
                this.colorSlots[i] = null;
    }
    
    public RelativeCoordinate getFloorSpawn(int floor) {
        return new RelativeCoordinate(getLevel().FLOOR_SPAWNS.get(floor));
    }
    
    public RelativeCoordinate getFloorExit(int floor) {
        RelativeCoordinate exit = new RelativeCoordinate(getLevel().FLOOR_SPAWNS.get(floor));
        exit.getRelativeVector().x = getLevel().DOOR_X_PIXEL + getLevel().WAIT_X_OFFSET_PIXEL;
        return exit;
    }
    
    public void handleTip(int tipCents) {
        int newTip = Math.max(0,tipCents-1); // director's fee :P
        GameStateManager.getInstance().getElevator().giveTip(newTip);
    }
    
    public void despawn(Passenger passenger) {
        clearWaitingSlot(passenger);
        clearElevatorSlot(passenger);
        clearColorSlot(passenger);
        this.activePassengers.remove(passenger);
        GameStateManager.getInstance().despawnEntity(passenger);
    }

}
