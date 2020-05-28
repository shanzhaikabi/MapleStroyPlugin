package com.sz.plugin.artifact.buff.effect;

public class AttackUp implements BaseDamage {

    public int extra;

    public AttackUp(String extra) {
        this(Integer.valueOf(extra));
    }

    private AttackUp(int extra) {
        this.extra = extra;
    }

    @Override
    public Long doEffect(Object... args) {
        return dealPercentDamage(extra, args);
    }

    static Long dealPercentDamage(int extra, Object[] args) {
        if (args.length != 1 && args.length != 2) return null;
        long dmg = (long) args[0];
        int lv = args.length == 1 ? 1 : (int)args[1];
        return (long)(dmg * extra * lv / 100);
    }
}
