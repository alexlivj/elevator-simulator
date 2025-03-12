package simulator.elevator.game.scene;

import simulator.elevator.game.entity.passenger.PassengerState;

public enum SceneType {
    STAR,
    GREETING,
    TOO_FAR,
    ELEVATOR_FULL,
    DOOR_SLAM,
    UNHAPPINESS_RIDING,
    GIVING_TIP;
    
    public String getJSONKey() {
        String key = "";
        switch (this) {
        case STAR:
            key = "star";
            break;
        case GREETING:
            key = "greeting";
            break;
        case TOO_FAR:
            key = "too_far";
            break;
        case ELEVATOR_FULL:
            key = "elevator_full";
            break;
        case DOOR_SLAM:
            key = "door_slam";
            break;
        case UNHAPPINESS_RIDING:
            key = "unhappiness_riding";
            break;
        case GIVING_TIP:
            key = "giving_tip";
            break;
        }
        return key;
    }
    
    public PassengerState getState() {
        switch (this) {
        case STAR:
            return null;
        case GREETING:
            return PassengerState.LOADING;
        case TOO_FAR:
            return PassengerState.WAITING;
        case ELEVATOR_FULL:
            return PassengerState.WAITING;
        case DOOR_SLAM:
            return null;
        case UNHAPPINESS_RIDING:
            return PassengerState.RIDING;
        case GIVING_TIP:
            return PassengerState.UNLOADING;
        }
        return null;
    }
}
