package simulator.elevator.game.scene.script;

import java.util.List;

import simulator.elevator.util.Pair;

public class OptionLineTree extends AbstractLineTree {

    private final List<Pair<String,AbstractLineTree>> playerOptions;
    private int selectedOptionIndex = -1;
    
    public OptionLineTree(PortraitType portrait, List<Pair<String,AbstractLineTree>> playerOptions) {
        super(portrait);
        this.playerOptions = playerOptions;
    }

    @Override
    protected String getLineForRender() {
        String line = "";
        for (int i=0; i<playerOptions.size(); i++)
            System.out.println(i+") "+playerOptions.get(i).first);
        return line;
    }

    @Override
    protected AbstractLineTree getNextLine() {
        if (this.selectedOptionIndex >= 0)
            return playerOptions.get(this.selectedOptionIndex).second;
        return null;
    }

    @Override
    protected void resetChildLines() {
        for (Pair<String,AbstractLineTree> p : this.playerOptions)
            p.second.reset();
    }
    
}
