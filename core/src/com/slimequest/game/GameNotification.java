package com.slimequest.game;

/**
 * Created by jacob on 9/20/16.
 */
public class GameNotification {
    public String objectId;
    public String message;

    public GameNotification(String objectId, String message) {
        this.objectId = objectId;
        this.message = message;
    }
}
