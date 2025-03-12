package simulator.elevator.game.scene;

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
}
