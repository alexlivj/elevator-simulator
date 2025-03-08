package simulator.elevator.game.scene.script;

import simulator.elevator.Main;

public abstract class AbstractLineTree {
    
    private static final int CHAR_PER_SEC = 8;

    private final PortraitType portrait;
    protected boolean isLineDone;
    protected float timeInLineSec = 0;

    public AbstractLineTree(PortraitType portrait) {
        this.portrait = portrait;
    }
    
    public boolean render(Main game, float deltaSec) {
        boolean finished = false;
        
        if (this.isLineDone) {
            AbstractLineTree nextLine = getNextLine();
            if (nextLine == null)
                finished = true;
            else
                getNextLine().render(game, deltaSec);
        } else {
            this.timeInLineSec += deltaSec;
            float doneTime = getLineForRender().length()/AbstractLineTree.CHAR_PER_SEC;
            game.font.draw(game.batch, getLineForRender(), 100, 120);
            this.isLineDone = this.timeInLineSec >= doneTime;
        }
        
        return finished;
    }
    
    public void reset() {
        this.isLineDone = false;
        this.timeInLineSec = 0f;
        resetChildLines();
    }
    
    protected abstract String getLineForRender();
    
    protected abstract AbstractLineTree getNextLine();
    
    protected abstract void resetChildLines();
    
}
