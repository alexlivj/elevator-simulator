package simulator.elevator.game.manager;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.game.entity.Passenger;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

public class PassengerDirector {

    private final Elevator elevator;
    private final List<RelativeCoordinate> floorSpawns;
    private final List<Scene> scenes;
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private Passenger scenePassenger = null;
    private Passenger[][] waitingSlots;
    private Passenger[] elevatorSlots;

    //TODO maybe read these from somewhere
    private static final int MAX_PASSENGERS_WORLD = 8;
    private static final int MAX_PASSENGERS_FLOOR = 3;
    private static final int MAX_PASSENGERS_ELEVATOR = 2;
    private static final int ELEVATOR_FLOOR_BUFFER_PIXEL = 10;
    private static final int PASSENGER_WIDTH_PIXEL = 16*2;
    private static final int WAIT_X_OFFSET_PIXEL = 245*2;
    private static final int RIDE_X_OFFSET_PIXEL = 277*2;
    private static final float SPAWN_OCCURRENCE_SEC = 1f;
    private static final float SCENE_OCCURRENCE_SPAWN = 0.3f;
    private static final Texture DEF_PASSENGER_TEXTURE = TextureUtility.doubleTextureSize("passenger.png");
    public static final float HAPPINESS_DECAY_RATE_SEC = 0.99f;
    public static final float[] HAPPINESS_DECAY_MOD = new float[6];
    static {
        HAPPINESS_DECAY_MOD[Passenger.PState.ARRIVING.value] = 0f;
        HAPPINESS_DECAY_MOD[Passenger.PState.WAITING.value] = 1f;
        HAPPINESS_DECAY_MOD[Passenger.PState.LOADING.value] = 1.5f;
        HAPPINESS_DECAY_MOD[Passenger.PState.RIDING.value] = 0.5f;
        HAPPINESS_DECAY_MOD[Passenger.PState.UNLOADING.value] = 1.5f;
        HAPPINESS_DECAY_MOD[Passenger.PState.LEAVING.value] = 0f;
    }
    private static final int MIN_SPEED_PIXEL_SEC = 20;
    private static final int MAX_SPEED_PIXEL_SEC = 40;
    private static final float MIN_PATIENCE = 0.8f;
    private static final float MIN_GENEROSITY = 0.8f;
    //NOTE max patience and generosity is just 1f, for easier balancing
    public static final int MAX_TIP_CENTS = 10;
    
    public PassengerDirector(Elevator elevator,
                             List<RelativeCoordinate> floorSpawns,
                             List<Scene> scenes) {
        this.elevator = elevator;
        this.floorSpawns = floorSpawns;
        this.scenes = scenes;
        
        this.waitingSlots = new Passenger[floorSpawns.size()][PassengerDirector.MAX_PASSENGERS_FLOOR];
        this.elevatorSlots = new Passenger[PassengerDirector.MAX_PASSENGERS_ELEVATOR];
    }
    
    public LinearEntity managePassengers(float deltaSec) {
        LinearEntity newPassenger = spawnPassengers(deltaSec);
        //TODO (?) if elevator is not at floor, shuffle waiting passengers to front of line
        return newPassenger;
    }
    
    public LinearEntity spawnPassengers(float deltaSec) {
        Passenger newPassenger = null;
        
        if (this.activePassengers.size() < MAX_PASSENGERS_WORLD
                && Math.random() < PassengerDirector.SPAWN_OCCURRENCE_SEC * deltaSec) {
            
            Scene newScene = null;
            if (this.scenePassenger == null) {
                if (this.scenes.size() > 0 && Math.random() < PassengerDirector.SCENE_OCCURRENCE_SPAWN)
                    newScene = this.scenes.remove(Math.round(getRandomRange(0,this.scenes.size()-1)));
            }
            
            Map<Integer,Integer> floorNumWaiting = new HashMap<Integer,Integer>();
            for (int i=0; i<floorSpawns.size(); i++)
                floorNumWaiting.put(i, 0);
            for (Passenger p : this.activePassengers) {
                if (p.getState().isBeforeOrAt(Passenger.PState.LOADING)) {
                    int startFloor = p.getStartFloor();
                    int numWaiting = floorNumWaiting.get(startFloor);
                    floorNumWaiting.put(startFloor, numWaiting+1);
                }
            }

            int leastBusyFloor = Math.round(getRandomRange(0,this.floorSpawns.size()-1));
            boolean allFloorsFull = true;
            for (Integer floor : floorNumWaiting.keySet()) {
                boolean maxWaiting =
                        PassengerDirector.MAX_PASSENGERS_FLOOR <= floorNumWaiting.get(leastBusyFloor);
                allFloorsFull = allFloorsFull && maxWaiting;
                int numWaiting = maxWaiting ? Integer.MAX_VALUE : floorNumWaiting.get(leastBusyFloor); 
                if (floorNumWaiting.get(floor) < numWaiting)
                    leastBusyFloor = floor;
            }
            if (allFloorsFull)
                return null;
            
            //TODO put this random generator in its own util? why doesn't java have a better built-in smh
            int randomDestFloor = Math.round(getRandomRange(0,this.floorSpawns.size()-2));
            if (randomDestFloor >= leastBusyFloor)
                randomDestFloor++;
            
            //TODO get random texture that matches scene, if applicable
            Texture texture = PassengerDirector.DEF_PASSENGER_TEXTURE;
            
            int speed = Math.round(getRandomRange(PassengerDirector.MIN_SPEED_PIXEL_SEC,
                                                  PassengerDirector.MAX_SPEED_PIXEL_SEC));
            float patience = getRandomRange(PassengerDirector.MIN_PATIENCE, 1f);
            float generosity = getRandomRange(PassengerDirector.MIN_GENEROSITY, 1f);
            Passenger.Personality personality = new Passenger.Personality(speed,patience,generosity);
            
            newPassenger = new Passenger(this,
                                         leastBusyFloor, randomDestFloor,
                                         texture, newScene,
                                         personality);
            
            this.activePassengers.add(newPassenger);
            if (newScene != null)
                this.scenePassenger = newPassenger;
        }
        
        return newPassenger;
    }
    
    public Integer getElevatorCurrentFloor() {
        Integer floor = null;
        if (this.elevator.isDoorOpen()) {
            Vector2 elevatorRelPos = this.elevator.getRelativePosition();
            int closestFloor = 0;
            int distance = Integer.MAX_VALUE;
            for (int i=0; i<this.floorSpawns.size(); i++) {
                int diffLen = Math.round(Math.abs(elevatorRelPos.y-this.floorSpawns.get(i).getRelativeVector().y));
                if (diffLen < distance) {
                    closestFloor = i;
                    distance = diffLen;
                }
            }
            if (distance < PassengerDirector.ELEVATOR_FLOOR_BUFFER_PIXEL)
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
            int rideBuffer = slot * PassengerDirector.PASSENGER_WIDTH_PIXEL;
            int rideXPos = PassengerDirector.RIDE_X_OFFSET_PIXEL + rideBuffer;
            slotPos = new RelativeCoordinate(this.elevator.getPosition(),
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
            int waitBuffer = slot * PassengerDirector.PASSENGER_WIDTH_PIXEL;
            int waitXPos = PassengerDirector.WAIT_X_OFFSET_PIXEL - waitBuffer;
            slotPos.getRelativeVector().x = waitXPos;
            this.waitingSlots[floor][slot] = passenger;
        }
        
        return slotPos;
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
    public RelativeCoordinate getFloorSpawn(int floor) {
        return new RelativeCoordinate(this.floorSpawns.get(floor));
    }
    
    public RelativeCoordinate getFloorExit(int floor) {
        RelativeCoordinate exit = new RelativeCoordinate(this.floorSpawns.get(floor));
        exit.getRelativeVector().x = PassengerDirector.WAIT_X_OFFSET_PIXEL;
        return exit;
    }
    
    public void handleTip(int tipCents) {
        int newTip = Math.max(0,tipCents-1); // director's fee :P
        this.elevator.giveTip(newTip);
    }
    
    public void despawn(Passenger passenger) {
        clearWaitingSlot(passenger);
        clearElevatorSlot(passenger);
        this.activePassengers.remove(passenger);
        GameStateManager.getInstance().despawnEntity(passenger);
    }
    
    private float getRandomRange(float first, float second) {
        return (float)(first + Math.random()*(second-first));
    }

}
