package simulator.elevator.game.scene;

import simulator.elevator.game.entity.passenger.Passenger;
import simulator.elevator.game.entity.passenger.PassengerPersonality;
import simulator.elevator.util.Pair;

public class SceneRequirements {
    
    private enum PersonalityTrait {
        PATIENCE,
        GENEROSITY,
        SPEED;
    }

    private final Pair<PassengerPersonality,PassengerPersonality> personalityBounds;
    private final Pair<Float,Float> hapinessBounds;
    
    public SceneRequirements(Pair<PassengerPersonality,PassengerPersonality> personalityBounds,
                             Pair<Float,Float> hapinessBounds) {
        this.personalityBounds = personalityBounds;
        this.hapinessBounds = hapinessBounds;
    }
    
    public boolean isValidPassenger(Passenger passenger) {
        PassengerPersonality personality = passenger.getPersonality();
        boolean validPatience = isInBounds(personality.patience(), fetchBounds(PersonalityTrait.PATIENCE));
        boolean validGenerosity = isInBounds(personality.generosity(), fetchBounds(PersonalityTrait.GENEROSITY));
        boolean validSpeed = isInBounds(personality.speedPixelSec(), fetchBounds(PersonalityTrait.SPEED));
        boolean validPersonality = validPatience && validGenerosity && validSpeed;
                
        boolean validHappiness = isInBounds(passenger.getHappiness(), this.hapinessBounds);
        
        return validPersonality && validHappiness;
    }
    
    public PassengerPersonality bindPersonality(PassengerPersonality personality) {
        PassengerPersonality newPersonality = null;

        int speed = personality.speedPixelSec();
        float patience = personality.patience();
        float generosity = personality.generosity();
        
        speed = Math.round(Math.max(this.personalityBounds.first.speedPixelSec(), speed));
        speed = Math.round(Math.min(speed, this.personalityBounds.second.speedPixelSec()));
        patience = Math.max(this.personalityBounds.first.patience(), patience);
        patience = Math.min(patience, this.personalityBounds.second.patience());
        generosity = Math.max(this.personalityBounds.first.generosity(), generosity);
        generosity = Math.min(generosity, this.personalityBounds.second.patience());
        
        newPersonality = new PassengerPersonality(speed,patience,generosity);
        
        return newPersonality;
    }
    
    public Pair<Float,Float> fetchBounds(PersonalityTrait trait) {
        float minTrait = Float.MIN_VALUE;
        float maxTrait = Float.MAX_VALUE;
        switch (trait) {
        case PATIENCE:
            minTrait = this.personalityBounds.first.patience();
            maxTrait = this.personalityBounds.second.patience();
            break;
        case GENEROSITY:
            minTrait = this.personalityBounds.first.generosity();
            maxTrait = this.personalityBounds.second.generosity();
            break;
        case SPEED:
            minTrait = this.personalityBounds.first.speedPixelSec();
            maxTrait = this.personalityBounds.second.speedPixelSec();
            break;
        }
        
        return new Pair<Float,Float>(minTrait,maxTrait);
    }
    
    public boolean isInBounds(float v, Pair<Float,Float> bounds) {
        return bounds.first < v && v < bounds.second;
    }

}
