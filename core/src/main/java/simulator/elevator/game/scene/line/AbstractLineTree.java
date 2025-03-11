package simulator.elevator.game.scene.line;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import simulator.elevator.Level;
import simulator.elevator.Main;
import simulator.elevator.game.scene.PortraitType;
import simulator.elevator.util.Pair;

public abstract class AbstractLineTree {
    
    protected static final int CHAR_PER_SEC = 8;

    private final PortraitType portrait;
    protected boolean done = false;
    protected float timeInLineSec = 0;

    public AbstractLineTree(PortraitType portrait) {
        this.portrait = portrait;
    }
    
    public Pair<OptionConsequence,LineReturn> render(Main game, Level level, Color color, float deltaSec) {
        this.timeInLineSec += deltaSec;
        Pair<OptionConsequence,LineReturn> lineOut = 
                new Pair<OptionConsequence,LineReturn>(null,LineReturn.CONTINUE);
        
        if (this.done) {
            AbstractLineTree nextLine = getNextLine();
            if (nextLine == null)
                lineOut = new Pair<OptionConsequence,LineReturn>(null,LineReturn.FINISH);
            else
                lineOut = getNextLine().render(game, level, color, deltaSec);
        } else {
            Vector2 portraitPos = 
                    this.portrait.isPlayerPortrait() ? level.PLAYER_PORTRAIT_POS : level.NPC_PORTRAIT_POS; 
            Vector2 textPos = 
                    this.portrait.isPlayerPortrait() ? level.PLAYER_TEXT_POS : level.NPC_TEXT_POS; 
            
            game.batch.setColor(color);
            game.batch.draw(level.PORTRAIT_TEXTURES.get(this.portrait), portraitPos.x, portraitPos.y);
            game.batch.setColor(Color.WHITE);
            game.font.draw(game.batch, getLineForRender(), textPos.x, textPos.y);
            
            lineOut = new Pair<OptionConsequence,LineReturn>(getConsequence(),LineReturn.CONTINUE);
            this.done = isLineDone();
        }
        
        return lineOut;
    }
    
    public void reset() {
        this.done = false;
        this.timeInLineSec = 0f;
        resetChildLines();
    }
    
    protected abstract String getLineForRender();
    
    protected abstract boolean isLineDone();
    
    protected abstract AbstractLineTree getNextLine();
    
    protected abstract OptionConsequence getConsequence();
    
    protected abstract void resetChildLines();
    
}
