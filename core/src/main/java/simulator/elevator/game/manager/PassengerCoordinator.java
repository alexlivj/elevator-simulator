package simulator.elevator.game.manager;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.util.RandomUtility;
import simulator.elevator.util.RelativeCoordinate;

public class PassengerCoordinator {

    //TODO maybe read these from somewhere
    private static final int MAX_PASSENGERS_WORLD = 8;
    private static final int MAX_PASSENGERS_FLOOR = 3;
    private static final int MAX_PASSENGERS_ELEVATOR = 2;
    private static final int ELEVATOR_FLOOR_BUFFER_PIXEL = 10;
    private static final int PASSENGER_WIDTH_PIXEL = 16*2;
    private static final int WAIT_X_OFFSET_PIXEL = 245*2;
    private static final int RIDE_X_OFFSET_PIXEL = 277*2;
    private static final float SPAWN_OCCURRENCE_SEC = 0.3f;
    private static final float SCENE_OCCURRENCE_SPAWN = 0.3f;
    private static final int MIN_SPEED_PIXEL_SEC = 40;
    private static final int MAX_SPEED_PIXEL_SEC = 60;
    private static final Color[] POSSIBLE_COLORS = new Color[PassengerCoordinator.MAX_PASSENGERS_WORLD];
    static {
        float h = 0f;
        float s = 0.15f;
        float v = 0.88f;
        
        int shift = RandomUtility.getRandomIntRange(0, MAX_PASSENGERS_WORLD-1);
        for (int i=0; i<MAX_PASSENGERS_WORLD; i++)
            PassengerCoordinator.POSSIBLE_COLORS[(i+shift) % MAX_PASSENGERS_WORLD] =
                    hsvToRgb(h+((float)i)/((float)MAX_PASSENGERS_WORLD),s,v);
    }
    private static Color hsvToRgb (float h, float s, float v) {
        float r = 1f;
        float g = 1f;
        float b = 1f;
        
        int i = (int) Math.floor(h*6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        
        switch (i % 6) {
        case 0:
            r = v;
            g = t;
            b = p;
            break;
        case 1:
            r = q;
            g = v;
            b = p;
            break;
        case 2:
            r = p;
            g = v;
            b = t;
            break;
        case 3:
            r = p;
            g = q;
            b = v;
            break;
        case 4:
            r = t;
            g = p;
            b = v;
            break;
        case 5:
            r = v;
            g = p; 
            b = q;
            break;
        }
        
        return new Color(r, g, b, 1f);
    }
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private boolean firstPassenger = true;
    
    private final Passenger[][] waitingSlots= 
            new Passenger[GameStateManager.FLOOR_SPAWNS.size()][PassengerCoordinator.MAX_PASSENGERS_FLOOR];
    private final Passenger[] elevatorSlots = new Passenger[PassengerCoordinator.MAX_PASSENGERS_ELEVATOR];
    private final Passenger[] colorSlots = new Passenger[PassengerCoordinator.MAX_PASSENGERS_WORLD];
    
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
    
    public AbstractEntity managePassengers(float deltaSec) {
        AbstractEntity newPassenger = spawnPassengers(deltaSec);
        //TODO (?) if elevator is not at floor, shuffle waiting passengers to front of line
        return newPassenger;
    }
    
    public AbstractEntity spawnPassengers(float deltaSec) {
        Passenger newPassenger = null;
        
        if (this.activePassengers.size() < MAX_PASSENGERS_WORLD
                && (this.firstPassenger 
                        || Math.random() < PassengerCoordinator.SPAWN_OCCURRENCE_SEC * deltaSec)) {
            StarRole starRole = null;
            if (Math.random() < PassengerCoordinator.SCENE_OCCURRENCE_SPAWN)
                starRole = SceneDirector.getInstance().requestStarScene();
            
            int leastBusyFloor = 0;
            if (!this.firstPassenger) {
                Map<Integer,Integer> floorNumWaiting = new HashMap<Integer,Integer>();
                for (int i=0; i<GameStateManager.FLOOR_SPAWNS.size(); i++)
                    floorNumWaiting.put(i, 0);
                for (Passenger p : this.activePassengers) {
                    if (p.getState().isBeforeOrAt(PassengerState.LOADING)) {
                        int startFloor = p.getStartFloor();
                        int numWaiting = floorNumWaiting.get(startFloor);
                        floorNumWaiting.put(startFloor, numWaiting+1);
                    }
                }
    
                leastBusyFloor = RandomUtility.getRandomIntRange(0,GameStateManager.FLOOR_SPAWNS.size()-1);
                boolean allFloorsFull = true;
                for (Integer floor : floorNumWaiting.keySet()) {
                    boolean maxWaiting =
                            PassengerCoordinator.MAX_PASSENGERS_FLOOR <= floorNumWaiting.get(leastBusyFloor);
                    allFloorsFull = allFloorsFull && maxWaiting;
                    int numWaiting = maxWaiting ? Integer.MAX_VALUE : floorNumWaiting.get(leastBusyFloor); 
                    if (floorNumWaiting.get(floor) < numWaiting)
                        leastBusyFloor = floor;
                }
                if (allFloorsFull)
                    return null;
            }
            
            int randomDestFloor = RandomUtility.getRandomIntRange(0,GameStateManager.FLOOR_SPAWNS.size()-2);
            if (randomDestFloor >= leastBusyFloor)
                randomDestFloor++;
            
            //TODO get random color
            Color color = new Color(Color.WHITE);
            
            int speed = RandomUtility.getRandomIntRange(PassengerCoordinator.MIN_SPEED_PIXEL_SEC,
                                                        PassengerCoordinator.MAX_SPEED_PIXEL_SEC);
            float patience = RandomUtility.getRandomRange(0f, 1f);
            float generosity = RandomUtility.getRandomRange(0f, 1f);
            PassengerPersonality personality = new PassengerPersonality(speed,patience,generosity);
            if (starRole != null)
                personality = starRole.requirements().bindPersonality(personality);
            
            newPassenger = new Passenger(leastBusyFloor, randomDestFloor,
                                         color,
                                         personality, starRole);
            Color randomColor = requestColorSlot(newPassenger);
            if (randomColor != null)
                color.set(randomColor);
            
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
            for (int i=0; i<GameStateManager.FLOOR_SPAWNS.size(); i++) {
                float floorRelY = GameStateManager.FLOOR_SPAWNS.get(i).getRelativeVector().y;
                int diffLen = Math.round(Math.abs(elevatorRelPos.y-floorRelY));
                if (diffLen < distance) {
                    closestFloor = i;
                    distance = diffLen;
                }
            }
            if (distance < PassengerCoordinator.ELEVATOR_FLOOR_BUFFER_PIXEL)
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
            int rideBuffer = slot * PassengerCoordinator.PASSENGER_WIDTH_PIXEL;
            int rideXPos = PassengerCoordinator.RIDE_X_OFFSET_PIXEL + rideBuffer;
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
            int waitBuffer = slot * PassengerCoordinator.PASSENGER_WIDTH_PIXEL;
            int waitXPos = PassengerCoordinator.WAIT_X_OFFSET_PIXEL - waitBuffer;
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
            color = PassengerCoordinator.POSSIBLE_COLORS[slot];
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
        return new RelativeCoordinate(GameStateManager.FLOOR_SPAWNS.get(floor));
    }
    
    public RelativeCoordinate getFloorExit(int floor) {
        RelativeCoordinate exit = new RelativeCoordinate(GameStateManager.FLOOR_SPAWNS.get(floor));
        exit.getRelativeVector().x = PassengerCoordinator.WAIT_X_OFFSET_PIXEL;
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
