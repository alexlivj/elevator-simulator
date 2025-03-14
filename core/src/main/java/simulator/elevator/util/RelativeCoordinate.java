package simulator.elevator.util;

import com.badlogic.gdx.math.Vector2;

/**
 * Holds a 2D coordinate using some arbitrary point as its origin, which itself
 * can be relative. Allows entities to smoothly move on the surface of another
 * moving entity. Also facilitates the game's camera.
 */
public class RelativeCoordinate {
    
    private RelativeCoordinate originOffset;
    private Vector2 relativeVector;
    
    public RelativeCoordinate(RelativeCoordinate originOffset, Vector2 relativeVector) {
        this.originOffset = originOffset;
        this.relativeVector = relativeVector;
    }
    
    public RelativeCoordinate(RelativeCoordinate original) {
        this.set(original);
    }
    
    public void set(RelativeCoordinate original) {
        this.originOffset = original.originOffset;
        this.relativeVector = new Vector2(original.relativeVector);
    }
    
    public RelativeCoordinate getOrigin() {
        return this.originOffset;
    }
    
    public Vector2 getRelativeVector() {
        return this.relativeVector;
    }
    
    public void setAbsoluteVector(Vector2 abs) {
        if (this.originOffset == null)
            this.relativeVector = new Vector2(abs);
        else
            this.relativeVector = new Vector2(abs).sub(this.originOffset.getAbsoluteVector());
    }
    
    // this is probably memory inefficient, but fine for this project
    public Vector2 getAbsoluteVector() {
        if (this.originOffset == null)
            return new Vector2(this.relativeVector);
        else
            return new Vector2(this.relativeVector).add(this.originOffset.getAbsoluteVector());
    }
    
    public void rebaseOrigin(RelativeCoordinate newOrigin) {
        Vector2 currAbs = this.getAbsoluteVector();
        Vector2 newAbsOffset = newOrigin.getAbsoluteVector();
        
        this.originOffset = newOrigin;
        this.relativeVector = new Vector2(currAbs).sub(newAbsOffset);
    }
}
