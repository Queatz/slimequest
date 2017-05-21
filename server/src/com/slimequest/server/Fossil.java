package com.slimequest.server;

import com.arangodb.entity.BaseDocument;
import com.google.gson.JsonObject;
import com.slimequest.shared.Json;

/**
 * Created by jacob on 5/21/17.
 */

public class Fossil extends BaseDocument {

    private String fossil;

    public Fossil() {

    }

    public Fossil(JsonObject fossil) {
        setFossil(fossil);
    }

    public JsonObject getFossil() {
        return Json.from(fossil, JsonObject.class);
    }

    public Fossil setFossil(JsonObject fossil) {
        this.fossil = Json.to(fossil);
        return this;
    }
}
