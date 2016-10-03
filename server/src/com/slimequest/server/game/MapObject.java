package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/11/16.
 */

public class MapObject extends GameObject {
    public Map map;
    public int x;
    public int y;

    @Override
    public JsonObject fossilize() {
        JsonObject fossil = super.fossilize();

        fossil.add("map", new JsonPrimitive(map.id));
        fossil.add("x", new JsonPrimitive(x));
        fossil.add("y", new JsonPrimitive(y));

        return fossil;
    }

    @Override
    public void defossilize(JsonObject fossil) {
        super.defossilize(fossil);

        map = (Map) Game.world.get(fossil.get("map").getAsString());
        x = fossil.get("x").getAsInt();
        y = fossil.get("y").getAsInt();

        map.add(this);
    }

    @Override
    public String getType() {
        throw new RuntimeException("Don't use MapObject directly, subclass it.");
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        final String source;

        // Find event source
        if (event.getData().isJsonPrimitive()) {
            source = event.getData().getAsString();
        } else {
            source = EventAttr.getId(event);
        }

        // Is event somehow from myself?
        boolean isMe = source != null && source.equals(id);

        if (GameEvent.MOVE.equals(event.getType())) {
            // If it is me, update my location
            if (isMe) {
                x = EventAttr.getX(event);
                y = EventAttr.getY(event);

                // If it's not a teleport movement, don't send to own client
                if (event.getData().getAsJsonObject().has(GameAttr.TELEPORT)) {
                    sendToClient(event);
                }
            } else {
                sendToClient(event);
            }
        } else if (GameEvent.JOIN.equals(event.getType())) {
            sendToClient(event);
        } else if (GameEvent.LEAVE.equals(event.getType())) {
            sendToClient(event);
        } else if (GameEvent.OBJECT_STATE.equals(event.getType())) {
            sendToClient(event);
        }  else if (GameEvent.EDIT_TILE.equals(event.getType())) {
            // Don't send own tile edits to client
            if (!isMe) {
                sendToClient(event);
            }
        } else {
            super.getEvent(event);
        }
    }

    // Send event to associated client
    private void sendToClient(GameNetworkEvent event) {
        if (channel != null) {
            System.out.println("Sending event: " + event.json());

            try {
                channel.writeAndFlush(event.json());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
