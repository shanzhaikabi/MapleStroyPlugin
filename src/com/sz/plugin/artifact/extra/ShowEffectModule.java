package com.sz.plugin.artifact.extra;

import com.sz.plugin.artifact.ExtraModule;
import com.sz.plugin.utils.MSUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ShowEffectModule implements ExtraModule {
    private int x;
    private int y;
    private boolean self;
    private boolean mob;
    private String path;
    private int duration;
    private boolean block;

    public ShowEffectModule(int x,int y,int duration,boolean self,boolean mob,boolean block,String path){
        this.x = x;
        this.y = y;
        this.self = self;
        this.mob = mob;
        this.path = path;
        this.duration = duration;
        this.block = block;
    }

    public boolean isSelf(){
        return this.self;
    }

    private List<Integer> getMobPos(String pos) {
        pos = pos.replace(",","");
        String[] p = pos.split(" ");
        return Arrays.stream(p).map(Integer::valueOf).collect(Collectors.toList());
    }

    public void showEffect(Object player,Object mob) throws Exception {
        int rx = x;
        int ry = y;
        if (this.mob){
            Object mob_ = MSUtils.doMethod(mob,"getMob");
            String mobPos = mob_.toString().split("\\(")[2];
            mobPos = mobPos.substring(0,mobPos.length()-1);
            List<Integer> pos = getMobPos(mobPos);
            rx = pos.get(0) + x;
            ry = pos.get(1) + y;
            MSUtils.doMethod(player,"showNpcEffectPlay",0,path,duration,rx,ry,true,0,true,0);
        }
        else if (block){
            Point pos = (Point) MSUtils.doMethod(player,"getPosition");
            rx = (int) (pos.getX() + x);
            ry = (int) (pos.getY() + y);
            MSUtils.doMethod(player,"showNpcEffectPlay",0,path,duration,rx,ry,true,0,true,0);
        }
        else{
            MSUtils.doMethod(player,"showNpcEffectPlay",0,path,duration,rx,ry,true,0,false,0);
        }
    }

}
