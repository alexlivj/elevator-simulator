package simulator.elevator.game.manager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

public class GameStateManager implements InputProcessor {

    //TODO maybe read these from somewhere
    private static final int GAME_TIME_SEC = 120;
    private static final RelativeCoordinate WORLD_ORIGIN = new RelativeCoordinate(null, new Vector2(0,0));
    private static final int FLOOR_SIZE = 192;
    public static final List<RelativeCoordinate> FLOOR_SPAWNS = new ArrayList<RelativeCoordinate>();
    static {
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,0)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE*2)));
        FLOOR_SPAWNS.add(new RelativeCoordinate(WORLD_ORIGIN, new Vector2(0,FLOOR_SIZE*3)));
    }
    private static final Texture FLOOR_TEXTURE = TextureUtility.doubleTextureSize("floor.png");
    private static final Texture BLINDERS_TEXTURE = TextureUtility.doubleTextureSize("blinders.png");
    private static final Pair<Integer,Integer> CAMERA_Y_BOUND = new Pair<Integer,Integer>(0,FLOOR_SIZE*3);
    private static final int CAMERA_Y_OFFSET = -250;
    private static final int ELEVATOR_SPEED_PIXEL_SEC = 30;
    public static final int ELEVATOR_DECAY_RATE_SEC = 5;
    public static final int ELEVATOR_DURABILITY_BUFFER_PIXEL = 5;
    private static final Pair<Integer,Integer> ELEVATOR_Y_BOUND;
    static {
        int lower = CAMERA_Y_BOUND.first-ELEVATOR_DURABILITY_BUFFER_PIXEL;
        int upper = CAMERA_Y_BOUND.second+ELEVATOR_DURABILITY_BUFFER_PIXEL;
        ELEVATOR_Y_BOUND = new Pair<Integer,Integer>(lower, upper);
    }
    
    private boolean paused = false;
    private float timeRemaining = GAME_TIME_SEC;
    private final RelativeCoordinate worldOrigin = WORLD_ORIGIN;
    
    private Elevator elevator;
    private final List<AbstractEntity> entities = new ArrayList<AbstractEntity>();
    private final List<AbstractEntity> deadEntities = new ArrayList<AbstractEntity>();

    private boolean spaceKeyUp = true;
    
    private static GameStateManager instance;
    public static GameStateManager getInstance() {
        if (GameStateManager.instance == null)
            instance = new GameStateManager();
        return instance;
    }
    
    private GameStateManager() {
        reset();
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.worldOrigin.getRelativeVector().set(new Vector2(0,0));
        this.elevator = new Elevator(new RelativeCoordinate(worldOrigin, new Vector2(0,0)),
                                     ELEVATOR_Y_BOUND);
        this.entities.clear();
        this.entities.add(this.elevator);
        PassengerCoordinator.getInstance().reset();
        SceneDirector.getInstance().reset();
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeRemaining -= deltaSec;
        
        AbstractEntity newEntity = PassengerCoordinator.getInstance().spawnPassengers(deltaSec);
        if (newEntity != null)
            this.entities.add(newEntity);

        if (!this.paused)
            for (AbstractEntity e : this.entities)
                e.update(deltaSec);
        
        moveCamera();
        
        //TODO render world
        for (RelativeCoordinate fPos : GameStateManager.FLOOR_SPAWNS) {
            Vector2 fAbsPos = fPos.getAbsoluteVector();
            game.batch.draw(FLOOR_TEXTURE, fAbsPos.x, fAbsPos.y);
        }
        
        for (AbstractEntity e : this.entities)
            e.render(game);
        game.batch.draw(BLINDERS_TEXTURE, 0, 0);
        
        //TODO render UI
        SceneDirector.getInstance().render(deltaSec);
        
        // to stop the mysterious concurrency errors
        for (AbstractEntity d : this.deadEntities)
            this.entities.remove(d);
        
        return this.timeRemaining <= 0;
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
    
    public Elevator getElevator() {
        return this.elevator;
    }
    
    public void despawnEntity(AbstractEntity e) {
        this.deadEntities.add(e);
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (this.paused)
            return false;
        
        int dy = 0;
        switch (keyCode) {
        case Input.Keys.UP:
            dy += ELEVATOR_SPEED_PIXEL_SEC;
            break;
        case Input.Keys.DOWN:
            dy -= ELEVATOR_SPEED_PIXEL_SEC;
            break;
        case Input.Keys.SPACE:
            if (this.spaceKeyUp)
                this.elevator.toggleDoor();
            this.spaceKeyUp = false;
            break;
        }
        
        if (!this.elevator.isDoorOpen())
            this.elevator.move(dy);
        
        return true;
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (this.paused)
            return false;
        
        switch (keyCode) {
        case Input.Keys.UP:
        case Input.Keys.DOWN:
            this.elevator.haltMove();
            break;
        case Input.Keys.SPACE:
            this.spaceKeyUp = true;
            break;
        }
        
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // not relevant
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // not relevant on desktop
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // not relevant
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // not relevant?
        return false;
    }
    
}
