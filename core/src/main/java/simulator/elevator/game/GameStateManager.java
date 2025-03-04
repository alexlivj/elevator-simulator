package simulator.elevator.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.util.Pair;

public class GameStateManager {

    //TODO maybe read these from somewhere
    private static final int GAME_TIME_SEC = 120;
    private static final int ELEVATOR_SPEED_PIXEL_SEC = 50;
    public static final int ELEVATOR_DECAY_RATE_SEC = 5;
    private static final Pair<Integer,Integer> CAMERA_Y_BOUND = new Pair<Integer,Integer>(0,400);
    private static final Pair<Integer,Integer> ELEVATOR_Y_BOUND = new Pair<Integer,Integer>(-50,450);
    
    private boolean paused = false;
    private float timeRemaining;
    
    private Elevator elevator;
    private final List<LinearEntity> entities = new ArrayList<LinearEntity>();
    
    private final RelativeCoordinate worldOrigin = new RelativeCoordinate(null, new Vector2(0,0)); 
    
    public GameStateManager() {
        reset();
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.elevator = new Elevator(new RelativeCoordinate(worldOrigin, new Vector2(0,0)), ELEVATOR_Y_BOUND);
        this.entities.clear();
        this.entities.add(this.elevator);
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeRemaining -= deltaSec;
        
        if (!this.paused) { 
            if (!this.elevator.isDoorOpen()) {
                int dy = 0;
                if (Gdx.input.isKeyPressed(Input.Keys.UP))
                    dy += ELEVATOR_SPEED_PIXEL_SEC;
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                    dy -= ELEVATOR_SPEED_PIXEL_SEC;
                if (dy == 0)
                    this.elevator.haltMove();
                else
                    this.elevator.move(dy, deltaSec);
            } else {
                this.elevator.haltMove();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
                this.elevator.toggleDoor();
        }

        if (!this.paused)
            for (LinearEntity e : this.entities)
                e.update(deltaSec);
        
        Vector2 screenOrigin = new Vector2(this.elevator.getRelativePosition());
        screenOrigin.x = 0;
        screenOrigin.y = -Math.max(CAMERA_Y_BOUND.first,Math.min(CAMERA_Y_BOUND.second,screenOrigin.y));
        this.worldOrigin.getRelativeVector().set(screenOrigin);
        
        for (LinearEntity e : this.entities)
            e.render(game);
        
        return this.timeRemaining <= 0;
    }
    
    public void pause() {
        this.paused = true;
    }
    
    public void resume() {
        this.paused = false;
    }
    
}
