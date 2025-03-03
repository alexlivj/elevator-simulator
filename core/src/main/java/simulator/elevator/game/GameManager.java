package simulator.elevator.game;

import java.util.ArrayList;
import java.util.List;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.Entity;

public class GameManager {
    
    private static final int GAME_TIME_SEC = 120;
    
    private boolean paused = false;
    private float timeRemaining;
    private Elevator elevator;
    
    private final List<Entity> entities = new ArrayList<Entity>();
    
    public GameManager() {
        reset();
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeRemaining -= deltaSec;
        for (Entity e : this.entities) {
            if (!this.paused)
                e.update(deltaSec);
            e.render(game);
        }
        
        return this.timeRemaining == 0;
    }
    
    public void pause() {
        this.paused = true;
    }
    
    public void resume() {
        this.paused = false;
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.elevator = new Elevator(null); //TODO elevator starting location
        this.entities.clear();
        this.entities.add(this.elevator);
    }
    
}
