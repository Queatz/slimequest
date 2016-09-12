package com.slimequest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.shared.GameNetworkEvent;

public class SlimeQuestGame extends ApplicationAdapter implements InputProcessor {

    // The game camera
    private OrthographicCamera cam;

    // The game viewport size, as a square
    private int size = 1000;

    // The player movement sensitivity
    private float sensitivity = 32;

    // The zoom applied to the camera
    private float zoom;

	@Override
	public void create() {
        // Set up game
        Gdx.input.setInputProcessor(this);
        Game.networking = new GameNetworking();
        Game.networking.start();

        // Set up world drawing
        cam = new OrthographicCamera();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Game.batch.enableBlending();
        Game.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void update() {
        Game.world.update();

        if (dragging != null && Game.player != null) {
            Game.player.addPos(
                    (int) (dragging.x - start.x) * zoom / sensitivity,
                    -(int) (dragging.y - start.y) * zoom / sensitivity
            );
        }
    }

	@Override
	public void render() {
        update();

        // Center camera on player
        if (Game.player != null) {
            cam.position.x = Game.player.pos.x;
            cam.position.y = Game.player.pos.y;
            cam.update();
        }

        // Clear device screen with black
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Setup & clear viewport
        Gdx.gl.glScissor(
                (int) (Gdx.graphics.getWidth() / 2 - size / zoom / 2),
                (int) (Gdx.graphics.getHeight() / 2 - size / zoom / 2),
                (int) (size / zoom),
                (int) (size / zoom)
        );
        Gdx.gl.glClearColor(.2f, .8f, 0.5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the world
        Game.batch.setProjectionMatrix(cam.combined);
        Game.batch.begin();
        Game.world.render();
        Game.batch.end();
	}
	
	@Override
	public void dispose() {
		Game.batch.dispose();
		GameResources.dispose();
	}

    @Override
    public void resize(int width, int height) {
        float ss = Math.min(width, height);

        // Save world unit / viewport unit
        zoom = size / ss;

        // Set camera metrics
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.zoom = zoom;
        cam.update();
        Game.batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    private Vector2 dragging;
    private Vector2 start = new Vector2();

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0) {
            return false;
        }

        start.x = screenX;
        start.y = screenY;
        dragging = new Vector2(start);

        return true;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (dragging == null) {
            return false;
        }

        dragging.x = screenX;
        dragging.y = screenY;

        return true;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) {
            return false;
        }

        dragging = null;

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
}
