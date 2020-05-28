package com.sz.plugin.effect;

import java.util.HashMap;
import java.util.Map;

public class EffectList {
    private static final Map<String,Class> effectMap = new HashMap<>();

    static{
        effectMap.put("attack_up",AttackUp.class);
    }

    public static Class getEffectByKey(String key) {
        return effectMap.get(key);
    }
}
