package com.sz.plugin.manager;

import com.sz.plugin.MainManager;
import com.sz.plugin.artifact.ArtifactFactory;
import com.sz.plugin.artifact.BaseArtifact;
import com.sz.plugin.artifact.EffectModule;
import com.sz.plugin.artifact.buff.BaseBuff;
import com.sz.plugin.artifact.buff.trigger.ArtifactTrigger;
import com.sz.plugin.artifact.extra.DamageHits;
import com.sz.plugin.artifact.extra.ShowEffectModule;
import com.sz.plugin.utils.MSUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ArtifactManager {
    int id;
    Object player;
    public long totalDamage = 0;
    boolean openEffect = false;
    long time = -1;
    int publicCd = 500;//内置0.5s公共cd
    long lastArtifactAct = -1;
    int actCd = 1000;//内置2s神器触发cd
    long damageCap = 10000000000L;
    boolean baned = false;
    double jobRadio = 1.0D;

    List<BaseArtifact> artifacts = new ArrayList();

    public void addDamage(long dmg){
        this.totalDamage += dmg;
    }

    public ArtifactManager(Object player) throws Exception {
        this.player = player;
        this.id = (int)MSUtils.doMethod(player,"getId");
        String p = (String) MSUtils.doMethod(player,"getQuestRecordEx", new Class[]{int.class, String.class},888999, "artifactEffect");
        this.openEffect = "1".equals(p);
        this.damageCap = getDamageCap();
        initArtifact();
    }

    private Object[] getObjectNeededForBuffArtifact(Object mob) throws Exception {
        Object mob_ = MSUtils.doMethod(mob,"getMob");
        Object[] objects = new Object[]{id,mob_};
        return objects;
    }

    public void dealWithArtifactTrigger(BuffManager buffManager) {
        for(BaseBuff buff : buffManager.buffList.values()){
            if (buff.trigger instanceof ArtifactTrigger){
                ((ArtifactTrigger) buff.trigger).cur_hits++;
            }
        }
    }

    private long getDamageCap() throws Exception {
        long lm;
        try {
            Object item = MSUtils.doMethod(this.player, "getInventorySlot", (byte)-1, (short)-11);
            lm = ((Long)MSUtils.doMethod(item, "getLimitBreak")).longValue();
        }
        catch (Exception e)
        {
            MSUtils.showWarning(this.player, "请不要在副本中更换装备！在此次副本中,你的神器将失效.");
            this.baned = true;
            lm = 200000000000L;
        }
        return lm;
    }

    private void removePlayer() throws Exception {
        MSUtils.doMethod(this.player, "setEvent", null);
    }

    private void checkDamageCap() throws Exception {
        long lm = getDamageCap();
        if (lm != this.damageCap) {
            MSUtils.showWarning(this.player, "请不要在副本中更换装备！在此次副本中,你的神器将失效.");
            this.baned = true;
        }
    }

    public void setOpenEffect() {
        this.openEffect = !openEffect;
    }

    public long dealWithArtifact(Object mob,long damage) throws Exception {
        if (this.baned) return 0L;
        MobManager.addMob(mob);
        int chance = new Random().nextInt(1000);
        if (chance < 10) {
            checkDamageCap();
        }
        double radio = damage * 1.0D / this.damageCap / 10.0D * this.jobRadio;
        long exDmg = 0;
        BuffManager buffManager = MainManager.getInstance().getBuffManager(id);
        String prefix = buffManager.showBuffWhenArtifactActive();
        for (BaseArtifact artifact:artifacts) {
            artifact.onTrigger(radio);
            if (System.currentTimeMillis() > time && artifact.isActive()){
                exDmg = artifact.extraDamage(damage);
                if (artifact.stack.isMax()){
                    showOverReady("",artifact);
                }
                else{
                    artifact.stack.setReady();
                    artifact.setEff(new Object[] {mob,exDmg});//normal type,wow~
                }
                /*showEffect(artifact,mob);
                showDamage(prefix,artifact,ed);*///move to act
                dealWithArtifactAct();
                time = System.currentTimeMillis() + Math.max(actCd,publicCd);
                dealWithArtifactTrigger(buffManager);
            }
        }
        return exDmg;
    }

    public long dealWithArtifactAct() throws Exception {
        if (this.baned) {
            MSUtils.showWarning(this.player, "在本次副本中无法使用神器！");
            return 0;
        }
        if (System.currentTimeMillis() < lastArtifactAct){
            MSUtils.showWarning(player,"神器触发器已过载，正在冷却中！");
            return 0;
        }
        checkDamageCap();
        BuffManager buffManager = MainManager.getInstance().getBuffManager(id);
        String prefix = buffManager.showBuffWhenArtifactActive();
        for (BaseArtifact artifact:artifacts) {
            if (!artifact.stack.isReady()) continue;
            artifact.stack.removeReady();
            Object[] objects = (Object[]) artifact.getEff();
            Object mob = objects[0];
            Object[] args = new Object[0];
            long ed = artifact.extraDamage(this.damageCap);
            ed += buffManager.dealWithArtifactDamage(ed,mob,artifact);
            if (artifact.effect instanceof EffectModule){
                args = getObjectNeededForBuffArtifact(mob);
            }
            artifact.onAct(args);
            showEffect(artifact,mob);
            if (ed > 0){
                int hits = 5;
                int delay = 0;
                boolean block = false;
                if (artifact.extra.containsKey(DamageHits.class)){
                    DamageHits e = (DamageHits) artifact.extra.get(DamageHits.class);
                    hits = e.getHits();
                    delay = e.getDelay();
                    block = e.getBlock();
                }
                ed = ActManager.doHurt(player,mob,ed,hits,delay,block);
            }
            showDamage(prefix,artifact,ed);
            lastArtifactAct = System.currentTimeMillis() + actCd;
            return ed;
        }
        MSUtils.showWarning(player,"你的神器均未就绪！");
        return 0;
    }

    public void initArtifact(){
        try{
            loadArtifactFromPlayer();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public void loadArtifactFromPlayer() throws Exception {
        String sql = "select a.name as An, a.active as Aa, a.effect as Ae, a.extra as Ax " +
                "from sz_artifact as a,sz_artifact_player as b,sz_artifact_equip as c " +
                "where a.id = b.aid and a.lv = b.lv and b.id = c.eid and b.pid = c.pid and c.pid = ? " +
                "order by c.slot, c.id";
        List<Map<String, Object>> res = MSUtils.customSqlResult(player, sql, MSUtils.doMethod(player, "getId"));
        for (Map<String, Object> r:res) {
            artifacts.add(ArtifactFactory.buildArtifact(r.get("An"),r.get("Aa"),r.get("Ae"),r.get("Ax")));
        }
    }

    public void showReady(String prefix, BaseArtifact artifact) throws Exception {
        if (!artifact.showDamage) return;
        String str = prefix;
        str += artifact.name + " 已就绪";
        if (artifact.stack.getMax() != 1){
            str += "[" + artifact.stack.getStack() + "/" + artifact.stack.getMax() + "]";
        }
        if (artifact.stack.showEffect())
            MSUtils.doMethod(player,"screenEffect",artifact.stack.getPath());
        if (artifact.itemModule != null){
            MSUtils.doMethod(player,"scriptProgressItemMessage",artifact.itemModule.getItemId(),str);
        }
        else{
            MSUtils.doMethod(player,"scriptProgressMessage",str);
        }
    }

    public void showOverReady(String prefix, BaseArtifact artifact) throws Exception {
        if (!artifact.showDamage) return;
        String str = prefix;
        str += artifact.name + " 已达到最高就绪等级，无法继续充能";
        if (artifact.itemModule != null){
            MSUtils.doMethod(player,"scriptProgressItemMessage",artifact.itemModule.getItemId(),str);
        }
        else{
            MSUtils.doMethod(player,"scriptProgressMessage",str);
        }
    }

    public void showDamage(String prefix, BaseArtifact artifact,long dmg) throws Exception {
        if (!artifact.showDamage) return;
        String str = prefix;
        if (artifact.damageModule != null){
            str += artifact.damageModule.showDamage(dmg);
        }
        else{
            str += artifact.name + " 额外造成伤害:" + dmg;
        }
        if (artifact.itemModule != null){
            MSUtils.doMethod(player,"scriptProgressItemMessage",artifact.itemModule.getItemId(),str);
        }
        else{
            MSUtils.doMethod(player,"scriptProgressMessage",str);
        }
    }

    public void showEffect(BaseArtifact artifact,Object mob) throws Exception {
        if (artifact.effectModule.size() > 0){
            for(ShowEffectModule module : artifact.effectModule){
                if (openEffect && module.isSelf()){
                    MSUtils.doMethod(player,"setInGameDirectionMode",true,false,false,true);
                    module.showEffect(player,mob);
                    MSUtils.doMethod(player,"setInGameDirectionMode",false,true,false,false);
                }
                else{
                    for (ArtifactManager manager : MainManager.getInstance().getArtifactManagers().values()) {
                        if (!manager.openEffect && manager.id != id) continue;
                        MSUtils.doMethod(manager.player,"setInGameDirectionMode",true,false,false,true);
                        module.showEffect(manager.player,mob);
                        MSUtils.doMethod(manager.player,"setInGameDirectionMode",false,true,false,false);
                    }
                }
            }
        }
    }
}
