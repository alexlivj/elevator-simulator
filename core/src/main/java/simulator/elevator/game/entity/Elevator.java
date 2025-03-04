package simulator.elevator.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Main;
import simulator.elevator.game.RelativeCoordinate;

public class Elevator extends LinearEntity {
    
    private int durability = 100;
    private boolean openDoor = true;

    private static final Texture ELEVATOR_OPEN_TEXTURE;
    static {
        Pixmap pm = new Pixmap(Gdx.files.internal("elevator-open.png"));
        Pixmap pmdoubled = new Pixmap(800, 560, pm.getFormat());
        pmdoubled.drawPixmap(pm,
                0, 0, pm.getWidth(), pm.getHeight(),
                0, 0, pmdoubled.getWidth(), pmdoubled.getHeight()
        );
        ELEVATOR_OPEN_TEXTURE = new Texture(pmdoubled);
        pm.dispose();
        pmdoubled.dispose();
    }
    private static final Texture ELEVATOR_CLOSED_TEXTURE;
    static {
        Pixmap pm = new Pixmap(Gdx.files.internal("elevator-closed.png"));
        Pixmap pmdoubled = new Pixmap(800, 560, pm.getFormat());
        pmdoubled.drawPixmap(pm,
                0, 0, pm.getWidth(), pm.getHeight(),
                0, 0, pmdoubled.getWidth(), pmdoubled.getHeight()
        );
        ELEVATOR_CLOSED_TEXTURE = new Texture(pmdoubled);
        pm.dispose();
        pmdoubled.dispose();
    }
    
    public Elevator(RelativeCoordinate pos) {
        super(pos, ELEVATOR_OPEN_TEXTURE); //TODO constant for elevator texture
    }
    
    @Override
    public void render(Main game) {
        super.render(game);
    }
    
    public void move(int dy) {
        RelativeCoordinate pos = getPosition();
        Vector2 newRel = new Vector2(pos.getRelativeVector()).add(new Vector2(0,dy));
        moveTo(new RelativeCoordinate(pos.getOrigin(), newRel), Math.abs(dy));
    }
    
    public void toggleDoor() {
        if (this.openDoor = !this.openDoor)
            setTexture(ELEVATOR_OPEN_TEXTURE);
        else
            setTexture(ELEVATOR_CLOSED_TEXTURE);
    }
    
}
