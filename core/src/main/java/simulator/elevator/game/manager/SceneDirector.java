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
import simulator.elevator.game.scene.script.StatementLineTree;
import simulator.elevator.game.scene.script.PortraitType;
import simulator.elevator.game.scene.script.Scene;
import simulator.elevator.game.scene.script.SceneType;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RandomUtility;

public class SceneDirector {
    
    private record ActiveScene (Passenger passenger, SceneType type, Scene scene) {}

    //TODO again, maybe read this from somewhere
    private static final Map<SceneType,Map<CastingDirection,List<Scene>>> ALL_NORMAL_SCENES = 
            new HashMap<SceneType,Map<CastingDirection,List<Scene>>>();
    private static final List<StarRole> ALL_STAR_SCENES = new ArrayList<StarRole>();
    static {
        PassengerPersonality minInf = 
                new PassengerPersonality(Integer.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        PassengerPersonality maxInf = 
                new PassengerPersonality(Integer.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        CastingDirection anyoneWithAPulse = 
                new CastingDirection(new Pair<PassengerPersonality,PassengerPersonality>(minInf, maxInf),
                        new Pair<Float,Float>(Float.MIN_VALUE, Float.MAX_VALUE));
        
        Map<CastingDirection,List<Scene>> elevatorFullMap = new HashMap<CastingDirection,List<Scene>>();
        ALL_NORMAL_SCENES.put(SceneType.ELEVATOR_FULL, elevatorFullMap);
        List<Scene> elevatorFullPulseScenes = new ArrayList<Scene>();
        elevatorFullMap.put(anyoneWithAPulse, elevatorFullPulseScenes);
        
        Scene simpleElevatorFull = new Scene(
                new StatementLineTree(PortraitType.PLAYER_NEUTRAL, null, "Sorry, elevator's full.", null),
                null);
        elevatorFullPulseScenes.add(simpleElevatorFull);
    }
    private static final int MAX_SCENES = 3;
    
    private List<StarRole> availableStarScenes = new ArrayList<StarRole>();
    private Pair<Passenger,StarRole> starScene = null;
    private PassengerState startStarScene = null;
    
    private Map<SceneType,Boolean> acceptingSceneType = new HashMap<SceneType,Boolean>();
    private Queue<ActiveScene> queuedScenes = new LinkedList<ActiveScene>();
    private ActiveScene activeScene = null;

    private Queue<StatementLineTree> queuedInterrupts = new LinkedList<StatementLineTree>();
    private StatementLineTree activeInterrupt = null;
    
    private static SceneDirector instance;
    public static SceneDirector getInstance() {
        if (instance == null)
            instance = new SceneDirector();
        return instance;
    }
    private SceneDirector () {
        for (SceneType type : SceneType.values())
            acceptingSceneType.put(type, true);
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
    
    public void queueInterrupt(StatementLineTree line) {
        this.queuedInterrupts.add(line);
    }
    
    public void render(float deltaSec) {
        boolean runningInterrupt = this.activeInterrupt != null;
        boolean runningScene = !runningInterrupt && this.activeScene != null;

        boolean switchInterrupt = !runningInterrupt;
        boolean switchScene = !runningScene;
        if (runningInterrupt)
            switchInterrupt = this.activeInterrupt.render(deltaSec);
        else if (runningScene)
            switchScene = this.activeScene.scene().render(deltaSec);
        
        if (switchInterrupt) {
            if (this.activeInterrupt != null)
                this.activeInterrupt.reset();
            this.activeInterrupt = null;
            if (!this.queuedInterrupts.isEmpty()) {
                this.activeInterrupt = this.queuedInterrupts.poll();
            }
        }
        if (switchScene) {
            if (this.activeScene != null)
                this.activeScene.scene().reset();
            this.activeScene = null;
            if (this.startStarScene != null) {
                this.activeScene = new ActiveScene(this.starScene.first, SceneType.STAR, 
                        this.starScene.second.scenes().get(this.startStarScene));
                this.startStarScene = null;
            } else {
                if (this.queuedScenes.peek() != null)
                this.activeScene = this.queuedScenes.poll(); //sets to null if empty
            }
        }
    }
    
    public void readyStarScene(PassengerState state) {
        this.startStarScene = state;
    }
    
    public void ejectPassengerCurrentScene(Passenger passenger) {
        if (this.activeScene != null && this.activeScene.passenger() == passenger)
            this.activeScene.scene().eject();
    }
    
    public void removePassengerScenes(Passenger passenger) {
        if (this.activeScene != null && this.activeScene.passenger() == passenger)
            this.activeScene.scene().eject();
        else
            this.queuedScenes.removeIf(as -> as.passenger() == passenger);
    }
    
    public void requestScene(Passenger passenger, SceneType type) {
        Scene newScene = null;
        if ((this.starScene == null || this.activeScene.passenger() != this.starScene.first)
                && this.acceptingSceneType.get(type)
                && !this.hasSceneType(type)
                && this.numScenes() < SceneDirector.MAX_SCENES) {
            Map<CastingDirection,List<Scene>> stateScenes = ALL_NORMAL_SCENES.get(type);
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
                int randomSceneNum = RandomUtility.getRandomIntRange(0, stateScenes.get(req).size()-1);
                newScene = stateScenes.get(req).get(randomSceneNum);
                
                if (type == SceneType.ELEVATOR_FULL)
                    this.acceptingSceneType.put(type, false);
            }
        }
        if (newScene != null)
            this.queuedScenes.add(new ActiveScene(passenger, type, newScene));
    }
    
    private boolean hasSceneType(SceneType type) {
        Queue<ActiveScene> queueCopy = new LinkedList<ActiveScene>(this.queuedScenes);
        return (this.activeScene != null && this.activeScene.type() == type)
                || queueCopy.stream().map(as -> as.type()).anyMatch(t -> t == type);
    }
    
    private int numScenes() {
        return this.queuedScenes.size() + ((this.activeScene != null) ? 1 : 0);
    }

}
