package com.slimequest.game.game;

import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

import java.util.HashMap;

/**
 * Created by jacob on 9/11/16.
 */

public class World extends GameObject {
    private HashMap<String, Map> maps = new HashMap<String, Map>();
    private Map activeMap;

    @Override
    public void update() {
        if (activeMap != null) {
            activeMap.update();
        }
    }

    @Override
    public void render() {
        if (activeMap != null) {
            activeMap.render();
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.MOVE.equals(event.getType())) {
            if (activeMap != null) {
                activeMap.getEvent(event);
            }
        }

        else if (GameEvent.JOIN.equals(event.getType())) {
            String id = EventAttr.getId(event);
            int x = EventAttr.getX(event);
            int y = EventAttr.getY(event);

            // XXX TODO Not only players!!!!!!! :'D
            Player player = new Player();
            player.id = id;
            player.setPos(x, y);
            add(player);
        }

        else if (GameEvent.LEAVE.equals(event.getType())) {
            remove(event.getData().getAsString());
        }
    }

    public void add(GameObject player) {
        if (activeMap == null) {
            activeMap = new Map();
        }

        if (MapObject.class.isAssignableFrom(player.getClass())) {
            activeMap.add((MapObject) player);
        } else {
            // What to do with this object...
        }
    }

    public void remove(String id) {
        if (activeMap != null) {
            activeMap.remove(id);
        }
    }
}
