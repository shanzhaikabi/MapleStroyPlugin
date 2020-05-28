package com.sz.plugin.artifact.buff.effect;

import static com.sz.plugin.artifact.buff.effect.AttackUp.dealPercentDamage;

public class ArtifactAttackUp implements ArtifactDamage {

    public int extra;

    public ArtifactAttackUp(String extra) {
        this(Integer.valueOf(extra));
    }

    private ArtifactAttackUp(int extra) {
        this.extra = extra;
    }

    @Override
    public Long doEffect(Object... args) {
        return dealPercentDamage(extra, args);
    }
}
