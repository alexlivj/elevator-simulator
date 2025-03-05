package simulator.elevator.game;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.game.entity.Passenger;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

//TODO does this need to exist? or should these functionalities be absorbed by GameStateManager?
public class PassengerDirector {

    private final List<RelativeCoordinate> floorSpawns;
    private final int waitX;
    private final List<Scene> scenes;
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private Passenger scenePassenger = null;

    //TODO maybe read these from somewhere
    private static final int MAX_PASSENGERS_WORLD = 8;
    private static final float SPAWN_OCCURRENCE_SEC = 0.3f;
    private static final float SCENE_OCCURRENCE_SPAWN = 0.3f;
    private static final Texture DEF_PASSENGER_TEXTURE = TextureUtility.doubleTextureSize("passenger.png");
    private static final int MIN_SPEED_PIXEL_SEC = 20;
    private static final int MAX_SPEED_PIXEL_SEC = 40;
    
    public PassengerDirector(List<RelativeCoordinate> floorSpawns, int waitX, List<Scene> scenes) {
        this.floorSpawns = floorSpawns;
        this.waitX = waitX;
        this.scenes = scenes;
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
                if (p.getState() == Passenger.PState.WAITING) {
                    int startFloor = p.getStartFloor();
                    int numWaiting = floorNumWaiting.get(startFloor);
                    floorNumWaiting.put(startFloor, numWaiting);
                }
            }
            
            int leastBusyFloor = (int)(Math.round(Math.random() * (this.floorSpawns.size()-1)));
            for (Integer floor : floorNumWaiting.keySet())
                if (floorNumWaiting.get(floor) < floorNumWaiting.get(leastBusyFloor))
                    leastBusyFloor = floor;
            
            //TODO put this random generator in its own util? why doesn't java have a better built-in smh
            int randomDestFloor = (int)(Math.round(Math.random() * (this.floorSpawns.size()-2)));
            if (randomDestFloor >= leastBusyFloor)
                randomDestFloor++;
            
            //TODO get random texture that matches scene, if applicable
            Texture texture = PassengerDirector.DEF_PASSENGER_TEXTURE;
            
            //TODO randomly generate other stats
            
            newPassenger = new Passenger(this.floorSpawns.get(leastBusyFloor),
                                         leastBusyFloor, randomDestFloor,
                                         texture, newScene,
                                         waitX, PassengerDirector.MAX_SPEED_PIXEL_SEC); 
            //TODO randomize speed
            
            this.activePassengers.add(newPassenger);
            if (newScene != null)
                this.scenePassenger = newPassenger;
        }
        
        return newPassenger;
    }

}
