package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.google.gson.JsonElement;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;
import com.slimequest.game.events.GameNetworkTagEvent;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 10/7/16.
 */

public class Carrot extends MapObject {
    public boolean isEaten;

    @Override
    public void update() {
        if (Game.player == null) {
            return;
        }

        if (doesTrigger(Game.player) && !isEaten) {
            isEaten = true;
            Game.networking.send(new GameNetworkTagEvent(id));
            ((Player) Game.player).eatCarrot();
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.OBJECT_STATE.equals(event.getType())) {
            isEaten = event.getData().getAsJsonObject().get(GameAttr.EATEN).getAsBoolean();
        }
    }

    @Override
    public void render() {
        Texture texture = GameResources.img(isEaten ? "eatencarrot.png" : "carrot.png");
        Game.batch.draw(texture, pos.x, pos.y);
    }

    @Override
    public void init(JsonElement data) {
        isEaten = data.getAsJsonObject().get(GameAttr.EATEN).getAsBoolean();
    }
}
