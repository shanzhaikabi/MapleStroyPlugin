package com.cms.game.script.binding;

public class ScriptPlayer {
    public void	dropMessage(int type, String message){
        System.out.println("dropMessage:" + message);
    }

    public void scriptProgressMessage(String message){
        System.out.println("scriptProgressMessage:" + message);
    }

    public void runScript(String script){}

    public String getName(){
        return "name";
    }

    public void dropAlertNotice(String msg){

    }

    public java.util.List<java.util.Map<java.lang.String,java.lang.Object>> customSqlResult(String sql,Object ... args){
        return null;
    }

    public void customSqlInsert(String sql,Object ... args){

    }

    public void customSqlUpdate(String sql,Object ... args){

    }

    public int getId() {
        return 0;
    }
}
