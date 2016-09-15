package com.slimequest.server.game;

import com.slimequest.shared.GameType;

/**
 * Created by jacob on 9/11/16.
 */

public class Slime extends MapObject {
    @Override
    public String getType() {
        return GameType.SLIME;
    }
}
