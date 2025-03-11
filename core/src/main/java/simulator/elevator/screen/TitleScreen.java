package simulator.elevator.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import simulator.elevator.Main;
import simulator.elevator.game.entity.Elevator;
import simulator.elevator.game.manager.GameStateManager;

public class TitleScreen implements Screen, InputProcessor {
    
    private Main game;
    private float timeInScreenSec = 0f;
    
    public TitleScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        this.timeInScreenSec = 0f;
    }

    @Override
    public void render(float delta) {
        this.timeInScreenSec += delta;
        
        ScreenUtils.clear(Color.BLACK);

        this.game.batch.setProjectionMatrix(this.game.camera.combined);
        this.game.batch.begin();
        
        if (!GameStateManager.getInstance().isFinished()) {
            this.game.setScreen(new GameScreen(this.game));
        } else {
            String tipStr = GameStateManager.getInstance().getTipStr();
            this.game.font.draw(this.game.batch, "You collected "+tipStr, 350, 300);
            Elevator elevator = GameStateManager.getInstance().getElevator();
            if (elevator.isBroken()) {
                String conjunction = elevator.getTipTotal() == 0 ? "and" : "but";
                this.game.font.draw(this.game.batch, conjunction+" you broke the elevator!", 450, 250);
                this.game.font.draw(this.game.batch, "great job bozo", 440, 220);
            }
        }
        
        this.game.batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (this.timeInScreenSec > 3)
            this.game.setScreen(new GameScreen(this.game));
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (this.timeInScreenSec > 3)
            this.game.setScreen(new GameScreen(this.game));
        return true;
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

    @Override
    public boolean keyUp(int keycode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
