package com.sz.plugin.status;

import com.sz.plugin.effect.Effect;
import com.sz.plugin.status.damage.DamageArea;
import com.sz.plugin.utils.SpList;
import javafx.util.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerStatus {
    private long totalDamage = 0;//总伤害
    private SpList<DamageArea> area = new SpList<>();//伤害乘区
    private Object SkillPool;//记录技能情况
    private Object BuffPool;//记录buff
    private Map<Class, Map<String,Effect>> EffectPool;//记录效果

    public Map<Class, Map<String, Effect>> getEffectPool() {
        return EffectPool;
    }

    public Pair<Double,Map<String,Double>> calculateArea(){
        double ret = 1;
        Map<String,Double> map = new LinkedHashMap<>();
        area.forEach(u -> {
            double tmp = u.calculateArea();
            String name = u.getAreaName();
            map.put(name,tmp);
        });
        return new Pair<>(ret,map);
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public void addTotalDamage(long damage){
        totalDamage += damage;
    }

    public PlayerStatus(){

    }
}
