package simulator.elevator.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

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
        ScreenUtils.clear(Color.BLACK);
        this.game.batch.begin();
        
        boolean end = this.game.manager.render(this.game, deltaSec);
        
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
