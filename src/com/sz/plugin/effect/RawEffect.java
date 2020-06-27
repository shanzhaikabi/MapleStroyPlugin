package com.sz.plugin.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RawEffect {
    private Map<String,RawEffectInside> effectMap = new HashMap<>();

    public Set<String> showKeys(){
        return effectMap.keySet();
    }

    public void setUnuseable(String key){
        if (effectMap.containsKey(key)) effectMap.get(key).setUseable(false);
    }

    public String getValue(String key){
        return isUseable(key) ? effectMap.get(key).getValue() : null;
    }

    public boolean isUseable(String key){
        return effectMap.containsKey(key) ? effectMap.get(key).isUseable() : false;
    }

    public RawEffect(String str) throws Exception {
        if (str == null) throw new Exception();
        String[] tmp = str.split("|");
        for (String s : tmp) {
            String[] t = str.split(":");
            if (t.length != 2) throw new Exception();
            RawEffectInside r = new RawEffectInside(t[0],t[1]);
            effectMap.put(t[0],r);
        }
    }
}

class RawEffectInside{
    private String key;
    private String value;
    private boolean useable;
    
    public RawEffectInside(String key,String value){
        this.key = key;
        this.value = value;
        this.useable = true;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isUseable() {
        return useable;
    }

    public void setUseable(boolean useable) {
        this.useable = useable;
    }
}
