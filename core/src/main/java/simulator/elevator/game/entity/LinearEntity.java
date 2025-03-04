package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.RelativeCoordinate;
import simulator.elevator.util.Pair;

public abstract class LinearEntity {

    // position MUST remain the same obj, so that it can be used as a relative origin
    private final RelativeCoordinate position;
    private Pair<RelativeCoordinate,RelativeCoordinate> path = null;
    private int speedPixelSec = 0;
    
    //TODO use animation handler, probably
    private Texture texture;
    
    public LinearEntity(RelativeCoordinate pos, Texture texture) {
        this.position = pos;
        this.texture = texture;
    }
    
    public void update(float deltaSec) {
        // move entity along its path, fetching its moved absolute position
        if (this.path != null) {
            Vector2 absDest = this.path.second.getAbsoluteVector();
            Vector2 oldAbsPos = this.position.getAbsoluteVector();
            
            // (v=(dest - pos)) * delta*speed/∥v∥
            Vector2 absPos =
                    new Vector2(absDest)
                     .sub(oldAbsPos);

            float distance =  this.speedPixelSec*deltaSec;
            if (Math.abs(absPos.len()) <= distance) {
                absPos.set(absDest);
                this.haltMove();
            } else {
                absPos.setLength(distance);
            }
            
            this.position.setAbsoluteVector(absPos.add(oldAbsPos));
        }
    }
    
    public void render(Main game) {
        // get the actual screen pixel coordinates
        Vector2 absPos = this.position.getAbsoluteVector();
        
        // draw our texture
        game.batch.draw(texture, absPos.x, absPos.y);
    }
    
    public void rebaseOrigin(RelativeCoordinate newOrigin) {
        this.position.rebaseOrigin(newOrigin);
    }
    
    public void moveTo(RelativeCoordinate dest, int speedPixelSec) {
        this.speedPixelSec = speedPixelSec;
        this.path = new Pair<RelativeCoordinate, RelativeCoordinate>(new RelativeCoordinate(this.position), dest);
        if (this.position.getOrigin() != dest.getOrigin())
            this.position.rebaseOrigin(dest.getOrigin());
    }
    
    public void haltMove() {
        this.path = null;
        this.speedPixelSec = 0;
    }
    
    public void cancelMove() {
        //NOTE if this was rebased during the move message, this method reverts to previous origin
        if (path != null)
            this.position.set(this.path.first);
    }
    
    protected RelativeCoordinate getPosition() {
        return this.position;
    }

}
