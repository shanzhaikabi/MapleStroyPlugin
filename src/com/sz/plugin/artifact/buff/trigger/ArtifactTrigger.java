package com.sz.plugin.artifact.buff.trigger;

import com.sz.plugin.artifact.buff.BaseTrigger;

public class ArtifactTrigger implements BaseTrigger {

    public int hits;
    public int cur_hits = 0;

    public ArtifactTrigger(String hits){
        this(Integer.valueOf(hits));
    }

    public ArtifactTrigger(int hits){
        this.hits = hits;
    }

    @Override
    public boolean onTrigger() {
        return cur_hits >= hits;
    }

    @Override
    public void reset() {
        cur_hits = 0;
    }
}
