package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.RelativeCoordinate;
import simulator.elevator.util.Pair;

public abstract class Entity {

    // position MUST remain the same obj, so that it can be used as a relative origin
    private final RelativeCoordinate position;
    private Pair<RelativeCoordinate,RelativeCoordinate> path = null;
    private int speedPixelSec = 0;
    
    private Texture texture;
    
    public Entity(RelativeCoordinate pos, Texture texture) {
        this.position = pos;
        this.texture = texture;
    }
    
    public void render(Main game, float deltaSec) {
        // move entity along its path, fetching its moved absolute position
        Vector2 absPos;
        if (path != null) {
            Vector2 absDest = this.path.second.getAbsoluteVector();
            Vector2 oldAbsPos = this.position.getAbsoluteVector();
            // (v=(dest - pos)) * delta*speed/∥v∥
            absPos = new Vector2(absDest)
                     .sub(oldAbsPos)
                     .setLength(this.speedPixelSec * deltaSec);
            this.position.setAbsoluteVector(absPos);
        } else {
            absPos = this.position.getAbsoluteVector();
        }
        
        // draw
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

}
