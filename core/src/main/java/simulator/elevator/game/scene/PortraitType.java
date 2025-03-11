package simulator.elevator.game.scene;

import java.util.Arrays;
import java.util.Optional;

public enum PortraitType {
    PLAYER_NEUTRAL,
    NPC_NEUTRAL;
    
    public boolean isPlayerPortrait() {
        return this == PLAYER_NEUTRAL;
    }
    
    public static PortraitType getPortraitType(String JSONKey) {
        Optional<PortraitType> portrait = 
                Arrays.stream(PortraitType.values())
                .filter(type -> type.getJSONKey().equals(JSONKey))
                .findFirst();
        return portrait.isPresent() ? portrait.get() : null;
    }
    
    public String getJSONKey() {
        switch (this) {
        case PLAYER_NEUTRAL:
            return "player_neutral";
        case NPC_NEUTRAL:
            return "npc_neutral";
        }
        return null;
    }
}
