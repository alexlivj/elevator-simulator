package simulator.elevator.game;

import simulator.elevator.game.entity.Elevator;

public class GameState {
    
    private static final int GAME_TIME_SEC = 120;
    
    private float timeRemaining;
    private Elevator elevator;
    
    public GameState() {
        reset();
    }
    
    public void reset() {
        this.timeRemaining = GAME_TIME_SEC;
        this.elevator = new Elevator(null); //TODO elevator starting location
    }
    
}
