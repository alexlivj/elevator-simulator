package simulator.elevator.data.scene;

import java.util.List;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Line {
    
    private static int CHAR_PER_SEC = 10;
    
    private final Image portrait;
    private final boolean playerLine;
    private final String npcLine;
    private final Sound npcSFX;
    private final List<String> playerOptions;
    
    public Line(Image portrait, boolean playerLine,
                String npcLine, Sound npcSFX,
                List<String> playerOptions) {
        this.portrait = portrait;
        this.playerLine = playerLine;
        this.npcLine = npcLine;
        this.npcSFX = npcSFX;
        this.playerOptions = playerOptions;
    }
    
    public boolean render(float timeInLine) {
        boolean finished = false;
        return finished;
    }

}
