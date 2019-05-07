package com.sz.plugin;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPlayer;
import com.sz.plugin.raid.RaidManager;

public class MainManager {
    private static class MainManagerHolder {
        private static final MainManager INSTANCE = new MainManager();
    }
    public static final MainManager getInstance(){
        return MainManagerHolder.INSTANCE;
    }

    public static ScriptEvent event;
    public static ScriptPlayer player;
    public static ScriptPlayer[] members;

    private RaidManager raid_instance;

    public RaidManager getRaidManger() {
        if (raid_instance == null) {
            try {
                raid_instance = new RaidManager(event);
                return raid_instance;
            } catch (Exception e) {
                return null;
            }
        }
        return raid_instance;
    }

    public void mobDied(ScriptMob mob){
        if (raid_instance != null){
            try {
                raid_instance.mobDied(mob);
            }catch (Exception e){

            }
        }
    }

    public void close(){
        raid_instance = null;

    }
}
