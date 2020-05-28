package com.sz.plugin.artifact.effect;

import com.sz.plugin.artifact.EffectModule;

public class DamagePercentArtifact implements EffectModule {

    public int percent;

    public DamagePercentArtifact(String percent){
        this(Integer.valueOf(percent));
    }

    private DamagePercentArtifact(int percent){
        this.percent = percent;
    }

    @Override
    public long extraDamage(long damage) {
        return damage * percent / 100 ;
    }

    @Override
    public void onAct(Object[] args) {

    }
}
