package com.sz.plugin.artifact;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sz.plugin.artifact.buff.*;
import com.sz.plugin.artifact.buff.effect.*;
import com.sz.plugin.artifact.buff.trigger.*;

import java.sql.Time;
import java.util.LinkedHashMap;
import java.util.Map;

public class BuffFactory {
    public static Map<String,Class> typeClassMap = new LinkedHashMap();

    public static RawBuff buildRawBuff(String str){
        String name = "default";
        String id = "default";
        int lv = 1;
        int mob = -1;
        boolean self = true;
        int stackable = 0;
        String trigger = "";
        String effect = "";
        String active = "";
        String extra = "";
        String[] s = str.split("\\|");
        for (String s2 : s) {
            String[] k = s2.split("!");
            String type = k[0];
            switch (type){
                case "trigger": trigger = k[1];break;
                case "effect" : effect = k[1];break;
                case "active" : active = k[1];break;
                case "extra" : extra = k[1];break;
                case "name" : name = k[1];break;
                case "id" : id = k[1];break;
                case "lv" : lv = Integer.valueOf(k[1]);break;
                case "mob" : mob = Integer.valueOf(k[1]);break;
                case "self" : self = Boolean.valueOf(k[1]);break;
                case "stackable" : stackable = Integer.valueOf(k[1]);break;
            }
        }
        BaseBuff buff = buildBuff(name,id,lv,mob,trigger,effect,active,extra);
        if (stackable > 0) buff = new StackableBuff(buff,stackable);
        return new RawBuff(buff,self);
    }

    public static BaseBuff buildBuff(String name,String id,int lv,int mob,String trigger,String effect,String active,String extra){
        BaseBuff buff = new BaseBuff(name,id,lv,mob);
        if (trigger.length() > 0){
            try{
                BaseTrigger trigger1 = (BaseTrigger) buildModule("Trigger",trigger);
                buff.trigger = trigger1;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        if (effect.length() > 0){
            try{
                BaseEffect effect1 = (BaseEffect) buildModule("Effect",effect);
                buff.effect = effect1;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        if (active.length() > 0){
            try{
                BaseActive active1 = (BaseActive) buildModule("Active",active);
                buff.active = active1;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        if (extra.length() > 0){
            buildExtra(buff,extra);
        }
        return buff;
    }

    public static void buildExtra(BaseBuff buff, String extra){
        String[] x = extra.split("@");
        String[] args = x[1].split("\\?");
        for(String s : args){
            String[] a = s.split("\\$");
            String type = a[0];
            switch(type){
                case "removeMsg" :
                    String msg = a[1];
                    buff.setRemovedMessage(msg);
                    break;
                case "gainMsg" :
                    String msg1 = a[1];
                    buff.setGainMessage(msg1);
                    break;
                case "showMsg" :
                    String msg2 = a[1];
                    buff.setShowMessage(msg2);
                    break;
            }
        }
    }

    public static Object buildModule(String base,String s){
        String[] x = s.split("@");
        String[] args = (x.length > 1 ? x[1] : "").split("\\?");
        String type = x[0];
        String typeClass;
        Class cls = null;
        if (typeClassMap.containsKey(type)){
            cls = typeClassMap.get(type);
        }
        else{
            typeClass = type;
            try {
                cls = Class.forName("com.sz.plugin.buff." + base + "." + typeClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            Object module = cls.getConstructors()[0].newInstance(args);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static{
        typeClassMap.put("attack", AttackUp.class);
        typeClassMap.put("artifact_attack", ArtifactAttackUp.class);
        typeClassMap.put("final_attack", FinalAttackUp.class);
        typeClassMap.put("hit", HitTrigger.class);
        typeClassMap.put("time", TimeTrigger.class);
        typeClassMap.put("artifact", ArtifactTrigger.class);
    }
}
