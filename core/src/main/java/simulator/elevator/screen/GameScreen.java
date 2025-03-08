package simulator.elevator.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import simulator.elevator.Main;
import simulator.elevator.game.manager.GameStateManager;

public class GameScreen implements Screen {
    
    private Main game;
    
    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameStateManager.getInstance().reset();
        Gdx.input.setInputProcessor(GameStateManager.getInstance());
    }

    @Override
    public void render(float deltaSec) {
        ScreenUtils.clear(Color.BLACK);

        this.game.batch.setProjectionMatrix(this.game.camera.combined);
        this.game.batch.begin();

        boolean end = GameStateManager.getInstance().render(this.game, deltaSec);
        
        this.game.batch.end();
        
        if (end)
            this.game.setScreen(new TitleScreen(this.game));
    }

    @Override
    public void resize(int width, int height) {
        // the window can't be resized
    }

    @Override
    public void pause() {
        GameStateManager.getInstance().pause();
    }

    @Override
    public void resume() {
        GameStateManager.getInstance().resume();
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
