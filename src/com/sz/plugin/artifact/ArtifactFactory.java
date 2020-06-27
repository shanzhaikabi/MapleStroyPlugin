package com.sz.plugin.artifact;

import com.sz.plugin.artifact.active.*;
import com.sz.plugin.artifact.effect.*;
import com.sz.plugin.artifact.extra.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArtifactFactory {

    public static Map<String,Class> typeClassMap = new LinkedHashMap();

    public static Object buildModule(String type,String[] args){
        String typeClass;
        Class cls = null;
        if (typeClassMap.containsKey(type)){
            cls = typeClassMap.get(type);
        }
        else{
            typeClass = type + "Artifact";
            try {
                cls = Class.forName("com.sz.plugin.artifact" + typeClass);
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

    public static void buildExtraModule(BaseArtifact artifact,String str){
        if (str.length() == 0) return;
        String[] extras = str.split("\\|");
        for (String extra : extras) {
            String[] e = extra.split(":");
            String type = e[0];
            String[] args = e.length > 1 ? e[1].split(",") : new String[0];
            switch (type){
                case "show_item":
                    artifact.itemModule = (ShowItemModule) buildModule(type,args);
                    break;
                case "show_damage":
                    artifact.damageModule = (ShowDamageModule) buildModule(type,args);
                    break;
                case "not_show_damage":
                    artifact.showDamage = false;
                    break;
                case "show_effect":
                    artifact.effectModule.add(setEffectModule(args));
                    break;
                case "active_stack":
                    artifact.stack = new ActiveStackModule(args);
                    break;
                default:
                    ExtraModule module = (ExtraModule) buildModule(type,args);
                    artifact.extra.put(module.getClass(),module);
            }
        }
    }

    public static ShowEffectModule setEffectModule(String[] args){
        int x = 0;
        int y = 0;
        boolean self = true;
        boolean mob = false;
        boolean block = false;
        String path = "";
        int duration = 0;
        for (String s2 : args) {
            String[] k = s2.split("!");
            String type = k[0];
            switch (type) {
                case "x":
                    x = Integer.valueOf(k[1]);
                    break;
                case "y":
                    y = Integer.valueOf(k[1]);
                    break;
                case "duration":
                    duration = Integer.valueOf(k[1]);
                    break;
                case "self":
                    self = Boolean.valueOf(k[1]);
                    break;
                case "mob":
                    mob = Boolean.valueOf(k[1]);
                    break;
                case "block":
                    block = Boolean.valueOf(k[1]);
                    break;
                case "path":
                    path = k[1];
                    break;
            }
        }
        ShowEffectModule module = new ShowEffectModule(x,y,duration,self,mob,block,path);
        return module;
    }

    public static BaseArtifact buildArtifact(Object name,Object active,Object effect,Object extra) {
        return buildArtifact((String)name,(String)active,(String)effect,(String)extra);
    }

    public static BaseArtifact buildArtifact(String name,String active,String effect,String extra){
        String[] actives = active.split(":");
        String activeArgs = actives.length > 1 ? actives[1] : "";
        String[] effects = effect.split(":");
        String effectArgs = effects.length > 1 ? effects[1] : "";

        ActiveModule activeModule = (ActiveModule) buildModule(actives[0],activeArgs.split(","));
        EffectModule effectModule = (EffectModule) buildModule(effects[0],effectArgs.split(","));

        BaseArtifact artifact = new BaseArtifact(name);
        artifact.active = activeModule;
        artifact.effect = effectModule;

        buildExtraModule(artifact,extra);

        return artifact;
    }

    static{
        typeClassMap.put("chance", ChanceArtifact.class);
        typeClassMap.put("attack_time", AttackTimeArtifact.class);
        typeClassMap.put("time", TimeArtifact.class);
        typeClassMap.put("damage_percent", DamagePercentArtifact.class);
        typeClassMap.put("damage", DamageArtifact.class);
        typeClassMap.put("buff", BuffArtifact.class);
        typeClassMap.put("show_damage",ShowDamageModule.class);
        typeClassMap.put("show_item",ShowItemModule.class);
        typeClassMap.put("damage_hits", DamageHits.class);
        typeClassMap.put("active_stack", ActiveStackModule.class);
    }
}
