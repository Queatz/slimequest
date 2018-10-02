package com.slimequest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.slimequest.game.screens.MenuScreen;
import com.slimequest.game.screens.Screen;
import com.slimequest.game.screens.ScreenManager;

public class SlimeQuestGame extends ApplicationAdapter implements InputProcessor, ScreenManager {

    private Screen screen = new MenuScreen(this);

    @Override
    public void goToScreen(Screen screen) {
        if (this.screen != null) {
            this.screen.pause();
            this.screen.dispose();
        }

        this.screen = screen;
        this.screen.create();
        this.screen.resume();
    }

    @Override
	public void create() {
        screen.create();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void pause() {
        screen.pause();
    }

    @Override
    public void resume() {
        screen.resume();
    }

	@Override
	public void render() {
        screen.render();
    }

	@Override
	public void dispose() {
        screen.dispose();
    }

    @Override
    public void resize(int width, int height) {
        screen.resize(width, height);
    }

    @Override
    public boolean scrolled(int amount) {
        return screen.scrolled(amount);
    }

    @Override
    public boolean keyDown(int keycode) {
        return screen.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return screen.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return screen.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return screen.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return screen.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return screen.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return screen.mouseMoved(screenX, screenY);
    }
}