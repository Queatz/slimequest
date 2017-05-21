package com.slimequest.server;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.slimequest.server.game.Fossilize;
import com.slimequest.server.game.GameObject;
import com.slimequest.shared.Json;

import java.io.IOException;

/**
 * Created by jacob on 5/21/17.
 */

public class GameArangoDb {

    private static final String DB_USER = "slime";
    private static final String DB_PASS = "slime";
    private static final String DB_COLLECTION = "slime";

    private static ArangoDatabase __arangoDatabase;

    public static boolean isEmpty() {
        return getCollection().count().getCount() == 0;
    }

    public static void loadAll() {
        ArangoCursor<Fossil> cursor = getDb().query("for x in " + DB_COLLECTION + " return x", null, null, Fossil.class);

        while (cursor.hasNext()) {
            GameObject gameObject =
                    (GameObject) Fossilize.defossilize(Json.from(cursor.next().getFossil(), JsonObject.class));

            Game.world.add(gameObject);
        }
    }

    public static void saveAll() {
        Game.world.save();
    }

    private static ArangoCollection getCollection() {
        return getDb().collection(DB_COLLECTION);
    }

    private static ArangoDatabase getDb() {
        if (__arangoDatabase != null) {
            return __arangoDatabase;
        }

        __arangoDatabase = new ArangoDB.Builder()
                .user(DB_USER)
                .password(DB_PASS)
                .build()
                .db();

        try {
            __arangoDatabase.createCollection(DB_COLLECTION);
        } catch (ArangoDBException ignored) {
            // Whatever
        }

        return __arangoDatabase;
    }

    public static void delete(String id) {
        try {
            getCollection().deleteDocument(id);
        } catch (ArangoDBException e) {
            e.printStackTrace();
        }
    }

    public static void save(GameObject gameObject) {
        JsonObject fossil = Fossilize.fossilize(gameObject);

        if (fossil == null) {
            System.out.println("Skipping save of " + gameObject.getType() + "...");
            return;
        }

        Fossil fossilDocument = new Fossil(fossil);
        fossilDocument.setKey(gameObject.id);

        try {
            getCollection().updateDocument(fossilDocument.getKey(), fossilDocument);
        } catch (ArangoDBException e) {
            try {
                getCollection().insertDocument(fossilDocument);
            } catch (ArangoDBException e2) {
                e2.printStackTrace();
            }
        }

        System.out.println("Saved " + gameObject.getType() + "...");
    }

    public static GameObject get(String id) {
        ArangoCursor<Fossil> cursor = getDb().query("for x in " + DB_COLLECTION + " filter x._key == @id return x", ImmutableMap.<String, Object>of(
                "id", id
        ), null, Fossil.class);

        GameObject gameObject = null;

        if (cursor.hasNext()) {
            gameObject = (GameObject) Fossilize.defossilize(Json.from(cursor.next().getFossil(), JsonObject.class));
        }

        return gameObject;
    }
}
