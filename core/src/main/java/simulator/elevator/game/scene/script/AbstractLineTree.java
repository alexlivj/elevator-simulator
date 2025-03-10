package simulator.elevator.game.scene.script;

import simulator.elevator.Main;
import simulator.elevator.util.Pair;

public abstract class AbstractLineTree {
    
    protected static final int CHAR_PER_SEC = 8;

    private final PortraitType portrait;
    protected boolean done = false;
    protected float timeInLineSec = 0;

    public AbstractLineTree(PortraitType portrait) {
        this.portrait = portrait;
    }
    
    public Pair<OptionConsequence,LineReturn> render(Main game, float deltaSec) {
        this.timeInLineSec += deltaSec;
        Pair<OptionConsequence,LineReturn> lineOut = 
                new Pair<OptionConsequence,LineReturn>(null,LineReturn.CONTINUE);
        
        if (this.done) {
            AbstractLineTree nextLine = getNextLine();
            if (nextLine == null)
                lineOut = new Pair<OptionConsequence,LineReturn>(null,LineReturn.FINISH);
            else
                lineOut = getNextLine().render(game, deltaSec);
        } else {
            game.font.draw(game.batch, getLineForRender(), 100, 120);
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
