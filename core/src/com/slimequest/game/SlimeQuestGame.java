package com.slimequest.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.slimequest.game.events.GameNetworkCreateObjectEvent;
import com.slimequest.game.events.GameNetworkEditTeleportTargetEvent;
import com.slimequest.game.events.GameNetworkRemoveObjectEvent;
import com.slimequest.game.game.MapObject;
import com.slimequest.game.game.MapTile;
import com.slimequest.game.game.Player;
import com.slimequest.game.game.Sign;
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

    // Last game notification that was shown
    private GameNotification displayGameNotification;
    private Date lastGameNotification;

    // Movement
    private Vector2 dragging;
    private Vector2 start = new Vector2();

    private Date lastConnectionError;

    // Credits roll
    private boolean showingCredits;
    private float creditsRollOffsetY;

    @Override
	public void create() {
        Game.world = new World();
        Game.batch = new SpriteBatch();
        Game.shapeRenderer = new ShapeRenderer();

        // Set up game
        Game.game = this;
        Gdx.input.setInputProcessor(this);

        new GameNetworking().start();

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
    }

    private void update() {
        // Run any pending actions
        while (!runInGames.isEmpty()) {
            runInGames.poll().runInGame();
        }

        // Update world
        Game.world.update();

        // Update notifications

        // Last notification sticks around longer
        int delay = Game.gameNotifications.isEmpty() ? 3000 : 1000;

        if (lastGameNotification == null || lastGameNotification.before(new Date(new Date().getTime() - delay))) {
            if (!Game.gameNotifications.isEmpty()) {
                lastGameNotification = new Date();
                displayGameNotification = Game.gameNotifications.poll();

                notificationShown(displayGameNotification);
            } else {
                lastGameNotification = null;
                displayGameNotification = null;
            }
        }

        // Player movement
        if (dragging != null && Game.player != null && !((Player) Game.player).frozen && !Game.isEditing) {
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

    private void notificationShown(GameNotification notification) {
        if (notification.message.contains("win")) {
            GameResources.snd("hehehe.ogg").play();
        }
    }

    public void post(RunInGame runInGame) {
        runInGames.add(runInGame);
    }

	@Override
	public void render() {
        // Update the world
        update();

        // Center camera on player
        if (Game.player != null) {
            cam.position.x = Game.player.pos.x;
            cam.position.y = Game.player.pos.y;
            cam.update();
        }

        // Update viewport position reference
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

        if (showingCredits) {
            Gdx.gl.glClearColor(.12f, .08f, 0.03f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            creditsRollOffsetY += 1;

            GlyphLayout glyphLayout = new GlyphLayout();
            glyphLayout.setText(Game.font, Game.credits, Color.WHITE, Game.viewportSize, Align.center, true);

            Game.batch.setProjectionMatrix(uiCam.combined);

            Game.font.getData().markupEnabled = true;
            Game.batch.begin();
            Game.font.draw(Game.batch,
                    Game.credits,
                    0,
                    creditsRollOffsetY,
                    Game.viewportSize,
                    Align.center,
                    true);

            int frame = (int) ((new Date().getTime() / 100) % 2);
            String dir = new String[] {
                "front",
                "right",
                "back",
                "left",
            }[(int) ((new Date().getTime() / 800) % 4)];
            Texture bunnyImg = GameResources.img(frame == 0 ? "Bunny-" + dir + "-final.png" : "Bunny-" + dir + "-jump-final.png");
            Game.batch.draw(bunnyImg, Game.viewportSize / 2 - bunnyImg.getWidth() / 2, creditsRollOffsetY - glyphLayout.height - Game.viewportSize / 4);


            Game.batch.end();
            Game.font.getData().markupEnabled = false;

            if (creditsRollOffsetY - glyphLayout.height > Game.viewportSize * 1.5) {
                showingCredits = false;
            }

            return;
        }

        // Clear
        Gdx.gl.glClearColor(.25f, .25f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw the world
        Game.batch.setProjectionMatrix(cam.combined);
        Game.batch.begin();
        Game.world.render();
        Game.batch.end();

        // UI

        Texture bunnyImg = GameResources.img("Bunny-right-final.png");
        Game.batch.setProjectionMatrix(uiCam.combined);

        // Draw the it player
        if (Game.world.itPlayerId != null) {
            Game.batch.setColor(Player.getBunnyColor(Game.world.itPlayerId));

            GlyphLayout layout = new GlyphLayout();
            layout.setText(Game.font, "is it!");
            int fh = (int) layout.height;

            int x = 4;
            int y = Game.viewportSize - bunnyImg.getHeight() - 2;

            Game.batch.begin();
            Game.batch.draw(bunnyImg, x, y);

            Game.font.setColor(1, 1, 1, 1);
            Game.font.draw(Game.batch, "is it!", x + bunnyImg.getWidth() + 4, y + fh + 2);
            Game.batch.end();
        }

        // Display game notifications
        if (displayGameNotification != null) {
            GlyphLayout layout = new GlyphLayout();
            layout.setText(Game.font, displayGameNotification.message);
            int w = bunnyImg.getWidth() + 4 + (int) layout.width;

            int x = Game.viewportSize / 2 - w / 2;
            int y = (int) (Game.viewportSize * .75f);

            Game.batch.begin();

            if (":butterfly".equals(displayGameNotification.objectId)) {
                Game.batch.setColor(1f, 1f, 1f, 1f);
                Game.batch.draw(GameResources.img("butterfly.png"), x, y);
            } else {
                Game.batch.setColor(Player.getBunnyColor(displayGameNotification.objectId));
                Game.batch.draw(bunnyImg, x, y);
            }

            Game.font.setColor(1f, 1f, 1f, 1f);
            Game.font.draw(Game.batch, displayGameNotification.message, x + bunnyImg.getWidth() + 4, y + (int) layout.height + 4);
            Game.batch.end();

        }

        // Reset color
        Game.batch.setColor(1f, 1f , 1f, 1f);

        if (Game.isEditing) {
            renderEditing();
        }

        // Display connection error
        if (Game.connectionError || Game.connecting) {
            if (!Game.connecting) {
                lastConnectionError = new Date();
            }

            Game.batch.begin();
            Game.batch.setProjectionMatrix(uiCam.combined);

            if (Game.connecting) {
                Game.font.setColor(1, 1f, 0, 1);
            } else {
                Game.font.setColor(1, .333f, 0, 1);
            }

            Game.font.draw(Game.batch, Game.connecting ? "Connecting..." : "Connection error", 4, 12);
            Game.batch.end();
        }

        // Display connection error
        else if (lastConnectionError != null && lastConnectionError.after(new Date(new Date().getTime() - 1500))) {
            Game.batch.begin();
            Game.batch.setProjectionMatrix(uiCam.combined);
            Game.font.setColor(.333f, 1, 0, 1);
            Game.font.draw(Game.batch, "Connection restored", 4, 12);
            Game.batch.end();
        }
    }

    private void renderEditing() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Draw a grid
        Game.shapeRenderer.setProjectionMatrix(cam.combined);
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
            // Do nothing
        }

        // Choose editing tool
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

            Texture sign = GameResources.img("sign.png");
            Game.batch.draw(sign, teleporter.getWidth(), Game.viewportSize - sign.getHeight());

            Texture delObj = GameResources.img("del_obj.png");
            Game.batch.draw(delObj, Game.viewportSize - Game.ts * 2, Game.viewportSize - teleporter.getHeight());
            Game.batch.end();

            // Editing map
        } else {
            chooseTileDebouncer.update();

            // Draw a close button
            Game.batch.begin();
            Texture closeButton = GameResources.img("close_button.png");
            Game.batch.draw(closeButton, Game.viewportSize - closeButton.getWidth(), Game.viewportSize - closeButton.getHeight());

            // Draw last edit location
            Vector3 lastTap = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            Game.font.setColor(1, 1, 1, 1);
            Game.font.draw(Game.batch, Game.world.activeMap.id + ":" +
                    Integer.toString((int) Math.floor(lastTap.x / Game.ts)) + ":" +
                    Integer.toString((int) Math.floor(lastTap.y / Game.ts)), 4, 12);
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
            if (didChoosePaint) {
                Vector3 t = uiCam.unproject(new Vector3(dragging.x, dragging.y, 0));
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

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (dragging == null) {
            return false;
        }

        dragging.x = screenX;
        dragging.y = screenY;

        // Tiles are drawn on drag
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

        // Select edit tool
        if (Game.isEditing && !didChoosePaint) {
            Vector3 t = uiCam.unproject(new Vector3(dragging.x, dragging.y, 0));

            // Top ts * 2 reserved for objects
            float ts = Game.ts;
            if (t.y > Game.viewportSize - Game.ts * 2) {
                if (new Rectangle(0, Game.viewportSize - ts, ts, ts).contains(t.x, t.y)) {
                    chooseObject(GameType.TELEPORT);
                } else if (new Rectangle(ts, Game.viewportSize - ts, ts, ts).contains(t.x, t.y)) {
                    chooseObject(GameType.SIGN);
                } else if (new Rectangle(Game.viewportSize - ts * 2, Game.viewportSize - ts, ts, ts).contains(t.x, t.y)) {
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

        // Count taps in the same location
        if (now.getTime() < lastTapUp.getTime() + 333 && (lastTapPos == null || lastTapPos.dst(tap) < Game.ts / 2 / zoom)) {
            tapCount++;
        } else {
            tapCount = 0;
        }

        // In edit mode, double tap toggles edit tool chooser
        if (Game.isEditing && tapCount == 1) {
            chooseTileDebouncer.debounce();
        }

        // Triple tap always toggles edit mode
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

    // Editing stuff

    private void toggleEditing() {
        Game.isEditing = !Game.isEditing;

        // Also make sure they start off choosing their edit tool
        didChoosePaint = false;
    }

    // Edit: draw tile
    private void drawTile(Vector2 at) {
        Vector3 pos = cam.unproject(new Vector3(at.x, at.y, 0));
        int tX = (int) Math.floor(pos.x / Game.ts);
        int tY = (int) Math.floor(pos.y / Game.ts);

        Game.world.activeMap.editTile(tX, tY, paintTile);
    }

    // Edit: draw object
    private void drawObject(Vector2 at) {
        Vector3 pos = cam.unproject(new Vector3(at.x, at.y, 0));

        // Delete objects
        if ("".equals(paintObject)) {
            MapObject mapObject = Game.world.activeMap.findObjectAt(new Vector2(pos.x, pos.y));

            if (mapObject != null) {
                if (Teleport.class.isAssignableFrom(mapObject.getClass()) ||
                        Sign.class.isAssignableFrom(mapObject.getClass())) {
                    Game.world.remove(mapObject.id);
                    Game.networking.send(new GameNetworkRemoveObjectEvent(mapObject));
                }
            }

            return;
        }

        if (Game.world.activeMap.checkCollision(new Vector2(pos.x, pos.y))) {
            GameResources.snd("howdareyouare.ogg").play();
            return;
        }

        int tX = (int) Math.floor(pos.x / Game.ts) * Game.ts;
        int tY = (int) Math.floor(pos.y / Game.ts) * Game.ts;

        // Create the new object
        final MapObject mapObject = (MapObject) Game.world.create(paintObject);
        mapObject.map = Game.world.activeMap;
        mapObject.setPos(new Vector2(tX, tY));

        // Add the object and notify the server
        Game.world.activeMap.add(mapObject);
        Game.networking.send(new GameNetworkCreateObjectEvent(mapObject));

        // If it's a teleport, show the destination chooser
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

    public void showCredits() {
        showingCredits = true;
        creditsRollOffsetY = 0;
    }
}