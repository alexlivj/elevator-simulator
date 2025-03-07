package simulator.elevator.game.scene.script;

public enum PortraitType {
    PLAYER_NEUTRAL,
    NPC_NEUTRAL;
    
    public boolean isPlayerPortrait() {
        return this == PLAYER_NEUTRAL;
    }
}
