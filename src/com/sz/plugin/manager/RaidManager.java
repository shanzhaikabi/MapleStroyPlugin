package com.sz.plugin.manager;

import com.sz.plugin.MainManager;
import com.sz.plugin.status.PlayerStatus;
import com.sz.plugin.utils.MSUtils;
import org.mozilla.javascript.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RaidManager {
    private Object event;
    private long start_time;
    private long end_time;
    private int raid_id;
    private int loot_num;
    private int roll_time;

    public RaidManager(Object event) throws Exception{
        this.event = event;
        start_time = new Date().getTime();
        raid_id = getRaidId();
        MSUtils.doMethod(event,"setVariable","raidid",raid_id);
        initPlayers();
        if (MSUtils.doMethod(event,"getVariable","lootnum") != null)
            loot_num = (int)Math.floor((Double) MSUtils.doMethod(event,"getVariable","lootnum"));
        else loot_num = 0;
    }

    public void mobDied(Object mob) throws Exception {
        end_time = new Date().getTime();
        if (loot_num == 0) return;
        calculateDamage();
        updateLootNum();
        setRoll();
    }

    private void setRoll() throws Exception {
        Object[] members = getMembers();
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
            MSUtils.doMethod(event,"startTimer","waitLoot", 1000);
            for(Object player : members){
                MSUtils.doMethod(player,"scriptProgressMessage","出现了稀有掉落，请在3分钟内完成ROLL点！");
                MSUtils.doMethod(player,"dropMessage",7,"出现了稀有掉落，请在3分钟内完成ROLL点！");
            }
        }
    }

    private void calculateDamage() throws Exception {
        Object[] members = getMembers();
        for(Object player : members){
            PlayerStatus status = MainManager.getInstance().getPlayerStatus((int)MSUtils.doMethod(player,"getId"));
            long totalDamage = status.getTotalDamage();
            long dps = totalDamage / ((end_time - start_time) / 1000);
            MSUtils.doMethod(event,"setVariable",MSUtils.doMethod(player,"getName"),"damage",totalDamage);
            MSUtils.doMethod(event,"setVariable",MSUtils.doMethod(player,"getName"),"dps",dps);
            MSUtils.doMethod(player,"dropAlertNotice","5秒后将打开奖励面板！请不要打开任何NPC！");
        }
        MSUtils.doMethod(event,"startTimer","openNpc", 5 * 1000);
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

    private void setItemFromLoot(Map<String,Object> finalList,int roll_no) throws Exception{
        if (MSUtils.doMethod(event,"getVariable","members") == null) return;
        Object[] members = (Object[]) MSUtils.doMethod(event,"getVariable","members");
        String sql = "insert into sz_raid_roll(raidid,rollno,itemid,quantity,prefix) values (?,?,?,?,?)";
        MSUtils.doMethod(members[0],"customSqlInsert",sql,raid_id, roll_no,finalList.get("itemid"), finalList.get("quantity"), finalList.get("equipdetail"));
    }

    private void updateLootNum(int num) throws Exception{
        Object[] members = getMembers();
        String sql = "update sz_raid_log set status = ? where raidid = ? and status <= 0";
        MSUtils.doMethod(members[0],"customSqlUpdate",sql,num,raid_id);
    }

    private void updateLootNum() throws Exception {
        Object[] members = getMembers();
        String sql = "update sz_raid_log set status = ?, costtime = ? where raidid = ? and status <= 0";
        MSUtils.doMethod(members[0],"customSqlInsert",sql,loot_num,end_time - start_time,raid_id);
    }

    private List<Map<String,Object>> getLootDetail(int loot_detail) throws Exception {
        Object[] members = getMembers();
        String sql = "select * from sz_lootdetail where lootdetail = ?";
        return (List<Map<String,Object>>)MSUtils.doMethod(members[0],"customSqlResult",sql,loot_detail);
    }

    public Object[] getMembers() throws Exception {
        return null;
    }

    private List<Map<String,Object>> getRollList() throws Exception {
        if (MSUtils.doMethod(event,"getVariable","members") == null) throw new Exception();
        Object[] members = getMembers();
        String raid_name = (String) MSUtils.doMethod(event,"getVariable","raidname");
        String sql = "select * from sz_roll where raidname = ? order by rollno";
        return (List<Map<String,Object>>)MSUtils.doMethod(members[0],"customSqlResult",sql,raid_name);
    }

    private void initPlayers() throws Exception {
        Object[] members = getMembers();
        for(Object player : members){
            initPlayer(player);
        }
    }

    private void initPlayer(Object player) throws Exception{
        String sql = "insert into sz_raid_log(raidid,raidname,date,characterid,isleader,status) values (?,?,?,?,?,?)";
        int is_leader = 0;
        if ((int)Math.floor((Double) MSUtils.doMethod(event,"getVariable","leaderid")) == (int)MSUtils.doMethod(player,"getId")) is_leader = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatedDate = sdf.format(new Date(start_time));
        MSUtils.doMethod(player,"customSqlInsert",sql,raid_id,MSUtils.doMethod(event,"getVariable","raidname"),formatedDate,MSUtils.doMethod(player,"getId"),is_leader,0);
    }

    private int getRaidId() throws Exception {
        Object[] members = getMembers();
        String sql = "select max(raidid) as a from sz_raid_log";
        List<Map<String,Object>> rs = (List<Map<String,Object>>)MSUtils.doMethod(members[0],"customSqlResult",sql);
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
        Object[] members = getMembers();
        for(Object player : members){
            MSUtils.doMethod(player,"runScript","0_开箱3");
        }
    }

    private void waitLoot() throws Exception {
        if (roll_time == 180){
            finishLoot();
            return;
        }
        Object[] members = getMembers();
        String sql = "select * from sz_raid_log where raidid = ? and status != -5";
        List<Map<String,Object>>  rs = (List<Map<String,Object>>)MSUtils.doMethod(members[0],"customSqlResult",sql,raid_id);
        for(Map<String,Object> r : rs) {
            if (r == null) continue;
            roll_time++;
            MSUtils.doMethod(event,"startTimer","waitLoot", 1000);
            return;
        }
        finishLoot();
    }

    private void finishLoot() throws Exception {
        if (roll_time > 180) return;
        updateLootNum(-6);
        Object[] members = getMembers();
        for(Object player : members){
            MSUtils.doMethod(player,"scriptProgressMessage","ROLL点已完成，请打开开箱界面查看情况！");
            MSUtils.doMethod(player,"dropMessage",7,"ROLL点已完成，请打开开箱界面查看情况！");
        }
        roll_time = 181;
    }
}
