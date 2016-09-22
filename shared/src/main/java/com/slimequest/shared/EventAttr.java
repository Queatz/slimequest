package com.slimequest.shared;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/12/16.
 */
public class EventAttr {
    private static boolean teleport;

    public static String getId(GameNetworkEvent event) {
        try {
            return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.ID).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static int getX(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.X).getAsInt();
    }

    public static int getY(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.Y).getAsInt();
    }

    public static String getMapId(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.MAP).getAsString();
    }

    public static String getType(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.TYPE).getAsString();
    }

    public static JsonArray getTiles(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonArray(GameAttr.TILES);
    }

    public static boolean getFrozen(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().get(GameAttr.FROZEN).getAsBoolean();
    }

    public static String getTag(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive(GameAttr.TAG).getAsString();
    }

    public static JsonArray getTile(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonArray(GameAttr.TILE);
    }

    public static String getData(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().get(GameAttr.DATA).getAsString();
    }

    public static boolean getSelfReceive(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().get(GameAttr.SELF_RECEIVE).getAsBoolean();
    }

    public static boolean getTeleport(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().get(GameAttr.TELEPORT).getAsBoolean();

    }
}
