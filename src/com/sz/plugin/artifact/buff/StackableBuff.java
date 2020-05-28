package com.sz.plugin.artifact.buff;

import com.sz.plugin.utils.JsUtils;

public class StackableBuff extends BaseBuff {

    public int maxLv;

    public StackableBuff(){}

    public StackableBuff(BaseBuff buff,int maxLv){
        JsUtils.fatherToChild(buff,this);
        this.maxLv = maxLv;
    }

    public StackableBuff copy(){
        StackableBuff c = new StackableBuff();
        JsUtils.fatherToChild(this,c);
        return c;
    }

}
