package com.sz.plugin.artifact.buff;

import com.sz.plugin.utils.JsUtils;

public class BaseBuff
{
    public String name;
    public String id;
    public int lv;
    public Object mob;
    public BaseTrigger trigger = new BaseTrigger()
    {
        public boolean onTrigger() {
            return false;
        }

        public void reset()
        {
        }
    };

    public BaseActive active = new BaseActive()
    {
        public boolean isActive() {
            return true;
        }
    };

    public BaseEffect effect = new BaseEffect()
    {
        public Object doEffect(Object[] args) {
            return null;
        }
    };

    public String removed = null;

    public String show = "";

    public String gain = null;

    public BaseBuff(String name, String id, int lv, int mob)
    {
        this.name = name;
        this.id = id;
        this.lv = lv;
        this.mob = (mob == -1 ? null : Integer.valueOf(mob));
    }

    public BaseBuff()
    {
    }

    public boolean onTrigger()
    {
        return this.trigger.onTrigger();
    }

    public boolean isActive()
    {
        return this.active.isActive();
    }

    public Object doEffect(Object[] args)
    {
        return this.effect.doEffect(args);
    }

    public void setRemovedMessage(String s)
    {
        this.removed = s;
    }

    public String showOnRemoved() {
        return this.removed == null ? this.name + "效果结束了。" : this.removed;
    }

    public void setShowMessage(String s)
    {
        this.show = s;
    }

    public String showOnActived() {
        return this.show;
    }

    public void setGainMessage(String s)
    {
        this.gain = s;
    }

    public String showOnGain() {
        return this.gain == null ? "获得了" + this.name + "效果。" : this.gain;
    }

    public BaseBuff copy() {
        BaseBuff c = new BaseBuff();
        JsUtils.fatherToChild(this, c);
        return c;
    }
}