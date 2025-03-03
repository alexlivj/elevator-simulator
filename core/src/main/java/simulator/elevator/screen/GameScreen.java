package simulator.elevator.screen;

import com.badlogic.gdx.Screen;

import simulator.elevator.Main;

public class GameScreen implements Screen {
    
    private Main game;
    
    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        this.game.manager.reset();
    }

    @Override
    public void render(float deltaSec) {
        this.game.manager.render(this.game, deltaSec);
    }

    @Override
    public void resize(int width, int height) {
        // the window can't be resized
    }

    @Override
    public void pause() {
        this.game.manager.pause();
    }

    @Override
    public void resume() {
        this.game.manager.resume();
    }

    @Override
    public void hide() {
        // do nothing
    }

    @Override
    public void dispose() {
        // do nothing
    }
    
}
