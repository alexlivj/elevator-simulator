package simulator.elevator.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.LinearEntity;
import simulator.elevator.game.scene.Scene;
import simulator.elevator.util.Pair;

public class GameStateManager {

    //TODO maybe read these from somewhere
    private static final int GAME_TIME_SEC = 120;
    private static final RelativeCoordinate WORLD_ORIGIN = new RelativeCoordinate(null, new Vector2(0,0));
    private static final int FLOOR_SIZE = 192;
    private static final List<RelativeCoordinate> FLOOR_SPAWNS = new ArrayList<RelativeCoordinate>();
    static {
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,0)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE*2)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE*3)));
    }
    private static final Pair<Integer,Integer> CAMERA_Y_BOUND = new Pair<Integer,Integer>(0,FLOOR_SIZE*3);
    private static final int CAMERA_Y_OFFSET = -250;
    private static final int ELEVATOR_SPEED_PIXEL_SEC = 30;
    public static final int ELEVATOR_DECAY_RATE_SEC = 5;
    public static final int ELEVATOR_BUFFER_PIXEL = 10;
    private static final Pair<Integer,Integer> ELEVATOR_Y_BOUND;
    static {
        int lower = CAMERA_Y_BOUND.first-ELEVATOR_BUFFER_PIXEL;
        int upper = CAMERA_Y_BOUND.second+ELEVATOR_BUFFER_PIXEL;
        ELEVATOR_Y_BOUND = new Pair<Integer,Integer>(lower, upper);
    }
    private static final List<Scene> SCENES = new ArrayList<Scene>();
    
    //TODO
    private PassengerDirector director = new PassengerDirector(FLOOR_SPAWNS, SCENES);
    
    private boolean paused = false;
    private float timeRemaining = GAME_TIME_SEC;
    private final RelativeCoordinate worldOrigin = WORLD_ORIGIN; 
    
    private Elevator elevator;
    private final List<LinearEntity> entities = new ArrayList<LinearEntity>();
    
    public GameStateManager() {
        reset();
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.worldOrigin.getRelativeVector().set(new Vector2(0,0));
        this.elevator = new Elevator(new RelativeCoordinate(worldOrigin, new Vector2(0,0)),
                                     ELEVATOR_Y_BOUND);
        this.entities.clear();
        this.entities.add(this.elevator);
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeRemaining -= deltaSec;
        
        LinearEntity newEntity = this.director.spawnPassengers(deltaSec);
        if (newEntity != null)
            this.entities.add(newEntity);

        if (!this.paused)
            handleInputs(deltaSec);

        if (!this.paused)
            for (LinearEntity e : this.entities)
                e.update(deltaSec);
        
        moveCamera();
        
        for (LinearEntity e : this.entities)
            e.render(game);
        
        return this.timeRemaining <= 0;
    }
    
    private void handleInputs(float deltaSec) {
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
    
    private void moveCamera() {
        Vector2 screenOrigin = new Vector2(this.elevator.getRelativePosition());
        screenOrigin.x = 0;
        screenOrigin.y = -Math.max(GameStateManager.CAMERA_Y_BOUND.first,
                                   Math.min(GameStateManager.CAMERA_Y_BOUND.second,screenOrigin.y));
        screenOrigin.y -= GameStateManager.CAMERA_Y_OFFSET;
        this.worldOrigin.getRelativeVector().set(screenOrigin);
    }
    
    public void pause() {
        this.paused = true;
    }
    
    public void resume() {
        this.paused = false;
    }
    
}
