package simulator.elevator.game.manager;

import java.util.Queue;

import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.game.scene.SceneType;
import simulator.elevator.util.Pair;

public class SceneDirector {
    
    private Queue<Pair<Passenger,Scene>> activeScenes;
    
    private static SceneDirector instance;
    public static SceneDirector getInstance() {
        if (instance == null)
            instance = new SceneDirector();
        return instance;
    }
    private SceneDirector () {
    }
    
    public void reset()
    {
        this.activeScenes.clear();
    }
    
    public void queueScene(Passenger passenger, Scene scene) {
        this.activeScenes.add(new Pair<Passenger,Scene>(passenger, scene));
    }
    
    public void render(float deltaSec) {
        if (this.activeScenes.peek().second.render(deltaSec))
            this.activeScenes.poll();
    }
    
    public void ejectPassengerScene(Passenger passenger) {
        if (this.activeScenes.peek().first == passenger)
            this.activeScenes.peek().second.eject();
        else
            this.activeScenes.removeIf(ps -> ps.first == passenger);
    }
    
    public Scene generateScene(SceneType type, PassengerPersonality personality,
                               PassengerState state, int happiness) {
        return null;
    }

}
