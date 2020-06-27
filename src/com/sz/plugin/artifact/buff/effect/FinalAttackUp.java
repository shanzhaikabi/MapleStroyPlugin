package com.sz.plugin.artifact.buff.effect;

import static com.sz.plugin.artifact.buff.effect.AttackUp.dealPercentDamage;

public class FinalAttackUp implements FinalDamage {

    public int extra;

    public FinalAttackUp(String extra) {
        this(Integer.valueOf(extra));
    }

    private FinalAttackUp(int extra) {
        this.extra = extra;
    }

    @Override
    public Long doEffect(Object... args) {
        return dealPercentDamage(extra, args);
    }
}
