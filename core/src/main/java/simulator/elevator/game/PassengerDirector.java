package simulator.elevator.game;

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
    private final int firstWaitXPos;
    private final int firstRidingXPos;
    private final List<Scene> scenes;
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private Passenger scenePassenger = null;
    private Passenger[] elevatorSlots = new Passenger[MAX_PASSENGERS_ELEVATOR];

    //TODO maybe read these from somewhere
    private static final int MAX_PASSENGERS_WORLD = 1;
    private static final int MAX_PASSENGERS_FLOOR = 3;
    private static final int MAX_PASSENGERS_ELEVATOR = 2;
    private static final int PASSENGER_WIDTH_PIXEL = 16*2;
    private static final float SPAWN_OCCURRENCE_SEC = 0.3f;
    private static final float SCENE_OCCURRENCE_SPAWN = 0.3f;
    private static final Texture DEF_PASSENGER_TEXTURE = TextureUtility.doubleTextureSize("passenger.png");
    private static final int MIN_SPEED_PIXEL_SEC = 20;
    private static final int MAX_SPEED_PIXEL_SEC = 40;
    private static final int ELEVATOR_FLOOR_BUFFER_PIXEL = 20;
    
    public PassengerDirector(Elevator elevator,
                             List<RelativeCoordinate> floorSpawns,
                             int firstWaitXPos, int firstRidingXPos,
                             List<Scene> scenes) {
        this.elevator = elevator;
        this.floorSpawns = floorSpawns;
        this.firstWaitXPos = firstWaitXPos;
        this.firstRidingXPos = firstRidingXPos;
        this.scenes = scenes;
    }
    
    public LinearEntity managePassengers(float deltaSec) {
        LinearEntity newPassenger = spawnPassengers(deltaSec);
        //TODO if elevator is not at floor, shuffle waiting passengers to front of line
        return newPassenger;
    }
    
    public LinearEntity spawnPassengers(float deltaSec) {
        Passenger newPassenger = null;
        
        if (this.activePassengers.size() < MAX_PASSENGERS_WORLD
                && Math.random() < PassengerDirector.SPAWN_OCCURRENCE_SEC * deltaSec) {
            
            Scene newScene = null;
            if (this.scenePassenger == null) {
                if (this.scenes.size() > 0 && Math.random() < PassengerDirector.SCENE_OCCURRENCE_SPAWN)
                    newScene = this.scenes.remove((int)(Math.round(Math.random() * (this.scenes.size()-1))));
            }
            
            Map<Integer,Integer> floorNumWaiting = new HashMap<Integer,Integer>();
            for (int i=0; i<floorSpawns.size(); i++)
                floorNumWaiting.put(i, 0);
            for (Passenger p : this.activePassengers) {
                if (p.getState().isBeforeOrAt(Passenger.PState.WAITING)) {
                    int startFloor = p.getStartFloor();
                    int numWaiting = floorNumWaiting.get(startFloor);
                    floorNumWaiting.put(startFloor, numWaiting+1);
                }
            }
            
            int leastBusyFloor = (int)(Math.round(Math.random() * (this.floorSpawns.size()-1)));
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
            int randomDestFloor = (int)(Math.round(Math.random() * (this.floorSpawns.size()-2)));
            if (randomDestFloor >= leastBusyFloor)
                randomDestFloor++;
            
            //TODO get random texture that matches scene, if applicable
            Texture texture = PassengerDirector.DEF_PASSENGER_TEXTURE;
            
            //TODO randomly generate other stats
            int speed = PassengerDirector.MAX_SPEED_PIXEL_SEC;
            
            //TODO we need to use wait slots, like how we do the elevator
            int waitBuffer = floorNumWaiting.get(leastBusyFloor) * PassengerDirector.PASSENGER_WIDTH_PIXEL;
            int waitXPos = this.firstWaitXPos - waitBuffer;
            
            newPassenger = new Passenger(this,
                                         leastBusyFloor, randomDestFloor,
                                         texture, newScene,
                                         waitXPos, speed);
            
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
            int rideXPos = this.firstRidingXPos + rideBuffer;
            slotPos = new RelativeCoordinate(this.elevator.getPosition(),
                                             new Vector2(rideXPos,0));
            this.elevatorSlots[slot] = passenger;
        }
        
        return slotPos;
    }
    
    public void clearElevatorSlot(Passenger passenger) {
        for (int i=0; i<this.elevatorSlots.length; i++)
            if (this.elevatorSlots[i] == passenger)
                this.elevatorSlots[i] = null;
    }
    
    public RelativeCoordinate getFloorSpawn(int floor) {
        return new RelativeCoordinate(this.floorSpawns.get(floor));
    }
    
    public RelativeCoordinate getFloorExit(int floor) {
        RelativeCoordinate exit = new RelativeCoordinate(this.floorSpawns.get(floor));
        exit.getRelativeVector().x = this.firstWaitXPos;
        return exit;
    }

}
