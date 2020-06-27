package com.sz.plugin.artifact.active;

import com.sz.plugin.artifact.ActiveModule;

/**
 * 攻击次数型神器，拥有以下属性:
 * time:触发神器所需要的攻击次数.
 */

public class AttackTimeArtifact implements ActiveModule {

    public int count;
    public double cur_count = 0;

    public AttackTimeArtifact(String time) {
        this(Integer.valueOf(time));
    }

    private AttackTimeArtifact(int chance) {
        this.count = chance;
    }

    @Override
    public boolean isActive() {
        if (this.cur_count > count){
            this.cur_count -= count;//到达次数后减去需要的次数
            this.cur_count /= 2;//再将当前次数减半
            return true;
        }
        return false;
    }

    @Override
    public void onTrigger(double radio) {
        this.cur_count += radio;
    }
}
