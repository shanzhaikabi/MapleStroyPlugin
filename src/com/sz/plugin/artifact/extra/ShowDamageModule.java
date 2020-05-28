package com.sz.plugin.artifact.extra;

import com.sz.plugin.artifact.ExtraModule;

public class ShowDamageModule implements ExtraModule {

    private String str = "";
    private String rep = "";

    public ShowDamageModule(String str){
        this(str,"%damage%");
    }

    private ShowDamageModule(String str,String rep){
        this.str = str;
        this.rep = rep;
    }

    public String showDamage(String damage){
        return this.str.replace(rep,damage);
    }

    public String showDamage(long damage){
        return this.str.replace(rep,String.valueOf(damage));
    }

}
