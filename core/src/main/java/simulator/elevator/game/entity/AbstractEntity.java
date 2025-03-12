package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.util.Pair;
import simulator.elevator.util.RelativeCoordinate;

public abstract class AbstractEntity {

    // position MUST remain the same object in memory, so that it can be used as a relative origin
    private final RelativeCoordinate position;
    private Pair<RelativeCoordinate,RelativeCoordinate> path = null;
    private int speedPixelSec = 0;
    
    //TODO use animation handler, probably
    private Texture texture;
    
    public AbstractEntity(RelativeCoordinate pos, Texture texture) {
        this.position = pos;
        this.texture = texture;
    }
    
    public void update(float deltaSec) {
        // move entity along its path, fetching its moved absolute position
        if (this.path != null) {
            Vector2 absDest = this.path.second.getAbsoluteVector();
            Vector2 oldAbsPos = this.position.getAbsoluteVector();
            
            // (v=(dest - pos)) * delta*speed/∥v∥
            Vector2 absDelta =
                    new Vector2(absDest)
                     .sub(oldAbsPos);

            float distance =  this.speedPixelSec*deltaSec;
            Vector2 newAbsPos;
            if (Math.abs(absDelta.len()) <= distance) {
                newAbsPos = new Vector2(absDest);
                this.haltMove();
            } else {
                absDelta.setLength(distance);
                newAbsPos = new Vector2(absDelta).add(oldAbsPos);
            }

            this.position.setAbsoluteVector(newAbsPos);
        }
    }
    
    public void render(Main game) {
        Vector2 absPos = this.position.getAbsoluteVector();
        game.batch.draw(texture, absPos.x, absPos.y);
    }
    
    public void rebaseOrigin(RelativeCoordinate newOrigin) {
        this.position.rebaseOrigin(newOrigin);
    }
    
    public void moveTo(RelativeCoordinate dest, int speedPixelSec) {
        this.speedPixelSec = speedPixelSec;
        this.path = new Pair<RelativeCoordinate, RelativeCoordinate>(new RelativeCoordinate(this.position),
                                                                     dest);
        if (this.position.getOrigin() != dest.getOrigin())
            this.position.rebaseOrigin(dest.getOrigin());
    }
    
    public void haltMove() {
        this.path = null;
        this.speedPixelSec = 0;
    }
    
    public void cancelMove() {
        if (path != null)
            this.position.set(this.path.first);
        haltMove();
    }
    
    public boolean isMoving() {
        return this.path != null;
    }
    
    public RelativeCoordinate getPosition() {
        return this.position;
    }
    
    public float getAbsDistanceFromStart() {
        if (path == null) {
            return 0;
        } else {
            Vector2 pos = this.position.getAbsoluteVector();
            Vector2 start = this.path.first.getAbsoluteVector();
            return new Vector2(pos).sub(start).len();
        }
    }
    
    protected void setTexture(Texture texture) {
        this.texture = texture;
    }

}
