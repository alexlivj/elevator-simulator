package simulator.elevator.game.scene.script;

import com.badlogic.gdx.audio.Sound;

public class NpcLineTree extends AbstractLineTree {
    
    private final String npcLine;
    private final Sound npcSFX;
    private final AbstractLineTree nextLine;
    
    public NpcLineTree(PortraitType portrait, Sound npcSFX, String npcLine, AbstractLineTree nextLine) {
        super(portrait);
        this.npcSFX = npcSFX;
        this.npcLine = npcLine;
        this.nextLine = nextLine;
    }
    
    @Override
    protected String getLineForRender() {
        return this.npcLine;
    }
    
    @Override
    protected AbstractLineTree getNextLine() {
        return this.nextLine;
    }

    @Override
    protected void resetChildLines() {
        this.nextLine.reset();
    }

}
