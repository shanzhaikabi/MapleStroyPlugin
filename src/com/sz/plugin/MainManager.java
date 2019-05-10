package com.sz.plugin;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPlayer;

import java.util.Map;

public class MainManager {
    private static class MainManagerHolder {
        private static final MainManager INSTANCE = new MainManager();
    }
    public static final MainManager getInstance(){
        return MainManagerHolder.INSTANCE;
    }

    private ScriptEvent event;
    public ScriptPlayer player;
    public ScriptPlayer[] members;
    private Map<Integer,PlayerStatus> statusMap;

    private RaidManager raid_instance;

    public void setRaidManger(ScriptEvent event) {
        if (raid_instance == null) {
            this.event = event;
            try {
                raid_instance = new RaidManager(event);
            } catch (Exception e) {
                close();
            }
        }
    }

    public void mobDied(ScriptMob mob){
        if (raid_instance != null){
            try {
                raid_instance.mobDied(mob);
            }catch (Exception e){
                close();
            }
        }
    }

    public void mobHit(ScriptPlayer player,ScriptMob mob,Long damage){

    }

    public void timerExpired(String key){
        if (raid_instance != null){
            try {
                raid_instance.timerExpired(key);
            }catch (Exception e){
                close();
            }
        }
    }

    public void close(){
        raid_instance = null;

    }

    public PlayerStatus getPlayerStatus(int id) throws Exception {
        if (statusMap.get(id) != null) return statusMap.get(id);
        throw new Exception();
    }
}
