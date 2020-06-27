package com.sz.plugin.artifact.effect;

import com.sz.plugin.MainManager;
import com.sz.plugin.artifact.BuffFactory;
import com.sz.plugin.artifact.EffectModule;
import com.sz.plugin.artifact.RawBuff;
import com.sz.plugin.manager.BuffManager;

public class BuffArtifact implements EffectModule {

    private RawBuff buff;

    public BuffArtifact(String _buff){
        this.buff = BuffFactory.buildRawBuff(_buff);
    }

    public void dealWithBuff(int id,Object mob){
        if (buff.buff.mob != null) buff.buff.mob = mob;
        if (!buff.self){
            for (BuffManager buffManager : MainManager.getInstance().getBuffManagers()) {
                try {
                    buffManager.addBuff(buff.buff);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            try {
                MainManager.getInstance().getBuffManager(id).addBuff(buff.buff);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public long extraDamage(long damage) {
        return 0;
    }

    @Override
    public void onAct(Object[] args) {
        dealWithBuff((int) args[0],args[1]);
    }
}
