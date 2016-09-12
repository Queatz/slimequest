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

    @Override
    public void update() {
        this.pos.interpolate(targetPos, .33f, Interpolation.pow4);
    }

    public void setPos(int x, int y) {
        this.targetPos.x = this.pos.x = x;
        this.targetPos.y = this.pos.y = y;
    }

    public void addPos(float x, float y) {
        this.pos.x = this.targetPos.x += x;
        this.pos.y = this.targetPos.y += y;
    }

    public void moveTo(int x, int y) {
        this.targetPos.x = x;
        this.targetPos.y = y;
    }

    public void moveBy(float x, float y) {
        this.targetPos.x += x;
        this.targetPos.y += y;
    }
}
