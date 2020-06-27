package com.sz.plugin.manager;

import com.sz.plugin.MainManager;
import com.sz.plugin.artifact.BaseArtifact;
import com.sz.plugin.artifact.buff.BaseBuff;
import com.sz.plugin.artifact.buff.StackableBuff;
import com.sz.plugin.artifact.buff.effect.*;
import com.sz.plugin.utils.MSUtils;

import java.text.MessageFormat;
import java.util.*;

public class BuffManager {

    int maxPerLine = 5;//must be odd
    int zy = -40;
    int zx = 20;

    class BuffEffect {
        public int pos;
        public String id;
        public long ts;
        public String path;
        public int duration;
        public BuffEffect(String id, String path, int duration){
            this.id = id;
            this.path = path;
            this.duration = duration;
            this.ts = new Date().getTime() + duration;
        }
    }

    public Map<String,BaseBuff> buffList = new LinkedHashMap<>();

    int id;
    Object player;

    public Map<String, BuffEffect> pendingEffect = new LinkedHashMap<>();
    public Map<String, BuffEffect> curEffectMap = new LinkedHashMap<>();

    public int getMinPos(){
        int pos = 99999;
        for(BuffEffect e : curEffectMap.values()){
            pos = Math.min(pos,e.pos);
        }
        if (pos == 99999) pos = -1;
        pos++;
        return pos;
    }

    public void addPendingBuff(String id,String path,int duration) throws Exception {
        BuffEffect e = new BuffEffect(id, path, duration);
        Object event = MainManager.getInstance().event;
        if (!pendingEffect.containsKey(e.id)){
            pendingEffect.put(e.id,e);
        }
        else{
            BuffEffect effect = pendingEffect.get(e.id);
            if (effect.ts >= e.ts) return;
            pendingEffect.put(e.id,e);
        }
        if (!curEffectMap.containsKey(e.id)){
            MSUtils.doMethod(event,"startTimer"
                    , MessageFormat.format("sz_checkBuff:{0}",id)
                    , 0);//起始
        }
        else{
            long t = curEffectMap.get(e.id).ts - (e.ts - e.duration);
            int tt = (int) t;
            MSUtils.doMethod(event,"startTimer"
                    , MessageFormat.format("sz_checkBuff:{0}",id)
                    , tt);
        }
        MSUtils.doMethod(event,"startTimer"
                , MessageFormat.format("sz_checkBuff:{0}",id)
                , e.duration);//终止
    }

    public void showBuffEffect(String path, int duration, int pos) throws Exception {
        int y = pos / maxPerLine * zy;
        int xx = pos % maxPerLine + 1;
        int x = (xx / 2) * ((xx % 2 == 1) ? -1 : 1);

        MSUtils.doMethod(player,"setInGameDirectionMode",true,false,false,true,"");
        MSUtils.doMethod(player,"showNpcEffectPlay",0,path,duration,x,y,true,0,true,0,"");
        MSUtils.doMethod(player,"setInGameDirectionMode",false,true,false,false,"");
    }

    public void checkBuff(boolean forced) throws Exception {
        //remove timeout
        List<String> l = dealWithOnTrigger();
        for(String i : l){
            BuffEffect buff = curEffectMap.get(i);
            curEffectMap.remove(i);
        }
        l = new ArrayList<>();
        //check player status;
        if (!MainManager.getInstance().getArtifactManager(id).isAuto) return;
        for(String k : pendingEffect.keySet()){
            BuffEffect e = pendingEffect.get(k);
            int d = (int)(e.ts - new Date().getTime());
            if (d <= 0) l.add(k);
            else if (!curEffectMap.containsKey(k)){
                BuffEffect effect = pendingEffect.get(k);
                effect.pos = getMinPos();
                showBuffEffect(effect.path,d,effect.pos);
                curEffectMap.put(k,effect);
                l.add(k);
            }
        }
        for(String i : l){
            pendingEffect.remove(i);
        }
    }

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

    public List<String> dealWithOnTrigger() throws Exception {
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
        return l;
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
                extra += (long) buff.doEffect(new Object[]{damage, buff.lv});
            else
                extra += (long) buff.doEffect(new Object[]{damage});
        }
        return extra;
    }
}
