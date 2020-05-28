package com.sz.plugin.artifact.buff;

import com.sz.plugin.utils.JsUtils;
import com.sz.plugin.utils.MSUtils;
import netscape.javascript.JSUtil;

public class BaseBuff {

    public String name;
    public String id;
    public int lv;
    public Object mob;
    public BaseTrigger trigger = new BaseTrigger() {
        @Override
        public boolean onTrigger() {
            return false;
        }

        @Override
        public void reset() {

        }
    };
    public BaseActive active = new BaseActive() {
        @Override
        public boolean isActive() {
            return true;
        }
    };
    public BaseEffect effect = new BaseEffect() {
        @Override
        public Object doEffect(Object... args) {
            return null;
        }
    };

    public BaseBuff(String name,String id,int lv,int mob){
        this.name = name;
        this.id = id;
        this.lv = lv;
        this.mob = mob == -1 ? null : mob;
    }

    public BaseBuff(){
    }

    /**
     * 触发该Buff，可以进行计数器等操作
     * @return true,若该buff应被移除(如超时，或不满足条件等)
     */
    public boolean onTrigger(){
        return trigger.onTrigger();
    }

    /**
     * buff是否生效
     * @return true,当该buff将生效，将触发buff的effect
     */
    public boolean isActive(){
        return active.isActive();
    }

    /**
     * buff的效果
     * @return Object
     */
    public Object doEffect(Object... args){
        return effect.doEffect(args);
    }

    public String removed = null;

    public void setRemovedMessage(String s){
        removed = s;
    }

    public String showOnRemoved(){
        return removed == null ? name + "效果结束了。" : removed;
    }

    public String show = "";

    public void setShowMessage(String s){
        show = s;
    }

    public String showOnActived(){
        return show;
    }

    public String gain = null;

    public void setGainMessage(String s){
        gain = s;
    }

    public String showOnGain(){
        return gain == null ? "获得了" + name + "效果。" : gain;
    }

    public BaseBuff copy(){
        BaseBuff c = new BaseBuff();
        JsUtils.fatherToChild(this,c);
        return c;
    }
}
