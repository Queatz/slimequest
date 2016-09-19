package com.slimequest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.slimequest.game.events.GameNetworkCreateObjectEvent;
import com.slimequest.game.events.GameNetworkEditTeleportTargetEvent;
import com.slimequest.game.events.GameNetworkRemoveObjectEvent;
import com.slimequest.game.game.MapObject;
import com.slimequest.game.game.MapTile;
import com.slimequest.game.game.Teleport;
import com.slimequest.game.game.World;
import com.slimequest.shared.GameType;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SlimeQuestGame extends ApplicationAdapter implements InputProcessor {

    // The game camera
    private OrthographicCamera cam;

    // The ui camera
    private OrthographicCamera uiCam;

    // The player movement sensitivity (lower is higher)
    private float sensitivity = 32;

    // Maximum speed a player can move
    private float maxPlayerSpeed = 2;

    // The zoom applied to the camera
    private float zoom;

    // Map editing tools
    private int tapCount;
    private Date lastTapUp = new Date();
    private boolean didChoosePaint;
    private int paintTile;
    private Vector2 lastTapPos;
    private Debouncer chooseTileDebouncer = new Debouncer(new Runnable() {
        @Override
        public void run() {
            if (Game.isEditing && tapCount == 1) {
                didChoosePaint = !didChoosePaint;
            }
        }
    }, 250);
    private String paintObject = null;
    private Teleport isEditingTeleport;

    // Pending actions from the server
    private ConcurrentLinkedQueue<RunInGame> runInGames = new ConcurrentLinkedQueue<>();

    @Override
	public void create() {
        Game.world = new World();
        Game.batch = new SpriteBatch();
        Game.shapeRenderer = new ShapeRenderer();

        // Set up game
        Game.game = this;
        Gdx.input.setInputProcessor(this);

        if (Game.networking == null) {
            Game.networking = new GameNetworking();
            Game.networking.start();
        }

        // Set up world drawing
        cam = new OrthographicCamera();
        uiCam = new OrthographicCamera();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Game.batch.enableBlending();
        Game.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("basis33.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        parameter.borderStraight = true;
        parameter.borderWidth = 1;
        Game.font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
//        if (Game.connectionError) {
//            if (Game.networking != null) {
//                Game.networking.close();
//            }
//            Game.networking = new GameNetworking();
//            Game.networking.start();
//        }

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void toggleEditing() {
        Game.isEditing = !Game.isEditing;

        if (Game.isEditing) {
            didChoosePaint = false;
        }
    }

    private void update() {
        Game.world.update();

        // Player movement
        if (dragging != null && Game.player != null && !Game.isEditing) {
            Vector2 pos = new Vector2(
                    (int) (dragging.x - start.x) * zoom / sensitivity,
                    -(int) (dragging.y - start.y) * zoom / sensitivity
            );

            // Find the desired movement speed
            float scl = Math.min(maxPlayerSpeed, pos.len());

            // Move in the desired direction at that speed
            Game.player.addPos(pos.nor().scl(scl));
        }
    }

    public void post(RunInGame runInGame) {
        runInGames.add(runInGame);
    }

	@Override
	public void render() {
        // Run any pending actions
        while (!runInGames.isEmpty()) {
            runInGames.poll().runInGame();
        }

        // Update the world
        update();

        // Center camera on player
        if (Game.player != null) {
            cam.position.x = Game.player.pos.x;
            cam.position.y = Game.player.pos.y;
            cam.update();
        }

        Game.viewportXY = new Vector2(cam.position.x, cam.position.y);

        // Clear device screen with black
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        // Clear viewport
        Gdx.gl.glScissor(
                (int) (Gdx.graphics.getWidth() / 2 - Game.viewportSize / zoom / 2),
                (int) (Gdx.graphics.getHeight() / 2 - Game.viewportSize / zoom / 2),
                (int) (Game.viewportSize / zoom),
                (int) (Game.viewportSize / zoom)
        );
        Gdx.gl.glClearColor(.2f, .8f, 0.5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the world
        Game.batch.setProjectionMatrix(cam.combined);
        Game.batch.begin();
        Game.world.render();
        Game.batch.end();

        if (Game.isEditing) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            Game.shapeRenderer.setProjectionMatrix(cam.combined);

            // Draw a grid
            int ts = Game.ts;
            int minX = (int) Math.floor((Game.viewportXY.x - Game.viewportSize / 2) / ts) * ts;
            int minY = (int) Math.floor((Game.viewportXY.y - Game.viewportSize / 2) / ts) * ts;
            int maxX = (int) Math.ceil((Game.viewportXY.x + Game.viewportSize / 2) / ts) * ts;
            int maxY = (int) Math.ceil((Game.viewportXY.y + Game.viewportSize / 2) / ts) * ts;

            Game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            Game.shapeRenderer.setColor(new Color(1f, 1f, 1f, .125f));

            for (int y = minY; y < maxY + 1; y += ts) {
                Game.shapeRenderer.rect(minX, y, Game.viewportSize + ts, 1);
            }

            for (int x = minX; x < maxX + 1; x += ts) {
                Game.shapeRenderer.rect(x, minY, 1, Game.viewportSize + ts);
            }

            Game.shapeRenderer.end();

            Game.batch.setProjectionMatrix(uiCam.combined);
            Game.shapeRenderer.setProjectionMatrix(uiCam.combined);

            if (isEditingTeleport != null) {

            }

            else if (!didChoosePaint) {
                Game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                Game.shapeRenderer.setColor(new Color(0f, 0f, 0f, .5f));
                Game.shapeRenderer.rect(0, 0, Game.viewportSize, Game.viewportSize);
                Game.shapeRenderer.setColor(new Color(1f, 1f, 1f, .25f));
                Game.shapeRenderer.rect(0, Game.viewportSize - Game.ts * 2, Game.viewportSize, Game.ts * 2);
                Game.shapeRenderer.setColor(new Color(0f, 0f, 0f, .25f));
                Game.shapeRenderer.rect(0, Game.viewportSize - Game.ts * 2 - 1, Game.viewportSize, 1);
                Game.shapeRenderer.end();

                Game.batch.begin();
                Texture mapTiles = GameResources.img("grassy_tiles.png");
                Game.batch.draw(mapTiles, 0, 0);

                Texture teleporter = GameResources.img("teleport_edit_mode.png");
                Game.batch.draw(teleporter, 0, Game.viewportSize - teleporter.getHeight());

                Texture delObj = GameResources.img("del_obj.png");
                Game.batch.draw(delObj, Game.viewportSize - Game.ts * 2, Game.viewportSize - teleporter.getHeight());
                Game.batch.end();
            } else {
                chooseTileDebouncer.update();

                Game.batch.begin();
                Texture closeButton = GameResources.img("close_button.png");
                Game.batch.draw(closeButton, Game.viewportSize - closeButton.getWidth(), Game.viewportSize - closeButton.getHeight());

                Vector3 lastTap = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                Game.font.setColor(1, 1, 1, 1);
                Game.font.draw(Game.batch, Game.world.activeMap.id + ":" +
                        Integer.toString((int) Math.floor(lastTap.x / Game.ts)) + ":" +
                        Integer.toString((int) Math.floor(lastTap.y / Game.ts)), 4, 12);
                Game.batch.end();
            }
        }

        // Display connection error
        if (Game.connectionError) {
            Game.batch.begin();
            Game.batch.setProjectionMatrix(uiCam.combined);
            Game.font.setColor(1, .333f, 0, 1);
            Game.font.draw(Game.batch, "Connection error", 4, 12);
            Game.batch.end();
        }
    }

	@Override
	public void dispose() {
		Game.batch.dispose();
        Game.font.dispose();

        Game.batch = null;
        Game.font = null;

		GameResources.dispose();
        Game.networking.close();
    }

    @Override
    public void resize(int width, int height) {
        float ss = Math.min(width, height);

        // Save world unit / viewport unit
        zoom = Game.viewportSize / ss;

        // Set camera metrics
        cam.viewportWidth = uiCam.viewportWidth = width;
        cam.viewportHeight = uiCam.viewportHeight = height;
        cam.zoom = uiCam.zoom = zoom;
        cam.update();

        uiCam.position.x = Game.viewportSize / 2;
        uiCam.position.y = Game.viewportSize / 2;
        uiCam.update();

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

    private void drawTile(Vector2 at) {
        if (Game.isEditing && paintObject == null && Game.world.activeMap != null) {
            Vector3 pos = cam.unproject(new Vector3(at.x, at.y, 0));
            int tX = (int) Math.floor(pos.x / Game.ts);
            int tY = (int) Math.floor(pos.y / Game.ts);

            Game.world.activeMap.editTile(tX, tY, paintTile);
        }
    }

    private void drawObject(Vector2 at) {
        if (Game.isEditing && paintObject != null && Game.world.activeMap != null) {

            Vector3 pos = cam.unproject(new Vector3(at.x, at.y, 0));

            // Delete objects
            if ("".equals(paintObject)) {
                MapObject mapObject = Game.world.activeMap.findObjectAt(new Vector2(pos.x, pos.y));

                if (mapObject != null) {
                    if (Teleport.class.isAssignableFrom(mapObject.getClass())) {
                        Game.world.remove(mapObject.id);
                        Game.networking.send(new GameNetworkRemoveObjectEvent(mapObject));
                    }
                }

                return;
            }

            if (Game.world.activeMap.checkCollision(new Vector2(pos.x, pos.y))) {
                // XXX Play fail sound how dare you are
                return;
            }

            int tX = (int) Math.floor(pos.x / Game.ts) * Game.ts;
            int tY = (int) Math.floor(pos.y / Game.ts) * Game.ts;

            final MapObject mapObject = (MapObject) Game.world.create(paintObject);
            mapObject.map = Game.world.activeMap;
            mapObject.setPos(new Vector2(tX, tY));

            Game.world.activeMap.add(mapObject);
            Game.networking.send(new GameNetworkCreateObjectEvent(mapObject));

            if (GameType.TELEPORT.equals(paintObject)) {
                isEditingTeleport = (Teleport) mapObject;

                Input.TextInputListener listener = new Input.TextInputListener() {
                    @Override
                    public void input(String text) {
                        Game.networking.send(new GameNetworkEditTeleportTargetEvent((Teleport) mapObject, text));
                        isEditingTeleport = null;
                    }

                    @Override
                    public void canceled() {
                        isEditingTeleport = null;
                    }
                };

                Gdx.input.getTextInput(listener, "Edit teleport", "", "map:x:y");
            }
        }
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0) {
            return false;
        }

        start.x = screenX;
        start.y = screenY;
        dragging = new Vector2(start);

        if (Game.isEditing) {
            Vector3 t = uiCam.unproject(new Vector3(dragging.x, dragging.y, 0));

            if (didChoosePaint) {

                Texture closeButton = GameResources.img("close_button.png");
                if (t.x > Game.viewportSize - closeButton.getWidth() &&
                        t.y > Game.viewportSize - closeButton.getHeight()) {
                    toggleEditing();
                } else if (paintObject != null) {
                    drawObject(dragging);
                } else {
                    drawTile(dragging);
                }
            }
        }

        return true;
    }

    private void chooseObject(String type) {
        paintObject = type;
        didChoosePaint = true;
    }

    private void chooseTile(Vector2 at) {
        paintObject = null;

        if (at.y < 0) {
            paintTile = -1;
        } else {
            paintTile = MapTile.getId(at);
        }

        didChoosePaint = true;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (dragging == null) {
            return false;
        }

        dragging.x = screenX;
        dragging.y = screenY;

        if (Game.isEditing && paintObject == null && didChoosePaint) {
            drawTile(dragging);
        }

        return true;
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || pointer > 0) {
            return false;
        }

        if (Game.isEditing && !didChoosePaint) {
            Vector3 t = uiCam.unproject(new Vector3(dragging.x, dragging.y, 0));

            // Top ts * 2 reserved for objects
            float ts = Game.ts;
            if (t.y > Game.viewportSize - Game.ts * 2) {
                if (new Rectangle(0, Game.viewportSize - ts, ts, ts).contains(t.x, t.y)) {
                    chooseObject(GameType.TELEPORT);
                } else
                if (new Rectangle(Game.viewportSize - ts * 2, Game.viewportSize - ts, ts, ts).contains(t.x, t.y)) {
                    chooseObject("");
                }
            } else {
                int h = GameResources.img("grassy_tiles.png").getHeight();
                chooseTile(new Vector2(t.x, h - t.y));
            }
        }

        dragging = null;

        Vector2 tap = new Vector2(screenX, screenY);
        Date now = new Date();

        if (now.getTime() < lastTapUp.getTime() + 500 && (lastTapPos == null || lastTapPos.dst(tap) < Game.ts / zoom)) {
            tapCount++;
        } else {
            tapCount = 0;
        }

        if (Game.isEditing && tapCount == 1) {
            chooseTileDebouncer.debounce();
        }

        if (tapCount == 2) {
            toggleEditing();
            tapCount = 0;
        }

        lastTapUp = now;

        if (lastTapPos == null) {
            lastTapPos = new Vector2();
        }

        lastTapPos.set(tap);

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
}
