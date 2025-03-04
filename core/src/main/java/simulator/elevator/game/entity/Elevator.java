package simulator.elevator.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import simulator.elevator.Main;
import simulator.elevator.game.RelativeCoordinate;

public class Elevator extends LinearEntity {
    
    private int durability = 100;
    private boolean openDoor = true;
    
    private static final Texture ELEVATOR_TEXTURE;
    static {
        Pixmap pm = new Pixmap(Gdx.files.internal("elevator.png"));
        Pixmap pmdoubled = new Pixmap(800, 560, pm.getFormat());
        pmdoubled.drawPixmap(pm,
                0, 0, pm.getWidth(), pm.getHeight(),
                0, 0, pmdoubled.getWidth(), pmdoubled.getHeight()
        );
        ELEVATOR_TEXTURE = new Texture(pmdoubled);
        pm.dispose();
        pmdoubled.dispose();
    }
    
    public Elevator(RelativeCoordinate pos) {
        
        super(pos, ELEVATOR_TEXTURE); //TODO constant for elevator texture
    }
    
    @Override
    public void render(Main game) {
        super.render(game);
    }
    
}
