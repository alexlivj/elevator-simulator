package simulator.elevator.game.entity;

import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.manager.GameStateManager;
import simulator.elevator.game.manager.SceneDirector;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RelativeCoordinate;

public class Elevator extends AbstractEntity {
    
    private final Level level;

    private int deltaY = 0;
    private final Pair<Integer,Integer> yAxisBound;
    private float durability = 100;
    private boolean openDoor = true;
    private int tipCents = 0;
    
    public Elevator(RelativeCoordinate pos, Pair<Integer,Integer> yAxisBound) {
        super(pos, GameStateManager.getInstance().getLevel().ELEVATOR_OPEN_TEXTURE);
        this.level = GameStateManager.getInstance().getLevel();
        this.yAxisBound = yAxisBound;
    }
    
    @Override
    public void update(float deltaSec) {
        // this is silly...
        if (this.deltaY != 0 && !this.openDoor) {
            RelativeCoordinate pos = getPosition();
            float posRelY = pos.getRelativeVector().y;
            if ((posRelY < this.yAxisBound.first && this.deltaY < 0)
                    || (this.yAxisBound.second < posRelY && 0 < this.deltaY)) {
                int dy = this.deltaY;
                this.deltaY *= this.level.ELEVATOR_BOUND_HALT_DECAY_MOD;
                haltMove();
                this.deltaY = dy; 
            } else {
                float absDeltaY = Math.abs(this.deltaY);
                if (absDeltaY > this.level.ELEVATOR_UNSAFE_SPEED_PIXEL_SEC) {
                    float maxUnsafeDiff = 
                            this.level.ELEVATOR_SPEED_PIXEL_SEC - this.level.ELEVATOR_UNSAFE_SPEED_PIXEL_SEC;
                    float unsafeDeltaY = this.level.ELEVATOR_SPEED_PIXEL_SEC - absDeltaY;
                    float unsafeMagnitude = 1 - unsafeDeltaY / maxUnsafeDiff;
                    this.durability -= unsafeMagnitude*this.level.ELEVATOR_UNSAFE_DECAY_RATE_SEC * deltaSec;
                }
                Vector2 newRel = new Vector2(pos.getRelativeVector()).add(new Vector2(0,this.deltaY));
                moveTo(new RelativeCoordinate(pos.getOrigin(), newRel), Math.abs(this.deltaY));
            }
        }
        
        super.update(deltaSec);
    }
    
    @Override
    public void render(Main game) {
        super.render(game);
    }
    
    public void move(float maxSpeedFraction) {
        this.deltaY = Math.round(maxSpeedFraction * this.level.ELEVATOR_SPEED_PIXEL_SEC);
    }
    
    @Override
    public void haltMove() {
        this.durability -= this.level.ELEVATOR_HALT_DECAY_PER_PIXEL_SEC * Math.abs(this.deltaY);
        this.deltaY = 0;
        super.haltMove();
    }
    
    public boolean toggleDoor() {
        if (this.openDoor = !this.openDoor) {
            haltMove();
            setTexture(this.level.ELEVATOR_OPEN_TEXTURE);
        } else {
            setTexture(this.level.ELEVATOR_CLOSED_TEXTURE);
            SceneDirector.getInstance().notifyDoorJustClosed();
        }
        return this.openDoor;
    }
    
    public boolean isDoorOpen() {
        return this.openDoor;
    }
    
    public Vector2 getRelativePosition() {
        return getPosition().getRelativeVector();
    }
    
    public boolean isBroken() {
        return this.durability <= 0;
    }
    
    public int getDurability() {
        return Math.round(this.durability);
    }
    
    public void giveTip(int tipCents) {
        this.tipCents += tipCents;
    }
    
    public int getTipTotal() {
        return this.tipCents;
    }
    
}
