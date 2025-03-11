package simulator.elevator.game.entity.passenger;

import java.util.Arrays;
import java.util.Optional;

import simulator.elevator.game.scene.PortraitType;

public enum PassengerState implements Comparable<PassengerState> {
    ARRIVING(0),
    WAITING(1),
    LOADING(2),
    RIDING(3),
    UNLOADING(4),
    LEAVING(5);
    
    public final int value;
    
    private PassengerState(int value) {
        this.value = value;
    }
    
    public boolean isBeforeOrAt(PassengerState p) {
        return this.value <= p.value;
    }
    
    public String getJSONKey() {
        String key = "";
        switch (this) {
        case ARRIVING:
            key = "arriving";
            break;
        case WAITING:
            key = "waiting";
            break;
        case LOADING:
            key = "loading";
            break;
        case RIDING:
            key = "riding";
            break;
        case UNLOADING:
            key = "unloading";
            break;
        case LEAVING:
            key = "leaving";
            break;
        }
        return key;
    }
    
    public static PassengerState getPassengerState(String JSONKey) {
        Optional<PassengerState> state = 
                Arrays.stream(PassengerState.values())
                .filter(type -> type.getJSONKey().equals(JSONKey))
                .findFirst();
        return state.isPresent() ? state.get() : null;
    }
}
