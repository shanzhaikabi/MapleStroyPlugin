package com.sz.plugin.manager;

import com.sz.plugin.effect.Effect;
import com.sz.plugin.effect.EffectList;
import com.sz.plugin.effect.RawEffect;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.stream.Collectors;

public class EffectManager {
    public static RawEffect getRawEffect(String str) throws Exception {
        return new RawEffect(str);
    }

    /**
     * 从rawEffect中提取所有可用的key,从EffectList中找到匹配的Effect.class,并通过rawEffect进行实例化.
     * @param rawEffect
     * @return
     */
    public static Map<Class,Effect> getEffectList(RawEffect rawEffect){
        return rawEffect.showKeys().stream()
                .map(k -> {
                    try {
                        return getEffectByKey(k,rawEffect);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(o -> null != o)
                .collect(Collectors.toMap(o -> o.getClass(),o -> o));
    }

    private static Effect getEffectByKey(String key,RawEffect rawEffect) throws Exception{
        if (!rawEffect.isUseable(key)) return null;
        Class c = EffectList.getEffectByKey(key);
        Constructor constructor = c.getConstructor(RawEffect.class);
        return (Effect) constructor.newInstance(rawEffect);
    }
}
