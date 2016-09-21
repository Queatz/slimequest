package com.slimequest.server;

import com.slimequest.server.game.World;

import java.util.Date;

/**
 * Created by jacob on 9/14/16.
 */

public abstract class RunInWorld {
    public Date scheduled;
    public abstract void runInWorld(World world);
}
