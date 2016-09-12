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
    private OrthographicCamera cam;
    private int size = 1000;
    private float zoom;

	@Override
	public void create() {
        cam = new OrthographicCamera();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.input.setInputProcessor(this);

        Game.networking = new GameNetworking();
        Game.networking.start();
    }



	@Override
	public void render() {

        if (dragging != null) {
            // XXX TODO make real movement, i.e. Game.player.moveBy(x, y)
            Game.player.x += (int) (dragging.x - start.x) * zoom / 32;
            Game.player.y -= (int) (dragging.y - start.y) * zoom / 32;
        }

        // Center camera

        if (Game.player != null) {
            cam.position.x = Game.player.x;
            cam.position.y = Game.player.y;
            cam.update();
        }

        // Clear screen with black

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Set viewport

        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        Gdx.gl.glScissor(
                (int) (w / 2 - size / zoom / 2),
                (int) (h / 2 - size / zoom / 2),
                (int) (size / zoom),
                (int) (size / zoom)
        );

        // Clear viewport

        Gdx.gl.glClearColor(.2f, .8f, 0.5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render world

        Game.batch.setProjectionMatrix(cam.combined);


        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Game.batch.enableBlending();
        Game.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
        zoom = size / ss;

        if (cam == null) {
            cam = new OrthographicCamera(width, height);
        } else {
            cam.viewportWidth = width;
            cam.viewportHeight = height;
        }

        cam.zoom = zoom;
        cam.update();
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
    private Vector2 start;

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (Game.player == null) {
            return false;
        }

        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0) return false;
        start = new Vector2();
        start.x = screenX;
        start.y = screenY;

        dragging = new Vector2(start);

        return true;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (dragging == null) return false;

        dragging.x = screenX;
        dragging.y = screenY;

        return true;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) return false;
        dragging = null;

        // XXX TODO debounced and sent from Game.player.moveBy(x, y)
        if (Game.player != null) {
            JsonObject position = new JsonObject();
            position.add("id", new JsonPrimitive(Game.player.id));
            position.add("x", new JsonPrimitive(Game.player.x));
            position.add("y", new JsonPrimitive(Game.player.y));
            Game.networking.send(new GameNetworkEvent("move", position));
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
}
