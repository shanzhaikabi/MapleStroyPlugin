package com.sz.plugin.manager;

import com.sz.plugin.utils.MSUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MobManager {
    private static Map<Integer,Object> mobMap = new LinkedHashMap<>();

    public static void addMob(Object mob) throws Exception {
        int id = (int) MSUtils.doMethod(mob,"getEntityId");
        addMob(id,mob);
    }

    public static void addMob(int id,Object mob) throws Exception {
        if (mobMap.containsKey(id)) return;
        long hp = (long) MSUtils.doMethod(mob,"getHp");
        if (hp > 0) {
            mobMap.put(id, mob);
        }
    }

    public static boolean checkMob(int id) throws Exception {
        if (!mobMap.containsKey(id)) return false;
        Object mob = mobMap.get(id);
        long hp = (long) MSUtils.doMethod(mob,"getHp");
        if (hp <= 0){
            mobMap.remove(id);
            return false;
        }
        return true;
    }

    public static Object getMob(int id) throws Exception {
        if (!checkMob(id)) return null;
        return mobMap.get(id);
    }

}
