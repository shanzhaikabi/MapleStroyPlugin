package com.sz.plugin;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPartyMember;
import com.cms.game.script.binding.ScriptPlayer;
import com.sz.plugin.manager.RaidManager;
import com.sz.plugin.manager.SkillManager;
import com.sz.plugin.status.PlayerStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainManager {
    public RaidManager getRaidManager() throws Exception {
        if (raidManager == null) throw new Exception();
        return raidManager;
    }
    public SkillManager getSkillManager() throws Exception {
        if (skillManager == null) throw new Exception();
        return skillManager;
    }

    private static class MainManagerHolder {
        private static final MainManager INSTANCE = new MainManager();
    }
    public static final MainManager getInstance(){
        return MainManagerHolder.INSTANCE;
    }

    private ScriptEvent event;
    private Map<Integer, PlayerStatus> statusMap = new LinkedHashMap<>();
    private ScriptPlayer player;

    private RaidManager raidManager = null;
    private SkillManager skillManager = null;

    public void setRaidManager(ScriptEvent event) {
        if (raidManager == null) {
            this.event = event;
            try {
                raidManager = new RaidManager(event);
                ScriptPartyMember[] members = raidManager.getMembers();
                for (ScriptPartyMember player:members) {
                    PlayerStatus status = new PlayerStatus();
                    statusMap.put(player.getId(),status);
                }
                skillManager = new SkillManager();
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }
    }

    public void mobDied(ScriptMob mob){
        if (raidManager != null){
            try {
                raidManager.mobDied(mob);
            }catch (Exception e){
                close();
            }
        }
    }

    public void mobHit(ScriptPlayer player,ScriptMob mob,Long damage){
        player.dropMessage(6,"" + damage);
    }

    public void timerExpired(String key){
        if (raidManager != null){
            try {
                raidManager.timerExpired(key);
            }catch (Exception e){
                close();
            }
        }
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
