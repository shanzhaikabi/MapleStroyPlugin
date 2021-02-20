package com.sz.plugin.artifact.active;

import com.sz.plugin.artifact.ActiveModule;

import java.util.Date;
import java.util.Random;

/**
 * 随机触发型神器，拥有以下属性:
 * chance:触发几率，数值范围0~1000，代表触发几率(1/1000)
 */
public class ChanceArtifact implements ActiveModule {

    public int chance;
    public double failedTime;
    public int critical;
    public int selfCd;//内置cd 单位为ms
    public long time;//时间戳 用于计算cd
    public double radio;

    public ChanceArtifact(String chance) {
        this(Integer.valueOf(chance));
    }

    private ChanceArtifact(int chance) {
        this.chance = chance;
        this.critical = Math.round(2000 / chance);//连续失败的补偿次数
        this.selfCd = Math.round(200000 / chance);//按几率倒数获得默认内置cd
        if (this.selfCd > 10000) this.selfCd = 10000;
        this.time = -1;
    }

    @Override
    public boolean isActive(){
        if (System.currentTimeMillis() < time){
            failedTime++;
            return false;//仍在cd内
        }
        int c = new Random().nextInt(1000);
        if (failedTime > critical || c < chance){
            failedTime = 0;
            time = System.currentTimeMillis() + selfCd;
            return true;
        }
        failedTime += radio;
        return false;
    }

    @Override
    public void onTrigger(double radio) {
        this.radio = radio;
    }
}
