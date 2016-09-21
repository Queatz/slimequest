package com.slimequest.shared;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/12/16.
 */
public class EventAttr {
    public static String getId(GameNetworkEvent event) {
        try {
            return event.getData().getAsJsonObject().getAsJsonPrimitive("id").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static int getX(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive("x").getAsInt();
    }

    public static int getY(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive("y").getAsInt();
    }

    public static String getMapId(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive("map").getAsString();
    }

    public static String getType(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive("type").getAsString();
    }

    public static JsonArray getTiles(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonArray("tiles");
    }

    public static boolean getFrozen(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().get("frozen").getAsBoolean();
    }

    public static String getTag(GameNetworkEvent event) {
        return event.getData().getAsJsonObject().getAsJsonPrimitive("tag").getAsString();
    }

}
