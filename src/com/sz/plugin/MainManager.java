package com.sz.plugin;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPartyMember;
import com.cms.game.script.binding.ScriptPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainManager {
    private static class MainManagerHolder {
        private static final MainManager INSTANCE = new MainManager();
    }
    public static final MainManager getInstance(){
        return MainManagerHolder.INSTANCE;
    }

    private ScriptEvent event;
    private Map<Integer,PlayerStatus> statusMap = new LinkedHashMap<>();
    private ScriptPlayer player;

    private RaidManager raid_instance = null;

    public void setRaidManager(ScriptEvent event) {
        if (raid_instance == null) {
            this.event = event;
            try {
                raid_instance = new RaidManager(event);
                ScriptPartyMember[] members = raid_instance.getMembers();
                for (ScriptPartyMember player:members) {
                    PlayerStatus status = new PlayerStatus();
                    statusMap.put(player.getId(),status);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        player.dropMessage(6,"" + damage);
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
