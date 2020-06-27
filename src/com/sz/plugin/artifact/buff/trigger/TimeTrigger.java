package com.sz.plugin.artifact.buff.trigger;

import com.sz.plugin.artifact.buff.BaseTrigger;

public class TimeTrigger implements BaseTrigger {

    private long time;
    private int duration;

    public TimeTrigger(String time){
        this(Integer.valueOf(time));
    }

    private TimeTrigger(int time){
        this.duration = time;
        this.time = System.currentTimeMillis() + duration * 1000;
    }

    @Override
    public void reset(){
        this.time = System.currentTimeMillis() + duration * 1000;
    }

    @Override
    public boolean onTrigger() {
        return this.time < System.currentTimeMillis();
    }
}
