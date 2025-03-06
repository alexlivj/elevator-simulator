package simulator.elevator.game.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.CastingDirection;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.script.NpcLineTree;
import simulator.elevator.game.scene.script.Scene;
import simulator.elevator.game.scene.script.SceneType;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RandomUtility;

public class SceneDirector {

    //TODO again, maybe read this from somewhere
    private static final Map<PassengerState,Map<CastingDirection,List<Scene>>> ALL_NORMAL_SCENES = 
            new HashMap<PassengerState,Map<CastingDirection,List<Scene>>>();
    private static final List<StarRole> ALL_STAR_SCENES = new ArrayList<StarRole>();
    static {
        PassengerPersonality minInf = 
                new PassengerPersonality(Integer.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        PassengerPersonality maxInf = 
                new PassengerPersonality(Integer.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        CastingDirection anyoneWithAPulse = 
                new CastingDirection(new Pair<PassengerPersonality,PassengerPersonality>(minInf, maxInf),
                        new Pair<Float,Float>(Float.MIN_VALUE, Float.MAX_VALUE));
        //TODO
    }
    
    private List<StarRole> availableStarScenes = new ArrayList<StarRole>();
    private Pair<Passenger,StarRole> starScene = null;
    private PassengerState startStarScene = null;
    
    private Queue<Pair<Passenger,Scene>> queuedScenes = new LinkedList<Pair<Passenger,Scene>>();
    private Pair<Passenger,Scene> activeScene = null;

    private Queue<NpcLineTree> queuedInterrupts = new LinkedList<NpcLineTree>();
    private NpcLineTree activeInterrupt = null;
    
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
        this.activeScene = null;
        this.activeInterrupt = null;
        this.queuedScenes.clear();
        this.queuedInterrupts.clear();
        this.availableStarScenes.clear();
        this.availableStarScenes.addAll(SceneDirector.ALL_STAR_SCENES);
    }
    
    public StarRole requestStarScene() {
        StarRole newStarScene = null;
        if (this.starScene != null) {
            int newStarSceneIndex = RandomUtility.getRandomIntRange(0,availableStarScenes.size()-1);
            newStarScene = this.availableStarScenes.remove(newStarSceneIndex);
        }
        return newStarScene;
    }
    
    public void queueInterrupt(NpcLineTree line) {
        this.queuedInterrupts.add(line);
    }
    
    public void queueScene(Passenger passenger, Scene scene) {
        this.queuedScenes.add(new Pair<Passenger,Scene>(passenger, scene));
    }
    
    public void render(float deltaSec) {
        boolean runningInterrupt = this.activeInterrupt != null;
        boolean runningScene = !runningInterrupt && this.activeScene != null;
        
        boolean switchScene = !runningInterrupt || !runningScene;
        if (runningInterrupt)
            switchScene = this.activeInterrupt.render(deltaSec);
        else if (runningScene)
            switchScene = this.activeScene.second.render(deltaSec);
        
        if (switchScene) {
            if (!this.queuedInterrupts.isEmpty()) {
                this.activeInterrupt = this.queuedInterrupts.poll();
            } else {
                this.activeInterrupt = null;
                if (this.startStarScene != null) {
                    this.activeScene = new Pair<Passenger,Scene>(
                            this.starScene.first, this.starScene.second.scenes().get(this.startStarScene));
                    this.startStarScene = null;
                } else {
                    this.activeScene = this.queuedScenes.poll(); //sets to null if empty
                }
            }
        }
    }
    
    public void readyStarScene(PassengerState state) {
        this.startStarScene = state;
    }
    
    public void ejectPassengerCurrentScene(Passenger passenger) {
        if (this.activeScene.first == passenger)
            this.activeScene.second.eject();
    }
    
    public void ejectPassengerScenes(Passenger passenger) {
        if (this.queuedScenes.peek().first == passenger)
            this.queuedScenes.peek().second.eject();
        else
            this.queuedScenes.removeIf(ps -> ps.first == passenger);
    }
    
    public Scene requestScene(Passenger passenger, SceneType type) {
        Scene newScene = null;
        if (this.starScene == null || this.activeScene.first != this.starScene.first) {
            Map<CastingDirection,List<Scene>> stateScenes = ALL_NORMAL_SCENES.get(passenger.getState());
            Set<CastingDirection> validReqs = new HashSet<CastingDirection>(stateScenes.keySet());
            validReqs.stream().filter(r -> r.isValidPassenger(passenger));
            
            int randomReqNum = RandomUtility.getRandomIntRange(0, validReqs.size());
            int i=0;
            CastingDirection req = null;
            for (CastingDirection r : validReqs) {
                if (i == randomReqNum) {
                    req = r;
                    break;
                }
                i++;
            }
            
            if (req != null) {
                int randomSceneNum = RandomUtility.getRandomIntRange(0, stateScenes.get(req).size());
                newScene = stateScenes.get(req).get(randomSceneNum);
            }
        }
        return newScene;
    }

}
