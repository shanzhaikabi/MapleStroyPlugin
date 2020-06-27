package com.sz.plugin.artifact;

public interface EffectModule {
    long extraDamage(long damage);
    void onAct(Object[] args);
}
