package simulator.elevator.game.entity;

import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.game.scene.Scene;

public class Passenger extends Entity {
    
    private int happiness = 100;
    private final int startFloor;
    private final int endFloor;
    private final Scene scene;
    
    public Passenger(int startFloor, int endFloor, Texture texture, Scene scene) {
        super(null, texture); //TODO transform start floor into world coordinates
        this.startFloor = startFloor;
        this.endFloor = endFloor;
        this.scene = scene;
    }

}
