package com.slimequest.game.game;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by jacob on 9/11/16.
 */

public class MapObject extends GameObject {
    public Map map;
    public Vector2 pos = new Vector2();
    public Vector2 targetPos = new Vector2();
    private Vector2 lastPos = new Vector2();

    @Override
    public void update() {
        // Alternative resolution
        // May want to use this if distance is > say the screen width
         this.pos.interpolate(targetPos, .667f, Interpolation.circleIn);

        // Find the desired movement speed
//        float scl = Math.min(2 /* XXX REPLACE WITH OBJECT SPEED */, pos.dst(targetPos));

        // Move in the desired direction at that speed
//        pos.add(targetPos.cpy().sub(pos).nor().scl(scl));

        // Save the last position. Used for collision detection
        lastPos.set(pos);
    }

    private void checkCollide() {
        // Do collision with map tiles
        if (map != null && map.checkCollision(pos)) {
            // TODO check velocity, if > ts then check each tile

            boolean collisionLastX = map.checkCollision(new Vector2(lastPos.x, pos.y));
            boolean collisionLastY = map.checkCollision(new Vector2(pos.x, lastPos.y));

            if (collisionLastX == collisionLastY) {
                pos.set(Map.snap(lastPos, pos));
            } else if (!collisionLastX) {
                pos.x = Map.snap(lastPos.x, pos.x);
            } else {
                pos.y = Map.snap(lastPos.y, pos.y);
            }

            targetPos.set(pos);
        }
    }


    public void moveTo(Vector2 vec) {
        this.targetPos.set(vec);
    }

    public void moveBy(Vector2 vec) {
        this.targetPos.add(vec);
    }

    public void setPos(Vector2 pos) {
        this.pos.set(this.targetPos.set(pos));
        checkCollide();
    }

    public void addPos(Vector2 pos) {
        this.pos.set(this.targetPos.add(pos));
        checkCollide();
    }
}
