package simulator.elevator.game.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerState;
import simulator.elevator.game.scene.CastingDirection;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.game.scene.SceneType;
import simulator.elevator.game.scene.StarRole;
import simulator.elevator.game.scene.line.LineReturn;
import simulator.elevator.game.scene.line.OptionConsequence;
import simulator.elevator.game.scene.line.StatementLineTree;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RandomUtility;

public class SceneDirector {
    
    private record ActiveScene (Passenger passenger, SceneType type, Scene scene) {}
    
    private List<StarRole> availableStarScenes = new ArrayList<StarRole>();
    private Pair<Passenger,StarRole> starScene = null;
    private PassengerState startStarScene = null;
    
    private Map<SceneType,Boolean> acceptingSceneType = new HashMap<SceneType,Boolean>();
    private Queue<ActiveScene> queuedScenes = new LinkedList<ActiveScene>();
    private ActiveScene activeScene = null;

    private Queue<Pair<Color,StatementLineTree>> queuedInterrupts =
            new LinkedList<Pair<Color,StatementLineTree>>();
    private Pair<Color,StatementLineTree> activeInterrupt = null;
    
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
        this.starScene = null;
        this.startStarScene = null;
        this.queuedScenes.clear();
        this.queuedInterrupts.clear();
        this.availableStarScenes.clear();
        this.availableStarScenes.addAll(getLevel().ALL_STAR_SCENES);
    }
    
    public Level getLevel() {
        return GameStateManager.getInstance().getLevel();
    }
    
    public StarRole requestStarScene() {
        StarRole newStarScene = null;
        if (this.starScene == null && this.availableStarScenes.size() > 0) {
            int newStarSceneIndex = RandomUtility.getRandomIntRange(0,availableStarScenes.size()-1);
            newStarScene = this.availableStarScenes.remove(newStarSceneIndex);
            this.starScene = new Pair<Passenger,StarRole>(null,newStarScene);
        }
        return newStarScene;
    }
    
    public void offerStarPassenger(Passenger passenger) {
        if (this.starScene != null && this.starScene.first == null)
            this.starScene = new Pair<Passenger,StarRole>(passenger, this.starScene.second);
    }
    
    public void queueInterrupt(Color color, StatementLineTree line) {
        this.queuedInterrupts.add(new Pair<Color,StatementLineTree>(color,line));
    }
    
    public void render(Main game, float deltaSec) {
        boolean runningInterrupt = this.activeInterrupt != null;
        boolean runningScene = !runningInterrupt && this.activeScene != null;

        boolean switchInterrupt = this.activeInterrupt == null;
        boolean switchScene = this.activeScene == null;
        if (runningInterrupt) {
            Pair<OptionConsequence,LineReturn> lineOut= 
                    this.activeInterrupt.second.render(game, getLevel(), 
                                                       this.activeInterrupt.first,
                                                       deltaSec);
            switchInterrupt = lineOut.second == LineReturn.FINISH;
        } else if (runningScene) {
            LineReturn lineReturn = this.activeScene.scene().render(game,
                                                                    deltaSec,
                                                                    this.activeScene.passenger());
            if (lineReturn != LineReturn.CONTINUE_NEXT)
                switchInterrupt = false;
            switchScene = lineReturn == LineReturn.FINISH;
        }
        
        if (switchInterrupt) {
            if (this.activeInterrupt != null)
                this.activeInterrupt.second.reset();
            GameStateManager.getInstance().setHidePlayerOptions(false);
            this.activeInterrupt = null;
            if (!this.queuedInterrupts.isEmpty()) {
                this.activeInterrupt = this.queuedInterrupts.poll();
                if (this.activeInterrupt != null)
                    GameStateManager.getInstance().setHidePlayerOptions(true);
            }
        }
        if (switchScene) {
            if (this.activeScene != null)
                this.activeScene.scene().reset();
            this.activeScene = null;
            if (this.startStarScene != null) {
                Scene starScene = this.starScene.second.scenes().get(this.startStarScene);
                if (starScene != null) {
                    this.activeScene = new ActiveScene(this.starScene.first, SceneType.STAR, starScene);
                }
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
        if (this.activeScene != null && this.activeScene.passenger() == passenger) {
            this.activeScene.scene().eject();
        } else {
            this.queuedScenes.removeIf(as -> as.passenger() == passenger);
            if (this.starScene != null && this.starScene.first == passenger)
                this.starScene = null;
        }
    }
    
    public void requestScene(Passenger passenger, SceneType type) {
        Scene newScene = null;
        PassengerState state = type.getState();
        if ((this.starScene == null 
                    || !(this.activeScene != null 
                            && this.activeScene.passenger() == this.starScene.first)
                    || (state == null
                            || !this.starScene.second.scenes().containsKey(state)
                            || this.starScene.second.scenes().get(state) == null))
                && this.acceptingSceneType.get(type)
                && !this.hasSceneType(type)
                && this.numScenes() < getLevel().MAX_SCENES) {
            Map<CastingDirection,List<Scene>> stateScenes = getLevel().ALL_NORMAL_SCENES.get(type);
            if (stateScenes != null) {
                Set<CastingDirection> validReqs = stateScenes.keySet().stream()
                        .filter(r -> r.isValidPassenger(passenger))
                        .collect(Collectors.toSet());
                
                int randomReqNum = RandomUtility.getRandomIntRange(0, validReqs.size()-1);
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
                    
                    if (type == SceneType.ELEVATOR_FULL || type == SceneType.TOO_FAR)
                        this.acceptingSceneType.put(type, false);
                }
            }
        }
        if (newScene != null)
            this.queuedScenes.add(new ActiveScene(passenger, type, newScene));
    }
    
    public void notifyDoorJustClosed() {
        this.acceptingSceneType.put(SceneType.TOO_FAR, true);
        this.acceptingSceneType.put(SceneType.ELEVATOR_FULL, true);
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
