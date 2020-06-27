package com.sz.plugin.artifact.effect;

import com.sz.plugin.artifact.EffectModule;

public class DamageArtifact implements EffectModule {

    public long dmg;

    public DamageArtifact(String dmg){
        this(Long.valueOf(dmg));
    }

    private DamageArtifact(long dmg){
        this.dmg = dmg;
    }

    @Override
    public long extraDamage(long damage) {
        return dmg;
    }

    @Override
    public void onAct(Object[] args) {

    }
}
