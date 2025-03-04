package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.GameStateManager;
import simulator.elevator.game.RelativeCoordinate;
import simulator.elevator.util.Pair;
import simulator.elevator.util.TextureUtility;

public class Elevator extends LinearEntity {
    
    private final Pair<Integer,Integer> yAxisBound;
    private int durability = 100;
    private boolean openDoor = true;

    private static final Texture ELEVATOR_OPEN_TEXTURE =
            TextureUtility.doubleTextureSize("elevator-open.png");
    private static final Texture ELEVATOR_CLOSED_TEXTURE =
            TextureUtility.doubleTextureSize("elevator-closed.png");
    
    public Elevator(RelativeCoordinate pos, Pair<Integer,Integer> yAxisBound) {
        super(pos, ELEVATOR_OPEN_TEXTURE); //TODO constant for elevator texture
        this.yAxisBound = yAxisBound;
    }
    
    @Override
    public void render(Main game) {
        super.render(game);
    }
    
    public void move(int dy, float deltaSec) {
        RelativeCoordinate pos = getPosition();
        float posRelY = pos.getRelativeVector().y;

        if ((posRelY < this.yAxisBound.first && dy < 0)
                || (this.yAxisBound.second < posRelY && 0 < dy)) {
            this.durability -= GameStateManager.ELEVATOR_DECAY_RATE_SEC * deltaSec;
            haltMove();
        } else {
            Vector2 newRel = new Vector2(pos.getRelativeVector()).add(new Vector2(0,dy));
            moveTo(new RelativeCoordinate(pos.getOrigin(), newRel), Math.abs(dy));
        }
    }
    
    public void toggleDoor() {
        if (this.openDoor = !this.openDoor)
            setTexture(ELEVATOR_OPEN_TEXTURE);
        else
            setTexture(ELEVATOR_CLOSED_TEXTURE);
    }
    
    public boolean isDoorOpen() {
        return this.openDoor;
    }
    
    public Vector2 getRelativePosition() {
        return getPosition().getRelativeVector();
    }
    
    public int getDurability() {
        return this.durability;
    }
    
}
