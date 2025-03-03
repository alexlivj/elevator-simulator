package simulator.elevator.game.entity;

import com.badlogic.gdx.math.Vector2;

import simulator.elevator.util.Pair;

public abstract class Entity {
    
    private Vector2 position;
    private Pair<Vector2,Vector2> path = null;
    private int speedPixelSec = 0;
    
    public Entity(Vector2 pos) {
        this.position = pos;
    }
    
    public void update(float deltaSec) {
        if (path != null) {
            // (v=(dest - pos)) * speed/∥v∥
            Vector2 direction =
                    new Vector2(this.path.second)
                    .sub(this.position)
                    .setLength(this.speedPixelSec);
            this.position.add(direction);
        }
    }
    
    public void moveTo(Vector2 dest, int speedPixelSec) {
        this.path = new Pair<Vector2, Vector2>(new Vector2(position), dest);
        this.speedPixelSec = speedPixelSec;
    }
    
    public void haltMove() {
        this.path = null;
        this.speedPixelSec = 0;
    }
    
    public void cancelMove() {
        if (path != null) {
            this.position = this.path.first;
        }
    }

}
