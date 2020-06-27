package com.sz.plugin.artifact.extra;

import com.sz.plugin.artifact.ExtraModule;

public class DamageHits implements ExtraModule {
    private int hits = 5;
    private int delay = 0;
    private boolean block = false;

    public DamageHits(String h, String d,String b){
        this(Integer.valueOf(h),Integer.valueOf(d),Boolean.valueOf(b));
    }

    private DamageHits(int h, int d, boolean b){
        this.hits = h;
        this.delay = d;
        this.block = b;
    }

    public int getHits() {
        return hits;
    }

    public int getDelay() {
        return delay;
    }

    public boolean getBlock(){
        return block;
    }
}
