package simulator.elevator.game.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.scene.line.OptionLineTree;
import simulator.elevator.game.entity.AbstractEntity;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RelativeCoordinate;

/**
 * Maintains a persistent set of data for the game's state. Orchestrates the
 * other managers, handles the game's input, and draws the UI elements.
 */
public class GameStateManager implements InputProcessor {
    
    private Level level = null;
    
    private boolean paused = false;
    private float timeRemaining;
    private boolean isFinished;
    
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
    private Texture doorToggleButtonTexture;
    private Texture elevatorSliderTexture;
    private Box doorToggleButtonBox;
    private Box elevatorSliderBox;
    private Vector2 sliderSelectOffset = null;
    private Pair<OptionLineTree,List<Box>> playerOptionBoxes = null;
    private boolean hidePlayerOptionBoxes = false;
    
    private static GameStateManager instance;
    public static GameStateManager getInstance() {
        if (GameStateManager.instance == null) {
            instance = new GameStateManager();
            instance.reset();
        }
        return instance;
    }
    
    private GameStateManager() {
        try {
            this.level = new Level("C:\\Users\\alex1\\mystuff\\elevator-simulator\\assets\\level.json");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void reset() {
        this.timeRemaining = this.level.GAME_LENGTH_SEC;
        this.isFinished = false;
        this.level.WORLD_ORIGIN.getRelativeVector().set(new Vector2(0,0));
        this.elevator = new Elevator(new RelativeCoordinate(this.level.WORLD_ORIGIN, new Vector2(0,0)),
                this.level.ELEVATOR_Y_BOUND);
        
        this.doorToggleButtonTexture = this.level.BUTTON_TEXTURE;
        this.elevatorSliderTexture = this.level.SLIDER_TEXTURE;
        this.doorToggleButtonBox = new Box(new Vector2(this.level.BUTTON_POS),
                new Vector2(doorToggleButtonTexture.getWidth(),
                        doorToggleButtonTexture.getHeight()));
        this.elevatorSliderBox = new Box(new Vector2(this.level.SLIDER_POS),
                new Vector2(this.elevatorSliderTexture.getWidth(),
                        this.elevatorSliderTexture.getHeight()));
        
        this.entities.clear();
        this.entities.add(this.elevator);
        PassengerCoordinator.getInstance().reset();
        SceneDirector.getInstance().reset();
    }
    
    public Level getLevel() {
        return this.level;
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
        
        for (int i=0; i<this.level.FLOOR_SPAWNS.size(); i++) {
            Vector2 fAbsPos = this.level.FLOOR_SPAWNS.get(i).getAbsoluteVector();
            game.batch.draw(this.level.FLOOR_TEXTURE, fAbsPos.x, fAbsPos.y);
            game.font.draw(game.batch, Integer.toString(i+1), 
                    getLevel().DOOR_X_PIXEL-18, fAbsPos.y+60);
        }
        
        for (AbstractEntity e : this.entities)
            e.render(game);
        
        game.batch.draw(this.level.STATIC_UI_TEXTURE, 0, 0);
        game.batch.draw(this.doorToggleButtonTexture,
                this.doorToggleButtonBox.pos.x,
                this.doorToggleButtonBox.pos.y);
        game.batch.draw(this.elevatorSliderTexture,
                this.elevatorSliderBox.pos.x,
                this.elevatorSliderBox.pos.y);
        
        game.font.draw(game.batch, Integer.toString(this.elevator.getDurability())+"%", 780, 30);
        String tipString = getTipStr();
        game.font.draw(game.batch, tipString, 30, 540);
        game.font.draw(game.batch, Integer.toString((int)this.timeRemaining)+"s", 780, 540);
        
        SceneDirector.getInstance().render(game, deltaSec);
        if (this.playerOptionBoxes != null && !this.hidePlayerOptionBoxes)
            for (Box b : this.playerOptionBoxes.second)
                game.batch.draw(this.level.OPTION_BOX_TEXTURE, b.pos.x, b.pos.y);
        
        // to stop the mysterious concurrency errors
        for (AbstractEntity d : this.deadEntities)
            this.entities.remove(d);
        
        this.isFinished = this.timeRemaining <= 0 || this.elevator.isBroken();
        
        return this.isFinished;
    }
    
    public String getTipStr() {
        int tipCents = this.elevator.getTipTotal();
        String sign = tipCents < 0 ? "-" : "";
        tipCents = Math.abs(tipCents);
        int tipPartCents = tipCents % 100;
        int tipPartDollars = (int) (tipCents / 100f);
        return sign+"$"+tipPartDollars+"."+(tipPartCents < 10 ? "0" : "")+tipPartCents;
    }
    
    public void giveBonusTime() {
        this.timeRemaining += getLevel().BONUS_TIME;
    }
    
    public boolean isFinished() {
        return this.isFinished;
    }
    
    private void moveCamera() {
        Vector2 screenOrigin = new Vector2(this.elevator.getRelativePosition());
        screenOrigin.x = 0;
        screenOrigin.y = -Math.max(this.level.CAMERA_Y_BOUND_PIXEL.first,
                                   Math.min(this.level.CAMERA_Y_BOUND_PIXEL.second,screenOrigin.y));
        screenOrigin.y -= this.level.CAMERA_Y_OFFSET_PIXEL;
        this.level.WORLD_ORIGIN.getRelativeVector().set(screenOrigin);
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
            newBoxes.add(new Box(new Vector2(getLevel().PLAYER_TEXT_POS.x-4, 
                                             getLevel().PLAYER_TEXT_POS.y-20-18*2*i), 
                                 new Vector2(150*2, 18*2)));
        this.playerOptionBoxes = new Pair<OptionLineTree,List<Box>>(tree, newBoxes);
    }
    
    public void setHidePlayerOptions(boolean hide) {
        this.hidePlayerOptionBoxes = hide;
    }
    
    public void clearPlayerOptions() {
        this.playerOptionBoxes = null;
    }
    
    private void useSliderValue() {
        //TODO there's gotta be a better way to math this....
        float dSlider = new Vector2(this.elevatorSliderBox.pos()).sub(this.level.SLIDER_POS).len();
        if (this.elevatorSliderBox.pos().y < this.level.SLIDER_POS.y)
            dSlider *= -1;
        float maxSlider = new Vector2(this.elevatorSliderBox.pos().x, this.level.SLIDER_Y_BOUND_PIXEL.first)
                .sub(this.level.SLIDER_POS).len();
        this.elevator.move(dSlider/maxSlider);
    }
    
    private static Vector2 translateScreen(int x, int y) {
        // for some reason, 0,0 for input is the top left for inputs
        // instead of bottom left corner like it is for drawing
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
            newPos.y = Math.max(this.level.SLIDER_Y_BOUND_PIXEL.first, 
                    Math.min(newPos.y, this.level.SLIDER_Y_BOUND_PIXEL.second));
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
