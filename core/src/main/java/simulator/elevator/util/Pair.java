package simulator.elevator.util;

public class Pair<T1, T2> {

    public final T1 first;
    public final T2 second;
    
    public Pair(T1 f, T2 s) {
        this.first = f;
        this.second = s;
    }
    
    @Override
    public String toString() {
        return "<"+first+","+second+">";
    }

}
