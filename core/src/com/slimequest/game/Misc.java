package com.slimequest.game;

import java.security.SecureRandom;

/**
 * Created by jacob on 9/21/16.
 */

public class Misc {
    public static float stringToFloat(String id) {
        return new SecureRandom(id.getBytes()).nextFloat();
    }
}
