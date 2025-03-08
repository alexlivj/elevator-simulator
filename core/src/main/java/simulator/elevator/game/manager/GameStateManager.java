package simulator.elevator.game.manager;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.scene.script.OptionLineTree;
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
    private static final Pair<Integer,Integer> CAMERA_Y_BOUND = new Pair<Integer,Integer>(0,FLOOR_SIZE*3);
    private static final int CAMERA_Y_OFFSET = -250;
    public static final int ELEVATOR_DURABILITY_BUFFER_PIXEL = 5;
    private static final Pair<Integer,Integer> ELEVATOR_Y_BOUND;
    static {
        int lower = CAMERA_Y_BOUND.first-ELEVATOR_DURABILITY_BUFFER_PIXEL;
        int upper = CAMERA_Y_BOUND.second+ELEVATOR_DURABILITY_BUFFER_PIXEL;
        ELEVATOR_Y_BOUND = new Pair<Integer,Integer>(lower, upper);
    }
    private static final Pair<Integer,Integer> SLIDER_Y_BOUND = 
            new Pair<Integer,Integer>(100-50,100+50);
    private static final Vector2 SLIDER_CENTER = new Vector2(320*2, 100);
    private static final Vector2 BUTTON_CENTER = new Vector2(264*2, 50);
    private static final Texture STATIC_UI = TextureUtility.doubleTextureSize("static-ui.png");
    
    private boolean paused = false;
    private float timeRemaining = GAME_TIME_SEC;
    private final RelativeCoordinate worldOrigin = WORLD_ORIGIN;
    
    private Elevator elevator;
    private final List<AbstractEntity> entities = new ArrayList<AbstractEntity>();
    private final List<AbstractEntity> deadEntities = new ArrayList<AbstractEntity>();

    private record Box(Vector2 pos, Vector2 size) {
        public boolean containsScreenPoint(int x, int y) {
            Vector2 point = translateScreen(x,y);

            return (this.pos.x < point.x && point.x < this.pos.x+this.size.x)
                    && (this.pos.y < point.y && point.y < this.pos.y+this.size.y);
        }
    }
    private Texture doorToggleButton = TextureUtility.doubleTextureSize("button.png");
    private Box doorToggleButtonBox = new Box(new Vector2(BUTTON_CENTER),
                                              new Vector2(doorToggleButton.getWidth(),
                                                          doorToggleButton.getHeight()));
    private Texture elevatorSlider = TextureUtility.doubleTextureSize("slider.png");
    private Box elevatorSliderBox = new Box(new Vector2(SLIDER_CENTER),
                                            new Vector2(elevatorSlider.getWidth(),
                                                        elevatorSlider.getHeight()));
    private Vector2 sliderSelectOffset = null;
    private Pair<OptionLineTree,List<Box>> playerOptionBoxes = null;
    
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
        
        for (RelativeCoordinate fPos : GameStateManager.FLOOR_SPAWNS) {
            Vector2 fAbsPos = fPos.getAbsoluteVector();
            game.batch.draw(FLOOR_TEXTURE, fAbsPos.x, fAbsPos.y);
        }
        
        for (AbstractEntity e : this.entities)
            e.render(game);
        
        game.batch.draw(STATIC_UI, 0, 0);
        game.batch.draw(this.doorToggleButton,
                this.doorToggleButtonBox.pos.x,
                this.doorToggleButtonBox.pos.y);
        game.batch.draw(this.elevatorSlider,
                this.elevatorSliderBox.pos.x,
                this.elevatorSliderBox.pos.y);
        game.font.draw(game.batch, Integer.toString(this.elevator.getDurability())+"%", 780, 30);
        int tipCents = this.elevator.getTipTotal();
        int tipPartCents = tipCents % 100;
        int tipPartDollars = (int) (tipCents / 100f);
        String tipString = "$"+tipPartDollars+"."+(tipPartCents == 0 ? "00" : tipPartCents);
        game.font.draw(game.batch, tipString, 30, 540);
        game.font.draw(game.batch, Integer.toString((int)this.timeRemaining)+"s", 780, 540);
        SceneDirector.getInstance().render(game, deltaSec);
        
        // to stop the mysterious concurrency errors
        for (AbstractEntity d : this.deadEntities)
            this.entities.remove(d);
        
        return this.timeRemaining <= 0 || this.elevator.isBroken();
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
    
    public void addPlayerOptionBoxes(OptionLineTree tree, int numBoxes) {
        List<Box> newBoxes = new ArrayList<Box>();
        for (int i=0; i<numBoxes; i++)
            newBoxes.add(new Box(new Vector2(205*2, 210*2+15*2*i), new Vector2(180*2, 15*2)));
        this.playerOptionBoxes = new Pair<OptionLineTree,List<Box>>(tree, newBoxes);
    }
    
    public void clearPlayerOptions() {
        this.playerOptionBoxes = null;
    }
    
    private void useSliderValue() {
        //TODO there's gotta be a better way to math this....
        float dSlider = new Vector2(this.elevatorSliderBox.pos()).sub(GameStateManager.SLIDER_CENTER).len();
        if (this.elevatorSliderBox.pos().y < GameStateManager.SLIDER_CENTER.y)
            dSlider *= -1;
        float maxSlider = new Vector2(this.elevatorSliderBox.pos().x, GameStateManager.SLIDER_Y_BOUND.first).sub(GameStateManager.SLIDER_CENTER).len();
        this.elevator.move(dSlider/maxSlider);
    }
    
    private static Vector2 translateScreen(int x, int y) {
        // for some reason, 0,0 for input is the top left, instead of bottom left corner
        return new Vector2(x,560-y);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (this.paused)
            return false;
        
        if (this.elevatorSliderBox.containsScreenPoint(screenX, screenY))
            this.sliderSelectOffset = translateScreen(screenX, screenY).sub(this.elevatorSliderBox.pos());
        
        if (this.playerOptionBoxes != null) {
            for (int i=0; i<this.playerOptionBoxes.second.size(); i++) {
                if (this.playerOptionBoxes.second.get(i).containsScreenPoint(screenX, screenY)) {
                    this.playerOptionBoxes.first.setSelection(i);
                    break;
                }
            }
        }
        
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (this.sliderSelectOffset != null) {
            Vector2 newPos = translateScreen(screenX, screenY).sub(this.sliderSelectOffset);
            newPos.x = this.elevatorSliderBox.pos().x;
            newPos.y = Math.max(SLIDER_Y_BOUND.first, Math.min(newPos.y, SLIDER_Y_BOUND.second));
            this.elevatorSliderBox.pos().set(newPos);
            useSliderValue();
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (this.doorToggleButtonBox.containsScreenPoint(screenX, screenY)) {
            if (!this.elevator.toggleDoor())
                useSliderValue();
        }
        this.sliderSelectOffset = null;
        
        return true;
    }

    @Override
    public boolean keyDown(int keyCode) {
        // not relevant
        return false;
    }

    @Override
    public boolean keyUp(int keyCode) {
        // not relevant
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
