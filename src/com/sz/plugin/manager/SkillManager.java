package com.sz.plugin.manager;

import com.cms.game.script.binding.ScriptPlayer;

import java.util.List;
import java.util.Map;

public class SkillManager {
    public SkillManager(ScriptPlayer[] members){
        for (ScriptPlayer player : members) {
            List<Map<String,Object>> rs = skillSQLManager.loadPassiveSkills(player);

        }
    }


    private void registerSkill(String effect){
        
    }
}

class skillSQLManager{

    public static List<Map<String,Object>> loadPassiveSkills(ScriptPlayer player){
        String sql = "select * from sz_skillinventory si,sz_skilldetail sd,sz_skillinfo sf " +
                "where si.characterid = ? and si.skillid = sf.skillid and sf.type = 'passive' and sd.skillid = si.skillid";
        List rs = player.customSqlResult(sql, player.getId());
        return rs;
    }
}
