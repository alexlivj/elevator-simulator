package simulator.elevator.game.scene;

import java.util.Map;

import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.game.entity.passenger.PassengerState;

public record StarScene(Map<PassengerState,Scene> scenes, 
        PassengerPersonality minPersonality, PassengerPersonality maxPersonality) {

}
