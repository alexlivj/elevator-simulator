package simulator.elevator.data.entity;

public class Passenger {
    
    private int happiness = 100;
    private final int startFloor;
    private final int endFloor;
    
    public Passenger(int startFloor, int endFloor) {
        this.startFloor = startFloor;
        this.endFloor = endFloor;
    }

}
