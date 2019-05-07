package com.sz.plugin.raid;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPartyMember;
import com.cms.game.script.binding.ScriptPlayer;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RaidManager {
    private ScriptEvent event;
    private long start_time;
    private long end_time;
    private int raid_id;
    private int lootnum;

    public RaidManager(ScriptEvent event) throws Exception{
        this.event = event;
        start_time = new Date().getTime();
        raid_id = getRaidId();
        event.setVariable("raidid",raid_id);
        initPlayers();
    }

    public void mobDied(ScriptMob mob) throws Exception {
        if (event.getVariable("members") == null) throw new Exception();
        end_time = new Date().getTime();
        if (lootnum == 0) return;
        updateLootNum();
        List<Map<String,Object>> rs = getRollList();
        if (getRollList() == null) return;
        int rollno = 1;
        for(int i = 0;i < rs.size();i++){
            int chance = (int) rs.get(i).get("chance");
            int loot_detail_id = (int) rs.get(i).get("lootdetail");
            int ran = (int)Math.floor(Math.random() * 100);
            if (ran > chance) continue;
            List<Map<String,Object>> loot_detail = getLootDetail(loot_detail_id);
            {
                Map<String, Object> detail = getFinalList(loot_detail);
                setItemFromLoot(raidid, detail.get("itemid"), detail.get("quantity"), rollno, detail.get("equipdetail"));
            }
            rollno++;
        }
    }

    private void updateLootNum() {
        if (event.getVariable("members") == null) return;
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        String sql = "update sz_raid_log set status = ?, costtime = ? where raidid = ? and status <= 0";
        members[0].customSqlUpdate(sql,lootnum,end_time - start_time,raid_id);
    }

    private List<Map<String,Object>> getLootDetail(int loot_detail) throws Exception {
        if (event.getVariable("members") == null) throw new Exception();
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        String sql = "select * from sz_lootdetail where lootdetail = ?";
        return members[0].customSqlResult(sql,loot_detail);
    }

    private List<Map<String,Object>> getRollList() throws Exception {
        if (event.getVariable("raidname") == null) throw new Exception();
        if (event.getVariable("members") == null) throw new Exception();
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        String raid_name = (String) event.getVariable("raidname");
        String sql = "select * from raidname = ? order by rollno";
        return members[0].customSqlResult(sql,raid_name);
    }

    private void initPlayers() throws Exception {
        if (event.getVariable("members") == null) throw new Exception();
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        for(ScriptPartyMember player : members){
            initPlayer(player);
        }
    }

    private void initPlayer(ScriptPlayer player){
        String sql = "insert into sz_raid_log(raidid,raidname,date,characterid,isleader,status) values (?,?,?,?,?,?)";
        java.sql.Date date = new java.sql.Date(start_time);
        player.customSqlInsert(sql,raid_id,date,player.getId(),((int)event.getVariable("leaderid") == player.getId()) ? 1 : 0,0);
    }

    private int getRaidId() throws Exception {
        if (event.getVariable("members") == null) throw new Exception();
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        if (members.length == 0) throw new Exception();
        String sql = "select max(raidid) as a from sz_raid_log";
        List<Map<String,Object>> rs = members[0].customSqlResult(sql);
        if (rs == null || rs.size() == 0) {
            return 1;
        }
        return (int) rs.get(0).get("a");
    }
}
