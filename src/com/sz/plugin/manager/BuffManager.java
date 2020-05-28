package com.sz.plugin.manager;

import com.sz.plugin.artifact.BaseArtifact;
import com.sz.plugin.artifact.buff.BaseBuff;
import com.sz.plugin.artifact.buff.StackableBuff;
import com.sz.plugin.artifact.buff.effect.*;
import com.sz.plugin.utils.MSUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BuffManager {

    public Map<String,BaseBuff> buffList = new LinkedHashMap<>();

    int id;
    Object player;

    public BuffManager(Object player) throws Exception {
        this.player = player;
        this.id = (int) MSUtils.doMethod(player,"getId");
    }

    public String showBuff(){
        String str = "当前生效的buff: ";
        for (BaseBuff buff : buffList.values()){
            str += buff.name;
            if (buff instanceof StackableBuff)
                str +=  "(" + buff.lv + ")";
            if (buff.mob != null) {
                str += "=>" + buff.mob.toString().split(" ")[1].split("\\(")[0];
            }
            str += "\t";
        }
        return str;
    }

    public void addBuff(BaseBuff _buff) throws Exception {
        BaseBuff buff = _buff.copy();
        buff.trigger.reset();
        //无此buff
        if (!buffList.containsKey(buff.id)){
            buffList.put(buff.id,buff);
            MSUtils.showMessage(player,buff.showOnGain());
            MSUtils.showMessage(player,showBuff());
            return;
        }
        //可堆叠
        if (buff instanceof StackableBuff){
            try{
                StackableBuff s_buff = (StackableBuff) buff;
                StackableBuff curBuff = (StackableBuff) buffList.get(buff.id);
                if (s_buff.maxLv < curBuff.lv) return;//目标buff的堆叠上限低于当前已有，则不叠加且不刷新
                if (s_buff.maxLv > curBuff.maxLv) curBuff.maxLv = s_buff.maxLv;//更新上限
                curBuff.lv += s_buff.lv;
                curBuff.lv = curBuff.lv > curBuff.maxLv ? curBuff.maxLv : curBuff.lv;
                curBuff.trigger.reset();
                MSUtils.showMessage(player,buff.showOnGain());
                MSUtils.showMessage(player,showBuff());
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                return;
            }
        }
        //等级更低
        if (buffList.get(buff.id).lv > buff.lv) return;
        buffList.replace(buff.id,buff);
        MSUtils.showMessage(player,buff.showOnGain());
        MSUtils.showMessage(player,showBuff());
    }

    public void dealWithOnTrigger() throws Exception {
        List<String> l = new ArrayList<>();
        for (String key : buffList.keySet()) {
            BaseBuff buff = buffList.get(key);
            if (buff.onTrigger()){
                l.add(key);
            }
        }
        for(String i : l){
            BaseBuff buff = buffList.get(i);
            MSUtils.showMessage(player,buff.showOnRemoved());
            buffList.remove(i);
        }
    }

    public boolean checkBuffWithMob(BaseBuff buff,Object mob){
        return buff.mob == null || buff.mob == mob;
    }

    public String showBuffWhenArtifactActive(){
        String str = "";
        for (BaseBuff u : buffList.values()) {
            str += u.showOnActived();
        }
        str = str.length() > 0 ? "[" + str + "]" : "";
        return str;
    }

    public long dealWithDamage(Class type, long damage,Object mob){
        long extra = 0;
        for (BaseBuff buff : buffList.values()){
            if (type.isInstance(buff.effect)){
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithBaseDamage(long damage,Object mob){
        long extra = 0;
        for (BaseBuff buff : buffList.values()){
            if (buff.effect instanceof BaseDamage){
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithArtifactDamage(long damage, Object mob, BaseArtifact artifact){
        long extra = 0;
        for (BaseBuff buff : buffList.values()){
            if (buff.effect instanceof ArtifactDamage){
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    public long dealWithFinalDamage(long damage,Object mob){
        long extra = 0;
        for (BaseBuff buff : buffList.values()){
            if (buff.effect instanceof FinalDamage){
                extra = dealWithNormalDamage(damage, mob, extra, buff);
            }
        }
        return extra;
    }

    private long dealWithNormalDamage(long damage, Object mob, long extra, BaseBuff buff) {
        if (!checkBuffWithMob(buff,mob)) return extra;
        if (buff.isActive()){
            if (buff instanceof StackableBuff)
                extra += (long) buff.doEffect(damage, buff.lv);
            else
                extra += (long)buff.doEffect(damage);
        }
        return extra;
    }
}
