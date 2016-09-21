package com.slimequest.game;

import java.util.Date;

/**
 * Created by jacob on 9/12/16.
 */
public class Debouncer {
    private long freqMs;
    private Runnable runnable;
    private Date lastTriggeredAt;
    private boolean pending;

    public Debouncer(Runnable runnable, long freqMs) {
        this.runnable = runnable;
        this.freqMs = freqMs;
    }

    public void debounce() {
        if (!pending) {
            lastTriggeredAt = new Date();
            pending = true;
        }
    }

    public void update() {
        if (pending && new Date().getTime() > lastTriggeredAt.getTime() + freqMs) {
            pending = false;
            this.runnable.run();
        }
    }

    public void cancel() {
        pending = false;
    }
}
