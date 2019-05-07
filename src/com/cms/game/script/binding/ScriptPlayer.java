package com.cms.game.script.binding;

public class ScriptPlayer {
    public void	dropMessage(int type, java.lang.String message){
        System.out.println("dropMessage:" + message);
    }

    public java.util.List<java.util.Map<java.lang.String,java.lang.Object>> customSqlResult(String sql,Object ... objects){
        return null;
    }

    public void customSqlInsert(String sql,Object ... objects){

    }

    public void customSqlUpdate(String sql,Object ... objects){

    }

    public int getId() {
        return 0;
    }
}
