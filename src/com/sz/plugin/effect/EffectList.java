package com.sz.plugin.effect;

import java.util.HashMap;
import java.util.Map;

public class EffectList {
    private static final Map<String,Class> effectMap = new HashMap<>();

    static{
        effectMap.put("attackup",AttackUp.class);
        effectMap.put("type",EffectType.class);
    }

    public static Class getEffectByKey(String key) {
        return effectMap.get(key);
    }
}
