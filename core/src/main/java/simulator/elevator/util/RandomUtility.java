package simulator.elevator.util;

public class RandomUtility {

    public static float getRandomRange(float first, float second) {
        return (float)(first + Math.random()*(second-first));
    }

    public static int getRandomIntRange(float first, float second) {
        return (int) Math.round(first + Math.random()*(second-first));
    }
    
}
