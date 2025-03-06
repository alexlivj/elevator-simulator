package simulator.elevator.game.entity.passenger;

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
}
