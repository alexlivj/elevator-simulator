package simulator.elevator.game.scene;

import java.util.List;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Line {
    
    private static final int CHAR_PER_SEC = 10;
    
    private final Image portrait;
    private final boolean playerLine;
    private final String npcLine;
    private final Sound npcSFX;
    private final List<String> playerOptions;
    
    private float timeInLineSec = 0;
    
    public Line(Image portrait, boolean playerLine,
                String npcLine, Sound npcSFX,
                List<String> playerOptions) {
        this.portrait = portrait;
        this.playerLine = playerLine;
        this.npcLine = npcLine;
        this.npcSFX = npcSFX;
        this.playerOptions = playerOptions;
    }
    
    public boolean render(float deltaSec) {
        boolean finished = false;
        this.timeInLineSec += deltaSec;
        
        //TODO actually render on screen
        System.out.println("---");
        if (playerLine)
            for (int i=0; i<playerOptions.size(); i++)
                System.out.println(i+") "+playerOptions.get(i));
        else
            System.out.println(npcLine);
        System.out.println("---");
        finished = this.timeInLineSec > 5; //TODO
        
        return finished;
    }

}
