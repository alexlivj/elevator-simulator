package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.manager.SceneDirector;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RelativeCoordinate;
import simulator.elevator.util.TextureUtility;

public class Elevator extends AbstractEntity {
    
    //TODO maybe read this from somewhere
    private static final int ELEVATOR_SPEED_PIXEL_SEC = 30;
    private static final int ELEVATOR_UNSAFE_SPEED_PIXEL_SEC = 10;
    private static final float ELEVATOR_DECAY_RATE_SEC = 3;
    
    private int deltaY = 0;
    private final Pair<Integer,Integer> yAxisBound;
    private float durability = 100;
    private boolean openDoor = true;
    private int tipCents = 0;

    private static final Texture ELEVATOR_OPEN_TEXTURE =
            TextureUtility.doubleTextureSize("elevator-open.png");
    private static final Texture ELEVATOR_CLOSED_TEXTURE =
            TextureUtility.doubleTextureSize("elevator-closed.png");
    
    public Elevator(RelativeCoordinate pos, Pair<Integer,Integer> yAxisBound) {
        super(pos, ELEVATOR_OPEN_TEXTURE);
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
                this.durability -= Elevator.ELEVATOR_DECAY_RATE_SEC * deltaSec;
                int dy = this.deltaY;
                haltMove();
                this.deltaY = dy; 
            } else {
                float absDeltaY = Math.abs(this.deltaY);
                if (absDeltaY > Elevator.ELEVATOR_UNSAFE_SPEED_PIXEL_SEC) {
                    float maxUnsafeDiff = Elevator.ELEVATOR_SPEED_PIXEL_SEC - Elevator.ELEVATOR_UNSAFE_SPEED_PIXEL_SEC;
                    float unsafeDeltaY = Elevator.ELEVATOR_SPEED_PIXEL_SEC - absDeltaY;
                    float unsafeMagnitude = 1 - unsafeDeltaY / maxUnsafeDiff;
                    this.durability -= unsafeMagnitude*Elevator.ELEVATOR_DECAY_RATE_SEC * deltaSec;
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
        this.deltaY = Math.round(maxSpeedFraction * Elevator.ELEVATOR_SPEED_PIXEL_SEC);
    }
    
    @Override
    public void haltMove() {
        this.deltaY = 0;
        super.haltMove();
    }
    
    public boolean toggleDoor() {
        if (this.openDoor = !this.openDoor) {
            haltMove();
            setTexture(ELEVATOR_OPEN_TEXTURE);
        } else {
            setTexture(ELEVATOR_CLOSED_TEXTURE);
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
