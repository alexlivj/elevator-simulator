package simulator.elevator.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.LinearEntity;

public class GameManager {
    
    private static final int GAME_TIME_SEC = 120;
    
    private boolean paused = false;
    private float timeRemaining;
    
    private Elevator elevator;
    private final List<LinearEntity> entities = new ArrayList<LinearEntity>();
    
    private final RelativeCoordinate worldOrigin = new RelativeCoordinate(null, new Vector2(0,0)); 
    
    public GameManager() {
        reset();
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.elevator = new Elevator(new RelativeCoordinate(worldOrigin, new Vector2(0,0)));
        this.entities.clear();
        this.entities.add(this.elevator);
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeRemaining -= deltaSec;
        
        int speedSec = 50;
        int dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            dy += speedSec;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            dy -= speedSec;
        if (dy == 0)
            this.elevator.haltMove();
        else
            this.elevator.move(dy);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            this.elevator.toggleDoor();
        
        for (LinearEntity e : this.entities) {
            if (!this.paused)
                e.update(deltaSec);
            e.render(game);
        }
        
        return this.timeRemaining <= 0;
    }
    
    public void pause() {
        this.paused = true;
    }
    
    public void resume() {
        this.paused = false;
    }
    
}
