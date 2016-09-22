package com.slimequest.game;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by jacob on 9/21/16.
 */

public class Misc {
    public static float stringToFloat(String id) {
        return new Random(new BigInteger(id.getBytes()).intValue()).nextFloat();
    }
}
