package simulator.elevator.game.entity;

public class Passenger extends Entity {
    
    private int happiness = 100;
    private final int startFloor;
    private final int endFloor;
    
    public Passenger(int startFloor, int endFloor) {
        super(null); //TODO transform start floor into world coordinates
        this.startFloor = startFloor;
        this.endFloor = endFloor;
    }

}
