package com.sz.plugin.manager;

import com.cms.game.script.binding.ScriptEvent;
import com.cms.game.script.binding.ScriptMob;
import com.cms.game.script.binding.ScriptPartyMember;
import com.cms.game.script.binding.ScriptPlayer;
import com.sz.plugin.MainManager;
import com.sz.plugin.status.PlayerStatus;
import org.mozilla.javascript.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RaidManager {
    private ScriptEvent event;
    private long start_time;
    private long end_time;
    private int raid_id;
    private int loot_num;
    private int roll_time;

    public RaidManager(ScriptEvent event) throws Exception{
        this.event = event;
        start_time = new Date().getTime();
        raid_id = getRaidId();
        event.setVariable("raidid",raid_id);
        initPlayers();
        if (event.getVariable("lootnum") != null)
            loot_num = (int)Math.floor((Double) event.getVariable("lootnum"));
        else loot_num = 0;
    }

    public void mobDied(ScriptMob mob) throws Exception {
        end_time = new Date().getTime();
        if (loot_num == 0) return;
        calculateDamage();
        updateLootNum();
        setRoll();
    }

    private void setRoll() throws Exception {
        ScriptPartyMember[] members = getMembers();
        List<Map<String,Object>> rs = getRollList();
        int roll_no = 1;
        for (Map<String, Object> r : rs) {
            int chance = (int) r.get("chance");
            int loot_detail_id = (int) r.get("lootdetail");
            int ran = (int) Math.floor(Math.random() * 100);
            if (ran > chance) continue;
            List<Map<String, Object>> loot_detail = getLootDetail(loot_detail_id);
            Map<String, Object> detail = getFinalList(loot_detail);
            setItemFromLoot(detail, roll_no);
            roll_no++;
        }
        if (roll_no > 1){//有roll的东西
            event.startTimer("waitLoot", 1000);
            for(ScriptPartyMember player : members){
                player.scriptProgressMessage("出现了稀有掉落，请在3分钟内完成ROLL点！");
                player.dropMessage(7,"出现了稀有掉落，请在3分钟内完成ROLL点！");
            }
        }
    }

    private void calculateDamage() throws Exception {
        ScriptPartyMember[] members = getMembers();
        for(ScriptPartyMember player : members){
            PlayerStatus status = MainManager.getInstance().getPlayerStatus(player.getId());
            long totalDamage = status.getTotalDamage();
            long dps = totalDamage / ((end_time - start_time) / 1000);
            event.setVariable(player.getName()+"damage",totalDamage);
            event.setVariable(player.getName()+"dps",dps);
            player.dropAlertNotice("5秒后将打开奖励面板！请不要打开任何NPC！");
        }
        event.startTimer("openNpc", 5 * 1000);
    }

    private Map<String,Object> getFinalList(List<Map<String,Object>> lootDetail){
        int heavy = 0;
        for (Map<String, Object> stringObjectMap : lootDetail) {
            heavy += (int) stringObjectMap.get("heavy");
        }
        int ran = (int)Math.floor(Math.random() * heavy);
        for (Map<String, Object> stringObjectMap : lootDetail) {
            ran -= (int) stringObjectMap.get("heavy");
            if (ran < 0) return stringObjectMap;
        }
        return null;
    }

    private void setItemFromLoot(Map<String,Object> finalList,int roll_no){
        if (event.getVariable("members") == null) return;
        ScriptPartyMember[] members = (ScriptPartyMember[])event.getVariable("members");
        String sql = "insert into sz_raid_roll(raidid,rollno,itemid,quantity,prefix) values (?,?,?,?,?)";
        members[0].customSqlInsert(sql,raid_id, roll_no,finalList.get("itemid"), finalList.get("quantity"), finalList.get("equipdetail"));
    }

    private void updateLootNum(int num) throws Exception{
        ScriptPartyMember[] members = getMembers();
        String sql = "update sz_raid_log set status = ? where raidid = ? and status <= 0";
        members[0].customSqlUpdate(sql,num,raid_id);
    }

    private void updateLootNum() throws Exception {
        ScriptPartyMember[] members = getMembers();
        String sql = "update sz_raid_log set status = ?, costtime = ? where raidid = ? and status <= 0";
        members[0].customSqlInsert(sql,loot_num,end_time - start_time,raid_id);
    }

    private List<Map<String,Object>> getLootDetail(int loot_detail) throws Exception {
        ScriptPartyMember[] members = getMembers();
        String sql = "select * from sz_lootdetail where lootdetail = ?";
        return members[0].customSqlResult(sql,loot_detail);
    }

    public ScriptPartyMember[] getMembers() throws Exception {
        if (event.getVariable("members") == null) throw new Exception();
        NativeArray array = (NativeArray) event.getVariable("members");
        return (ScriptPartyMember[]) array.toArray(new ScriptPartyMember[array.size()]);
    }

    private List<Map<String,Object>> getRollList() throws Exception {
        if (event.getVariable("raidname") == null) throw new Exception();
        ScriptPartyMember[] members = getMembers();
        String raid_name = (String) event.getVariable("raidname");
        String sql = "select * from sz_roll where raidname = ? order by rollno";
        return members[0].customSqlResult(sql,raid_name);
    }

    private void initPlayers() throws Exception {
        ScriptPartyMember[] members = getMembers();
        for(ScriptPartyMember player : members){
            initPlayer(player);
        }
    }

    private void initPlayer(ScriptPlayer player){
        String sql = "insert into sz_raid_log(raidid,raidname,date,characterid,isleader,status) values (?,?,?,?,?,?)";
        int is_leader = 0;
        if ((int)Math.floor((Double) event.getVariable("leaderid")) == player.getId()) is_leader = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatedDate = sdf.format(new Date(start_time));
        player.customSqlInsert(sql,raid_id,event.getVariable("raidname"),formatedDate,player.getId(),is_leader,0);
    }

    private int getRaidId() throws Exception {
        ScriptPartyMember[] members = getMembers();
        String sql = "select max(raidid) as a from sz_raid_log";
        List<Map<String,Object>> rs = members[0].customSqlResult(sql);
        if (rs == null || rs.size() == 0) {
            return 1;
        }
        return (int) rs.get(0).get("a") + 1;
    }

    public void timerExpired(String key) {
        switch (key){
            case "openNpc":
                try{
                    openNpc();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "waitLoot":
                try{
                    waitLoot();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void openNpc() throws Exception {
        ScriptPartyMember[] members = getMembers();
        for(ScriptPartyMember player : members){
            player.runScript("0_开箱3");
        }
    }

    private void waitLoot() throws Exception {
        if (roll_time == 180){
            finishLoot();
            return;
        }
        ScriptPartyMember[] members = getMembers();
        String sql = "select * from sz_raid_log where raidid = ? and status != -5";
        List<Map<String,Object>>  rs = members[0].customSqlResult(sql,raid_id);
        for(Map<String,Object> r : rs) {
            if (r == null) continue;
            roll_time++;
            event.startTimer("waitLoot", 1000);
            return;
        }
        finishLoot();
    }

    private void finishLoot() throws Exception {
        if (roll_time > 180) return;
        updateLootNum(-6);
        ScriptPartyMember[] members = getMembers();
        for(ScriptPartyMember player : members){
            player.scriptProgressMessage("ROLL点已完成，请打开开箱界面查看情况！");
            player.dropMessage(7,"ROLL点已完成，请打开开箱界面查看情况！");
        }
        roll_time = 181;
    }
}
