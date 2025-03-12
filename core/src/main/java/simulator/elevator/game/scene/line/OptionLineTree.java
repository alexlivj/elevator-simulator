package simulator.elevator.game.scene.line;

import java.util.List;

import simulator.elevator.game.manager.GameStateManager;
import simulator.elevator.game.scene.PortraitType;

public class OptionLineTree extends AbstractLineTree {
    
    protected static final int TIMEOUT_SEC = 8;

    private final List<Option> playerOptions;
    private int selectedOptionIndex = -1;
    private boolean firstRender = true;
    
    public OptionLineTree(PortraitType portrait, List<Option> playerOptions) {
        super(portrait);
        this.playerOptions = playerOptions;
    }
    
    public void setSelection(int selectIndex) {
        this.selectedOptionIndex = selectIndex;
    }
    
    @Override
    public void reset() {
        GameStateManager.getInstance().clearPlayerOptions();
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
            line += (i+1)+") " + playerOptions.get(i).line() + "\n\n";
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
        return playerOptions.get(index).next();
    }

    @Override
    protected void resetChildLines() {
        for (Option o : this.playerOptions)
            if (o.next() != null)
                o.next().reset();
    }

    @Override
    protected OptionConsequence getConsequence() {
        if (this.selectedOptionIndex >= 0)
            return this.playerOptions.get(this.selectedOptionIndex).consequence();
        
        return null;
    }
    
}
