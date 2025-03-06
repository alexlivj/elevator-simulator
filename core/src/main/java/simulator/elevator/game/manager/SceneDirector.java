package simulator.elevator.game.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.game.scene.SceneType;
import simulator.elevator.game.scene.StarScene;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RandomUtility;

public class SceneDirector {

    public static final List<StarScene> ALL_STAR_SCENES = new ArrayList<StarScene>();
    private List<StarScene> availableStarScenes = new ArrayList<StarScene>();
    
    private Queue<Pair<Passenger,Scene>> queuedScenes;
    private Pair<Passenger,StarScene> starScene = null;
    private Pair<Passenger,Scene> activeScene = null;
    private PassengerState activateStarScene = null;
    
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
        this.queuedScenes.clear();
        this.availableStarScenes.clear();
        this.availableStarScenes.addAll(SceneDirector.ALL_STAR_SCENES);
    }
    
    public StarScene requestStarScene() {
        StarScene newStarScene = null;
        if (this.starScene != null) {
            int newStarSceneIndex = RandomUtility.getRandomIntRange(0,availableStarScenes.size()-1);
            newStarScene = this.availableStarScenes.remove(newStarSceneIndex);
        }
        return newStarScene;
    }
    
    public void queueScene(Passenger passenger, Scene scene) {
        this.queuedScenes.add(new Pair<Passenger,Scene>(passenger, scene));
    }
    
    public void render(float deltaSec) {
        if (this.activeScene.second.render(deltaSec)) {
            if (this.activateStarScene == null) {
                this.activeScene = this.queuedScenes.poll();
            } else {
                this.activeScene = new Pair<Passenger,Scene>(
                        this.starScene.first, this.starScene.second.scenes().get(this.activateStarScene));
                this.activateStarScene = null;
            }
        }
    }
    
    public void readyStarScene(PassengerState state) {
        this.activateStarScene = state;
    }
    
    public void ejectCurrentScene() {
        this.activeScene.second.eject();
    }
    
    public void ejectPassengerScenes(Passenger passenger) {
        if (this.queuedScenes.peek().first == passenger)
            this.queuedScenes.peek().second.eject();
        else
            this.queuedScenes.removeIf(ps -> ps.first == passenger);
    }
    
    public Scene generateScene(SceneType type, PassengerPersonality personality,
                               PassengerState state, int happiness) {
        Scene newScene = null;
        if (this.starScene == null || this.activeScene.first != this.starScene.first) {
            //TODO
        }
        return newScene;
    }

}
