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

    public static int stringToInt(String id, int max) {
        return new Random(new BigInteger(id.getBytes()).intValue()).nextInt(max);
    }

    // http://www.java2s.com/Code/Java/2D-Graphics-GUI/HSVtoRGB.htm
    public static float[] HSVtoRGB(float h, float s, float v)
    {
        // H is given on [0->1] or -1. S and V are given on [0->1].
        // RGB are each returned on [0->1].
        float m, n, f;
        int i;

        float[] hsv = new float[3];
        float[] rgb = new float[3];

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = v;

        if (hsv[0] == -1)
        {
            rgb[0] = rgb[1] = rgb[2] = hsv[2];
            return rgb;
        }
        hsv[0] *= 6f;
        i = (int) (Math.floor(hsv[0]));
        f = hsv[0] - i;
        if (i % 2 == 0)
        {
            f = 1 - f; // if i is even
        }
        m = hsv[2] * (1 - hsv[1]);
        n = hsv[2] * (1 - hsv[1] * f);
        switch (i)
        {
            case 6:
            case 0:
                rgb[0] = hsv[2];
                rgb[1] = n;
                rgb[2] = m;
                break;
            case 1:
                rgb[0] = n;
                rgb[1] = hsv[2];
                rgb[2] = m;
                break;
            case 2:
                rgb[0] = m;
                rgb[1] = hsv[2];
                rgb[2] = n;
                break;
            case 3:
                rgb[0] = m;
                rgb[1] = n;
                rgb[2] = hsv[2];
                break;
            case 4:
                rgb[0] = n;
                rgb[1] = m;
                rgb[2] = hsv[2];
                break;
            case 5:
                rgb[0] = hsv[2];
                rgb[1] = m;
                rgb[2] = n;
                break;
        }

        return rgb;

    }
}
