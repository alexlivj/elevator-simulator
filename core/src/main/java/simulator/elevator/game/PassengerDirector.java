package simulator.elevator.game;

import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.game.entity.Passenger;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.TextureUtility;

public class PassengerDirector {

    private final List<RelativeCoordinate> floorSpawns;
    private final List<Scene> scenes;
    
    private List<Passenger> activePassengers = new ArrayList<Passenger>();
    private Passenger scenePassenger = null;

    private static final float SPAWN_OCCURRENCE_SEC = 0.3f;
    private static final float SCENE_OCCURRENCE_SPAWN = 0.3f;
    private static final Texture DEF_PASSENGER_TEXTURE = TextureUtility.doubleTextureSize("passenger.png");
    
    public PassengerDirector(List<RelativeCoordinate> floorSpawns, List<Scene> scenes) {
        this.floorSpawns = floorSpawns;
        this.scenes = scenes;
    }
    
    public LinearEntity directPassengers(float deltaSec) {
        Passenger newPassenger = null;
        
        if (Math.random() < PassengerDirector.SPAWN_OCCURRENCE_SEC / deltaSec) {
            Scene newScene = null;
            if (this.scenePassenger == null) {
                if (Math.random() < PassengerDirector.SCENE_OCCURRENCE_SPAWN)
                    newScene = scenes.remove((int)(Math.round(Math.random() * (this.scenes.size()-1))));
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
            
            int leastBusyFloor = (int) floorNumWaiting.keySet().toArray()[0];
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
                                         texture, newScene);
            
            this.activePassengers.add(newPassenger);
            if (newScene != null)
                this.scenePassenger = newPassenger;
        }
        
        return newPassenger;
    }

}
