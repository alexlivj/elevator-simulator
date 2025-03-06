package simulator.elevator.game.scene.script;

public abstract class AbstractLineTree {
    
    private static final int CHAR_PER_SEC = 10;

    private final PortraitType portrait;
    
    protected boolean isLineDone;
    protected float timeInLineSec = 0;

    public AbstractLineTree(PortraitType portrait) {
        this.portrait = portrait;
    }
    
    public boolean render(float deltaSec) {
        boolean finished = false;
        
        if (this.isLineDone) {
            AbstractLineTree nextLine = getNextLine();
            if (nextLine == null)
                finished = true;
            else
                getNextLine().render(deltaSec);
        } else {
            //TODO actually render on screen
            System.out.println("---");
            System.out.println(getLineForRender());
            System.out.println("---");
            this.timeInLineSec += deltaSec;
            this.isLineDone = true;
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
