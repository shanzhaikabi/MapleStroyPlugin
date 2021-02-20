package com.sz.plugin.artifact.active;

import com.sz.plugin.artifact.ActiveModule;

public class TimeArtifact implements ActiveModule {

    public int t;
    public long time;

    public TimeArtifact(String time) {
        this(Integer.valueOf(time));
    }

    private TimeArtifact(int t) {
        this.t = t * 1000;
        time = System.currentTimeMillis() + this.t;
    }

    @Override
    public boolean isActive() {
        if (System.currentTimeMillis() > time){
            time = System.currentTimeMillis() + t;
            return true;
        }
        return false;
    }

    @Override
    public void onTrigger(double radio) {
    }
}
