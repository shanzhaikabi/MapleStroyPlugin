package com.sz.plugin.artifact.buff.trigger;

import com.sz.plugin.artifact.buff.BaseTrigger;

public class HitTrigger implements BaseTrigger {

    private int hits;
    public int cur_hits;

    public HitTrigger(String hits){
        this(Integer.valueOf(hits));
    }

    private HitTrigger(int hits){
        this.hits = hits;
        this.cur_hits = 0;
    }

    @Override
    public void reset(){
        this.cur_hits = 0;
    }

    @Override
    public boolean onTrigger() {
        cur_hits++;
        return cur_hits > hits;
    }
}
