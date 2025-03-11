package simulator.elevator.game.scene;

import java.util.Map;

import simulator.elevator.game.entity.passenger.PassengerState;

public record StarRole(Map<PassengerState,Scene> scenes, CastingDirection requirements) {

}
