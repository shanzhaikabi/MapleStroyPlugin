package com.sz.plugin.artifact;

import com.sz.plugin.artifact.buff.BaseBuff;

public class RawBuff {

    public BaseBuff buff = null;
    public boolean self = true;

    public RawBuff(BaseBuff buff,boolean self){
        this.buff = buff;
        this.self = self;
    }
}
