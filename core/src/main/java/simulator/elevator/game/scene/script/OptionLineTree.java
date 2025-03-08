package simulator.elevator.game.scene.script;

import java.util.List;

import simulator.elevator.game.manager.GameStateManager;
import simulator.elevator.util.Pair;

public class OptionLineTree extends AbstractLineTree {
    
    protected static final int TIMEOUT_SEC = 8;

    private final List<Pair<String,AbstractLineTree>> playerOptions;
    private int selectedOptionIndex = -1;
    private boolean firstRender = true;
    
    public OptionLineTree(PortraitType portrait, List<Pair<String,AbstractLineTree>> playerOptions) {
        super(portrait);
        this.playerOptions = playerOptions;
    }
    
    public void setSelection(int selectIndex) {
        this.selectedOptionIndex = selectIndex;
    }
    
    @Override
    public void reset() {
        this.selectedOptionIndex = -1;
        this.firstRender = true;
        super.reset();
    }

    @Override
    protected String getLineForRender() {
        if (this.firstRender) {
            GameStateManager.getInstance().addPlayerOptionBoxes(this, this.playerOptions.size());
            this.firstRender = false;
        }
        
        String line = "";
        for (int i=0; i<playerOptions.size(); i++)
            line += (i+1)+") " + playerOptions.get(i).first + "\n\n";
        return line;
    }
    
    protected boolean isLineDone() {
        boolean done = this.selectedOptionIndex != -1 || this.timeInLineSec > TIMEOUT_SEC;
        
        if (done != this.done)
            GameStateManager.getInstance().clearPlayerOptions();
        
        return done;
    }

    @Override
    protected AbstractLineTree getNextLine() {
        int index = this.selectedOptionIndex;
        if (index == -1)
            index = 0;
        return playerOptions.get(index).second;
    }

    @Override
    protected void resetChildLines() {
        for (Pair<String,AbstractLineTree> p : this.playerOptions)
            p.second.reset();
    }
    
}
