package com.sz.plugin.artifact;

import com.sz.plugin.artifact.effect.BuffArtifact;
import com.sz.plugin.artifact.extra.ActiveStackModule;
import com.sz.plugin.artifact.extra.ShowDamageModule;
import com.sz.plugin.artifact.extra.ShowEffectModule;
import com.sz.plugin.artifact.extra.ShowItemModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaseArtifact {
    public String name;
    public ActiveModule active = new ActiveModule() {
        @Override
        public void onTrigger(double radio) {

        }

        @Override
        public boolean isActive() {
            return false;
        }
    };

    public EffectModule effect = new EffectModule() {
        @Override
        public long extraDamage(long damage) {
            return 0;
        }

        @Override
        public void onAct(Object[] args) {

        }
    };

    public ActiveStackModule stack = new ActiveStackModule(new String[0]);

    public Map<Class,ExtraModule> extra = new LinkedHashMap<>();

    public Object eff = null;

    public void setEff(Object eff){
        this.eff = eff;
    }

    public Object getEff(){
        return this.eff;
    }

    public BaseArtifact(String name){
        this.name = name;
    }

    public void onTrigger(double radio) {
        this.active.onTrigger(radio);
    }

    public boolean isActive(){
        return this.active.isActive();
    }

    public long extraDamage(long baseDamage){
        return this.effect.extraDamage(baseDamage);
    }

    public void onAct(Object[] args){
        this.effect.onAct(args);
    }

    //是否展示伤害
    public boolean showDamage = true;

    //自定义伤害展示
    public ShowDamageModule damageModule = null;
    public ShowItemModule itemModule = null;

    //自定义特效
    public List<ShowEffectModule> effectModule = new ArrayList<>();

}
