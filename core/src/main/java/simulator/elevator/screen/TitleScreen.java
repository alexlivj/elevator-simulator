package simulator.elevator.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import simulator.elevator.Main;

public class TitleScreen implements Screen {
    
    private Main game;
    
    public TitleScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        this.game.setScreen(new GameScreen(this.game)); // I'm gonna work on the game screen first
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }
    
}
