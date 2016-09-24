package com.slimequest.server.game;

import com.slimequest.shared.GameType;

/**
 * Created by jacob on 9/23/16.
 */

public class Sign extends MapObject {
    @Override
    public String getType() {
        return GameType.SIGN;
    }
}
