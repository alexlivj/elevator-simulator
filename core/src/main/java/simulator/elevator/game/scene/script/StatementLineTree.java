package simulator.elevator.game.scene.script;

import com.badlogic.gdx.audio.Sound;

public class StatementLineTree extends AbstractLineTree {
    
    private final String line;
    private final Sound sfx;
    private final AbstractLineTree nextLine;
    
    public StatementLineTree(PortraitType portrait, Sound npcSFX, String npcLine, AbstractLineTree nextLine) {
        super(portrait);
        this.sfx = npcSFX;
        this.line = npcLine;
        this.nextLine = nextLine;
    }
    
    protected boolean isLineDone() {
        float doneTime = this.line.length()/AbstractLineTree.CHAR_PER_SEC;
        return this.timeInLineSec >= doneTime;
    }
    
    @Override
    protected String getLineForRender() {
        return this.line;
    }
    
    @Override
    protected AbstractLineTree getNextLine() {
        return this.nextLine;
    }

    @Override
    protected void resetChildLines() {
        if (this.nextLine != null)
            this.nextLine.reset();
    }

}
