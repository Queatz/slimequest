package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 9/14/16.
 */

public class Teleport extends MapObject {

    public String target;

    private Set<MapObject> dontTrigger = new HashSet<>();

    @Override
    public JsonObject fossilize() {
        JsonObject fossil = super.fossilize();

        fossil.add("target", new JsonPrimitive(target));

        return fossil;
    }

    @Override
    public void defossilize(JsonObject fossil) {
        super.defossilize(fossil);
        target = fossil.get("target").getAsString();
    }

    @Override
    public String getType() {
        return GameType.TELEPORT;
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        switch (event.getType()) {
            case GameEvent.JOIN:
                MapObject obj = (MapObject) Game.world.get(EventAttr.getId(event));
                if (doesTrigger(obj)) {
                    dontTrigger.add(obj);
                }
                break;
        }
    }

    @Override
    public void update(float delta) {
        if (target == null) {
            return;
        }

        String[] vals = target.split(":");

        if (vals.length < 3) {
            return;
        }

        String targetMap = vals[0];
        int targetX = Integer.valueOf(vals[1]) * Game.ts + Game.ts / 2;
        int targetY = Integer.valueOf(vals[2]) * Game.ts + Game.ts / 2;

        if (map != null) {
            // TODO use realm or something...
            for (MapObject object : map.find()) {
                if (object == this) {
                    continue;
                }

                if (doesTrigger(object) && !dontTrigger.contains(object)) {
                    // Teleport dat object!
                    Game.world.move(
                            object,
                            (Map) Game.world.get(targetMap),
                            targetX,
                            targetY
                    );

                    // XXX Only one object per frame lol otherwise CME!!!
                    break;
                }
            }
        }

        // Once objects walk outside the trigger range, let them be triggered again
        for (MapObject object : dontTrigger) {
            if (!doesTrigger(object)) {
                dontTrigger.remove(object);

                // XXX Only one object per frame lol otherwise CME!!!
                break;
            }
        }
    }

    private boolean doesTrigger(MapObject object) {
        int ts2 = Game.ts / 2;
        return Math.abs(object.x - (x + ts2)) < ts2 &&
                Math.abs(object.y - (y + ts2)) < ts2;
    }
}
