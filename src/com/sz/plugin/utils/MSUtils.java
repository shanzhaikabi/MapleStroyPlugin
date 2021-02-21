package com.sz.plugin.utils;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class MSUtils {
    public static Object doMethod(Object obj, String name) throws Exception {
        Class c = obj.getClass();
        try{
            Method m = c.getDeclaredMethod(name);
            m.setAccessible(true);
            if (m.getReturnType().equals(Void.TYPE)){
                m.invoke(obj);
                return null;
            }
            return m.invoke(obj);
        }
        catch (Exception e){
            Method m = c.getMethod(name);
            if (m.getReturnType().equals(Void.TYPE)){
                m.invoke(obj);
                return null;
            }
            return m.invoke(obj);
        }
    }

    public static Object doMethod(Object obj, String name, Object... args) throws Exception{
        Class c = obj.getClass();
        try{
            Method[] ms = c.getDeclaredMethods();
            Method m = null;
            for (Method m1:ms) {
                if (m1.getName().equals(name)){
                    m = m1;
                    break;
                }
            }
            m.setAccessible(true);
            if (m.getReturnType().equals(Void.TYPE)){
                m.invoke(obj,args);
                return null;
            }
            return m.invoke(obj,args);
        }catch (Exception e){
            try {
                Method[] ms = c.getMethods();
                Method m = null;
                for (Method m1 : ms) {
                    if (m1.getName().equals(name)) {
                        m = m1;
                        break;
                    }
                }
                m.setAccessible(true);
                if (m.getReturnType().equals(Void.TYPE)) {
                    m.invoke(obj, args);
                    return null;
                }
                return m.invoke(obj, args);
            }catch (Exception e1){
                e1.printStackTrace();
                JsUtils.PrintMessage(e1);
                throw e1;
            }
        }
    }

    public static Object doMethod(Object obj, String name,Class[] cls, Object... args) throws Exception {
        Class c = obj.getClass();
        try{
            Method m = c.getDeclaredMethod(name,cls);
            m.setAccessible(true);
            if (m.getReturnType().equals(Void.TYPE)){
                m.invoke(obj,args);
                return null;
            }
            return m.invoke(obj,args);
        }catch (Exception e){
            Method m = c.getMethod(name,cls);
            m.setAccessible(true);
            if (m.getReturnType().equals(Void.TYPE)){
                m.invoke(obj,args);
                return null;
            }
            return m.invoke(obj,args);
        }
    }

    public static void dropMessage(Object player,int i, String str) throws Exception {
        doMethod(player,"dropMessage",new Class[]{int.class,String.class},i,str);
    }

    public static void showMessage(Object player, String str) throws Exception {
        doMethod(player,"dropMessage",new Class[]{int.class,String.class},4,str);
        doMethod(player,"setStaticScreenMessage",0,str,true);
    }

    public static void showWarning(Object player, String str) throws Exception {
        doMethod(player,"showSystemMessage",str);
        doMethod(player,"setStaticScreenMessage",0,str,true);
    }


    public static void showSkillEffect(Object player, int itemid, String str) throws Exception {
        doMethod(player,"scriptProgressItemMessage",new Class[]{int.class,String.class},itemid,str);
    }

    public static void customSqlInsert(Object player,String sql,Object... args) throws Exception{
        doMethod(player, "customSqlInsert",sql, args);
    }

    public static void customSqlUpdate(Object player,String sql,Object... args) throws Exception{
        doMethod(player, "customSqlUpdate",sql, args);
    }

    public static List<Map<String,Object>> customSqlResult(Object player,String sql,Object... args) throws Exception{
        return (List<Map<String, Object>>) doMethod(player, "customSqlResult",sql, args);
    }

}
