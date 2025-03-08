package simulator.elevator.game.scene.script;

import simulator.elevator.Main;

public abstract class AbstractLineTree {
    
    protected static final int CHAR_PER_SEC = 8;

    private final PortraitType portrait;
    protected boolean done = false;
    protected float timeInLineSec = 0;

    public AbstractLineTree(PortraitType portrait) {
        this.portrait = portrait;
    }
    
    public boolean render(Main game, float deltaSec) {
        this.timeInLineSec += deltaSec;
        boolean finished = false;
        
        if (this.done) {
            AbstractLineTree nextLine = getNextLine();
            if (nextLine == null)
                finished = true;
            else
                getNextLine().render(game, deltaSec);
        } else {
            game.font.draw(game.batch, getLineForRender(), 100, 120);
            this.done = isLineDone();
        }
        
        return finished;
    }
    
    public void reset() {
        this.done = false;
        this.timeInLineSec = 0f;
        resetChildLines();
    }
    
    protected abstract String getLineForRender();
    
    protected abstract boolean isLineDone();
    
    protected abstract AbstractLineTree getNextLine();
    
    protected abstract void resetChildLines();
    
}
