package simulator.elevator.game.scene;

public enum SceneType {
    STAR,
    GREETING,
    GIVING_TIP,
    ELEVATOR_FULL,
    DOOR_SLAM,
    UNHAPPINESS_RIDING;
    
    public String getJSONKey() {
        String key = "";
        switch (this) {
        case GREETING:
            key = "greeting";
            break;
        case GIVING_TIP:
            key = "giving_tip";
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
        case STAR:
            key = "star";
            break;
        }
        return key;
    }
}
