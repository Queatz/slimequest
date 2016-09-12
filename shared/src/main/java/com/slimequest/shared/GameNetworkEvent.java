package com.slimequest.shared;

import com.google.gson.JsonElement;

/**
 * Created by jacob on 9/10/16.
 */

public class GameNetworkEvent {
    final String type;
    final JsonElement data;

    private transient String rawJson;

    public GameNetworkEvent(String type) {
        this(type, null);
    }

    public GameNetworkEvent(String type, JsonElement data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public JsonElement getData() {
        return data;
    }

    public String json() {
        if (rawJson != null) {
            return rawJson;
        }

        rawJson = Json.to(this);

        return rawJson;
    }
}
