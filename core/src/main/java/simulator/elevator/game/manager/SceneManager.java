package simulator.elevator.game.manager;

import java.util.Queue;

import simulator.elevator.game.scene.Scene;

public class SceneManager {
    
    private Queue<Scene> scenes;
    
    public void queueScene(Scene scene) {
        scenes.add(scene);
    }
    
    public void render(float deltaSec) {
        if (scenes.peek().render(deltaSec))
            scenes.poll();
    }
    
    public void ejectCurrentScene() {
        scenes.peek().eject();
    }

}
