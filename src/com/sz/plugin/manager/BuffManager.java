package com.sz.plugin.manager;

import com.sz.plugin.artifact.BaseArtifact;
import com.sz.plugin.artifact.buff.BaseBuff;
import com.sz.plugin.artifact.buff.BaseTrigger;
import com.sz.plugin.artifact.buff.StackableBuff;
import com.sz.plugin.artifact.buff.effect.ArtifactDamage;
import com.sz.plugin.artifact.buff.effect.BaseDamage;
import com.sz.plugin.artifact.buff.effect.FinalDamage;
import com.sz.plugin.utils.MSUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuffManager
{
    public Map<String, BaseBuff> buffList = new LinkedHashMap();
    int id;
    Object player;

    public BuffManager(Object player)
            throws Exception
    {
        this.player = player;
        this.id = ((Integer)MSUtils.doMethod(player, "getId")).intValue();
    }

    public String showBuff() {
        String str = "当前生效的buff: ";
        for (BaseBuff buff : this.buffList.values()) {
            str = str + buff.name;
            if ((buff instanceof StackableBuff))
                str = str + "(" + buff.lv + ")";
            if (buff.mob != null) {
                str = str + "=>" + buff.mob.toString().split(" ")[1].split("\\(")[0];
            }
            str = str + "\t";
        }
        return str;
    }

    public void addBuff(BaseBuff _buff) throws Exception {
        BaseBuff buff = _buff.copy();
        buff.trigger.reset();

        if (!this.buffList.containsKey(buff.id)) {
            this.buffList.put(buff.id, buff);
            MSUtils.showMessage(this.player, buff.showOnGain());
            MSUtils.showMessage(this.player, showBuff());
            return;
        }

        if ((buff instanceof StackableBuff)) {
            try {
                StackableBuff s_buff = (StackableBuff)buff;
                StackableBuff curBuff = (StackableBuff)this.buffList.get(buff.id);
                if (s_buff.maxLv < curBuff.lv)
                {
                    return;
                }
                if (s_buff.maxLv > curBuff.maxLv) curBuff.maxLv = s_buff.maxLv;
                curBuff.lv += s_buff.lv;
                curBuff.lv = (curBuff.lv > curBuff.maxLv ? curBuff.maxLv : curBuff.lv);
                curBuff.trigger.reset();
                MSUtils.showMessage(this.player, buff.showOnGain());
                MSUtils.showMessage(this.player, showBuff());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                return;
            }
        }

        if (((BaseBuff)this.buffList.get(buff.id)).lv > buff.lv) return;
        this.buffList.replace(buff.id, buff);
        MSUtils.showMessage(this.player, buff.showOnGain());
        MSUtils.showMessage(this.player, showBuff());
    }

    public void dealWithOnTrigger() throws Exception {
        List l = new ArrayList();
        for (String key : this.buffList.keySet()) {
            BaseBuff buff = (BaseBuff)this.buffList.get(key);
            if (buff.onTrigger()) {
                l.add(key);
            }
        }
        for (Object i : l) {
            BaseBuff buff = (BaseBuff)this.buffList.get(i);
            MSUtils.showMessage(this.player, buff.showOnRemoved());
            this.buffList.remove(i);
        }
    }

    public boolean checkBuffWithMob(BaseBuff buff, Object mob) {
        return (buff.mob == null) || (buff.mob == mob);
    }

    public String showBuffWhenArtifactActive() {
        String str = "";
        for (BaseBuff u : this.buffList.values()) {
            str = str + u.showOnActived();
        }
        str = str.length() > 0 ? "[" + str + "]" : "";
        return str;
    }

    public long dealWithDamage(Class type, long damage, Object mob) {
        long extra = 0L;
        for (BaseBuff buff : this.buffList.values()) {
            if (type.isInstance(buff.effect)) {
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithBaseDamage(long damage, Object mob) {
        long extra = 0L;
        for (BaseBuff buff : this.buffList.values()) {
            if ((buff.effect instanceof BaseDamage)) {
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithArtifactDamage(long damage, Object mob, BaseArtifact artifact) {
        long extra = 0L;
        for (BaseBuff buff : this.buffList.values()) {
            if ((buff.effect instanceof ArtifactDamage)) {
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithFinalDamage(long damage, Object mob) {
        long extra = 0L;
        for (BaseBuff buff : this.buffList.values()) {
            if ((buff.effect instanceof FinalDamage)) {
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    private long dealWithNormalDamage(long damage, Object mob, long extra, BaseBuff buff) {
        if (!checkBuffWithMob(buff, mob)) return extra;
        if (buff.isActive()) {
            if ((buff instanceof StackableBuff))
                extra += ((Long)buff.doEffect(new Object[] { Long.valueOf(damage), Integer.valueOf(buff.lv) })).longValue();
            else
                extra += ((Long)buff.doEffect(new Object[] { Long.valueOf(damage) })).longValue();
        }
        return extra;
    }
}