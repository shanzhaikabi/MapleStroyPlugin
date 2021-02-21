package com.sz.plugin;

import com.sz.plugin.artifact.buff.effect.BaseDamage;
import com.sz.plugin.artifact.buff.effect.FinalDamage;
import com.sz.plugin.manager.*;
import com.sz.plugin.status.PlayerStatus;
import com.sz.plugin.utils.JsUtils;
import com.sz.plugin.utils.MSUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class MainManager {
    public RaidManager getRaidManager() throws Exception {
        if (raidManager == null) throw new Exception();
        return raidManager;
    }
    public SkillManager getSkillManager() throws Exception {
        if (skillManager == null) throw new Exception();
        return skillManager;
    }
    public Map<Integer,ArtifactManager> getArtifactManagers() throws Exception {
        if (artifactManagers == null) throw new Exception();
        return artifactManagers;
    }
    public ArtifactManager getArtifactManager(int playerId) throws Exception {
        if (!artifactManagers.containsKey(playerId)) throw new Exception();
        return artifactManagers.get(playerId);
    }
    public BuffManager getBuffManager(int playerId) throws Exception {
        if (!buffManagers.containsKey(playerId)) throw new Exception();
        return buffManagers.get(playerId);
    }
    public Collection<BuffManager> getBuffManagers() {
        return buffManagers.values();
    }

    public void removePlayer(int playerId){
        try{
            artifactManagers.remove(playerId);
            buffManagers.remove(playerId);
        }
        catch (Exception e){};
    }

    private static class MainManagerHolder {
        private static final MainManager INSTANCE = new MainManager();
    }
    public static final MainManager getInstance(){
        return MainManagerHolder.INSTANCE;
    }

    public void saveToFile(String path, String str) throws IOException {
        JsUtils.SaveToFile(path,str);
    }

    public void copyFile(String src,String tar) throws Exception {
        JsUtils.copyFile(src,tar);
    }

    public void searchClass() throws IOException, ClassNotFoundException {
        JsUtils.searchClass();
    }

    public void saveClass(Class cls) throws IOException {
        JsUtils.SaveClass(cls);
    }

    public void searchClass(String className) throws IOException, ClassNotFoundException {
        JsUtils.searchClass(className);
    }

    public Object[] members = null;
    public Object event = null;
    private long totalDamage = 0;

    private Map<Integer, PlayerStatus> statusMap = new LinkedHashMap<>();

    private RaidManager raidManager = null;
    private SkillManager skillManager = null;
    private Map<Integer,ArtifactManager> artifactManagers = new LinkedHashMap<>();
    private Map<Integer, BuffManager> buffManagers = new LinkedHashMap<>();
    private long timestamp;

    public void init(Object[] members,Object event) throws Exception{
        try{
            //注册members
            this.members = members;
            for (Object m : members) {
                int pid = (int)MSUtils.doMethod(m,"getId");
                artifactManagers.put(pid,new ArtifactManager(m));
                buffManagers.put(pid,new BuffManager(m));
            }
            this.event = event;
            timestamp = 0;
        }
        catch (Exception e){
            e.printStackTrace();
            JsUtils.PrintMessage(e);
            throw e;
        }
    }

    public void mobDied(Object mob){
        if (raidManager != null){
            try {
                raidManager.mobDied(mob);
            }catch (Exception e){
                close();
            }
        }
    }

    public long act(String str) throws Exception {
        str = str.replace(",","").split("_")[1];
        String[] s = str.split(":");
        String type = s[0];
        String[] args = s[1].split("\\|");
        return ActManager.dealWithAct(type,args);
    }

    public long mobHit(Object player,Object mob,Object damage) throws Exception {
        try{
            long dmg = ((Double)damage).longValue();
            if (timestamp == System.currentTimeMillis()) return 0;//防止多次触发mobHit
            if (dmg < 10000000) return dmg;//取消小伤害对系统的影响.
            timestamp = System.currentTimeMillis();
            int pid = (int)MSUtils.doMethod(player,"getId");
            //处理buff失效情况
            getBuffManager(pid).dealWithOnTrigger();
            //处理最终伤害
            long baseDmg = dmg + getBuffManager(pid).dealWithBaseDamage(dmg,mob);
            //检查是否发动技能
            long artifactDamage = getArtifactManager(pid).dealWithArtifact(mob,baseDmg);
            //处理追加伤害
            long finalDamage = getBuffManager(pid).dealWithFinalDamage(dmg,mob);
            long extraDamage = baseDmg /*+ artifactDamage*/ + finalDamage - dmg;
        /*long bossHp = (long) MSUtils.doMethod(mob,"getHp");
        long curHp = bossHp - extraDamage;
        if (curHp < 0) curHp = 0;
        MSUtils.doMethod(mob,"setHp",curHp);*/
            if (extraDamage > 0)
                MSUtils.doMethod(mob,"hurt",extraDamage);
            getArtifactManager(pid).addDamage(extraDamage + dmg);
            return extraDamage + dmg;
        }
        catch (Exception e){
            e.printStackTrace();
            JsUtils.PrintMessage(e);
            throw e;
        }
    }

    public void timerExpired(String key){
        if (raidManager != null){
            try {
                raidManager.timerExpired(key);
            } catch (Exception e){
                close();
            }
        }
    }

    public void calculateTotalDamage() throws Exception {
        for (ArtifactManager x:getArtifactManagers().values()) {
            totalDamage += x.totalDamage;
        }
    }

    public double getDamagePercent(int id) throws Exception {
        if (totalDamage == 0) calculateTotalDamage();
        return getArtifactManager(id).totalDamage * 1.0 / totalDamage;
    }

    public double getDamage(int id) throws Exception {
        if (totalDamage == 0) calculateTotalDamage();
        return getArtifactManager(id).totalDamage;
    }

    public void close(){
        raidManager = null;
    }

    public PlayerStatus getPlayerStatus(int id) throws Exception {
        if (statusMap.get(id) != null) return statusMap.get(id);
        throw new Exception();
    }

    public Map<Integer, PlayerStatus> getPlayerStatusMap(){
        return statusMap;
    }
}
