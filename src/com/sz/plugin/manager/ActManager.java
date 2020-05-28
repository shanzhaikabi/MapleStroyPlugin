package com.sz.plugin.manager;

import com.sz.plugin.MainManager;
import com.sz.plugin.artifact.BaseArtifact;
import com.sz.plugin.artifact.effect.BuffArtifact;
import com.sz.plugin.utils.MSUtils;

import java.text.MessageFormat;
import java.util.Random;

public class ActManager {
    public static long doArtifact(String id) throws Exception {
        int pid = Integer.valueOf(id);
        return MainManager.getInstance().getArtifactManager(pid).dealWithArtifactAct();
    }

    public static long dealWithAct(String type, String[] args) throws Exception {
        switch (type){
            case "hit":
                hit(args[0],args[1],args[2],args[3]);
                break;
            case "artifact":
                return doArtifact(args[0]);
        }
        return 0;
    }

    public static void hit(String pid,String mid,String i,String dmg) throws Exception{
        int entityId = Integer.valueOf(mid);
        long damage = Long.valueOf(dmg);
        int playerId = Integer.valueOf(pid);
        Object player = MainManager.getInstance().getArtifactManager(playerId).player;
        int m = Integer.valueOf(mid);
        Object mob = MobManager.getMob(m);
        if (mob != null){
            MSUtils.doMethod(mob,"hurt",damage);
            return;
        }
        //MSUtils.doMethod(player,"dropMessage",6,"无法获取实体！");
    }

    public static long doHurt(Object player,Object mob,long dmg,int hits,int delay,boolean block) throws Exception {
        long total = 0;
        int pid = (int) MSUtils.doMethod(player,"getId");
        int mid = (int) MSUtils.doMethod(mob,"getEntityId");
        Object event = MainManager.getInstance().event;
        if (delay == 0) delay = 150;
        Random random = new Random();
        for(int i = 1;i <= hits;i++){
            long d = Math.round(dmg * 1.0 / hits);
            if (!block)
            d = Math.round(d * 1.0 * (random.nextDouble() * 0.2 + 0.9));
            MSUtils.doMethod(event,"startTimer"
                    , MessageFormat.format("sz_hit:{0}|{1}|{2}|{3}",pid,mid,i,d)
                    ,delay * i);
            total += d;
        }
        MainManager.getInstance().getArtifactManager(pid).addDamage(total);
        return total;
    }
}
