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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ArtifactManager
{
    int id;
    Object player;
    public long totalDamage = 0L;
    boolean isAuto = false;
    long time = -1L;
    int publicCd = 500;
    long lastArtifactAct = -1L;
    int actCd = 1000;
    int check = 0;
    long damageCap = 10000000000L;
    boolean baned = false;
    double jobRadio = 1.0D;

    List<BaseArtifact> artifacts = new ArrayList();

    public void addDamage(long dmg) {
        this.totalDamage += dmg;
    }

    public ArtifactManager(Object player) throws Exception {
        this.player = player;
        this.id = ((Integer)MSUtils.doMethod(player, "getId")).intValue();
        String p = (String)MSUtils.doMethod(player, "getQuestRecordEx", new Class[] { Integer.TYPE, String.class }, new Object[] { Integer.valueOf(888999), "artifactAuto" });
        this.isAuto = "1".equals(p);
        this.damageCap = getDamageCap();
        initArtifact();
    }

    private Object[] getObjectNeededForBuffArtifact(Object mob) throws Exception {
        Object mob_ = MSUtils.doMethod(mob, "getMob");
        Object[] objects = { Integer.valueOf(this.id), mob_ };
        return objects;
    }

    public void dealWithArtifactTrigger(BuffManager buffManager) {
        for (BaseBuff buff : buffManager.buffList.values())
            if ((buff.trigger instanceof ArtifactTrigger))
                ((ArtifactTrigger)buff.trigger).cur_hits += 1;
    }

    private long getDamageCap() throws Exception {
        long lm;
        try{
            Object item = MSUtils.doMethod(this.player, "getInventorySlot", new Class[] { Byte.TYPE, Short.TYPE }, new Object[] { -1, -11 });
            lm = ((Long)MSUtils.doMethod(item, "getLimitBreak")).longValue();
        }
        catch (Exception e){
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

    public void setAuto() {
        this.isAuto = (!this.isAuto);
    }

    public long dealWithArtifact(Object mob, long damage) throws Exception {
        if (this.baned) return 0L;
        MobManager.addMob(mob);
        int chance = new Random().nextInt(1000);
        if (chance < 10) {
            checkDamageCap();
        }
        double radio = damage * 1.0D / this.damageCap / 10.0D * this.jobRadio;
        long exDmg = 0L;
        BuffManager buffManager = MainManager.getInstance().getBuffManager(this.id);
        String prefix = buffManager.showBuffWhenArtifactActive();
        for (BaseArtifact artifact : this.artifacts) {
            artifact.onTrigger(radio);
            if ((System.currentTimeMillis() > this.time) && (artifact.isActive())) {
                exDmg = artifact.extraDamage(damage);
                if (artifact.stack.isMax()) {
                    showOverReady("", artifact);
                }
                else {
                    artifact.stack.setReady();
                    artifact.setEff(new Object[] { mob, Long.valueOf(exDmg) });
                    if (!this.isAuto) {
                        showReady("", artifact);
                    }
                }

                if (this.isAuto) {
                    dealWithArtifactAct();
                    this.time = (System.currentTimeMillis() + Math.max(this.actCd, this.publicCd));
                }
                else {
                    this.time = (System.currentTimeMillis() + this.publicCd);
                }
                dealWithArtifactTrigger(buffManager);
            }
        }
        return exDmg;
    }

    public long dealWithArtifactAct() throws Exception {
        if (this.baned) {
            MSUtils.showWarning(this.player, "在本次副本中无法使用神器！");
            return 0L;
        }
        if (System.currentTimeMillis() < this.lastArtifactAct) {
            MSUtils.showWarning(this.player, "神器触发器已过载，正在冷却中！");
            this.check = 0;
            return 0L;
        }
        checkDamageCap();
        BuffManager buffManager = MainManager.getInstance().getBuffManager(this.id);
        String prefix = buffManager.showBuffWhenArtifactActive();
        for (BaseArtifact artifact : this.artifacts)
            if (artifact.stack.isReady()) {
                artifact.stack.removeReady();
                Object[] objects = (Object[])(Object[])artifact.getEff();
                Object mob = objects[0];
                Object[] args = new Object[0];
                long ed = artifact.extraDamage(this.damageCap);
                ed += buffManager.dealWithArtifactDamage(ed, mob, artifact);
                if ((artifact.effect instanceof EffectModule)) {
                    args = getObjectNeededForBuffArtifact(mob);
                }
                artifact.onAct(args);
                showEffect(artifact, mob);
                if (ed > 0L) {
                    int hits = 5;
                    int delay = 0;
                    boolean block = false;
                    if (artifact.extra.containsKey(DamageHits.class)) {
                        DamageHits e = (DamageHits)artifact.extra.get(DamageHits.class);
                        hits = e.getHits();
                        delay = e.getDelay();
                        block = e.getBlock();
                    }
                    ed = ActManager.doHurt(this.player, mob, ed, hits, delay, block);
                }
                showDamage(prefix, artifact, ed);
                this.lastArtifactAct = (System.currentTimeMillis() + this.actCd);
                this.check = 0;
                return ed;
            }
        this.check += 1;
        if (this.check == 5) {
            this.check = 0;
            setAuto();
            MSUtils.showMessage(this.player, new StringBuilder().append("你已切换神器触发模式，当前模式：").append(this.isAuto ? "自动" : "手动").toString());
        }
        MSUtils.showWarning(this.player, "你的神器均未就绪！");
        return 0L;
    }

    public void initArtifact() {
        try {
            loadArtifactFromPlayer();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void loadArtifactFromPlayer() throws Exception {
        String sql = "select a.name as An, a.active as Aa, a.effect as Ae, a.extra as Ax from sz_artifact as a,sz_artifact_player as b,sz_artifact_equip as c where a.id = b.aid and a.lv = b.lv and b.id = c.eid and b.pid = c.pid and c.pid = ? order by c.slot, c.id";

        List<Map<String, Object>> res = MSUtils.customSqlResult(this.player, sql, new Object[] { MSUtils.doMethod(this.player, "getId") });
        for (Map<String, Object> r : res)
            this.artifacts.add(ArtifactFactory.buildArtifact(r.get("An"), r.get("Aa"), r.get("Ae"), r.get("Ax")));
    }

    public void showReady(String prefix, BaseArtifact artifact) throws Exception
    {
        if (!artifact.showDamage) return;
        String str = prefix;
        str = new StringBuilder().append(str).append(artifact.name).append(" 已就绪").toString();
        if (artifact.stack.getMax() != 1) {
            str = new StringBuilder().append(str).append("[").append(artifact.stack.getStack()).append("/").append(artifact.stack.getMax()).append("]").toString();
        }
        if (artifact.stack.showEffect())
            MSUtils.doMethod(this.player, "screenEffect", new Object[] { artifact.stack.getPath() });
        if (artifact.itemModule != null) {
            MSUtils.doMethod(this.player, "scriptProgressItemMessage", new Object[] { Integer.valueOf(artifact.itemModule.getItemId()), str });
        }
        else
            MSUtils.doMethod(this.player, "scriptProgressMessage", new Object[] { str });
    }

    public void showOverReady(String prefix, BaseArtifact artifact) throws Exception
    {
        if (!artifact.showDamage) return;
        String str = prefix;
        str = new StringBuilder().append(str).append(artifact.name).append(" 已达到最高就绪等级，无法继续充能").toString();
        if (artifact.itemModule != null) {
            MSUtils.doMethod(this.player, "scriptProgressItemMessage", new Object[] { Integer.valueOf(artifact.itemModule.getItemId()), str });
        }
        else
            MSUtils.doMethod(this.player, "scriptProgressMessage", new Object[] { str });
    }

    public void showDamage(String prefix, BaseArtifact artifact, long dmg) throws Exception
    {
        if (!artifact.showDamage) return;
        String str = prefix;
        if (artifact.damageModule != null) {
            str = new StringBuilder().append(str).append(artifact.damageModule.showDamage(dmg)).toString();
        }
        else {
            str = new StringBuilder().append(str).append(artifact.name).append(" 额外造成伤害:").append(dmg).toString();
        }
        if (artifact.itemModule != null) {
            MSUtils.doMethod(this.player, "scriptProgressItemMessage", new Object[] { Integer.valueOf(artifact.itemModule.getItemId()), str });
        }
        else
            MSUtils.doMethod(this.player, "scriptProgressMessage", new Object[] { str });
    }

    public void showEffect(BaseArtifact artifact, Object mob) throws Exception
    {
        if (artifact.effectModule.size() > 0)
            for (ShowEffectModule module : artifact.effectModule) {
                if (module.isSelf()) {
                    MSUtils.doMethod(this.player, "setInGameDirectionMode", new Object[] { true, false, false, true });
                    module.showEffect(this.player, mob);
                    MSUtils.doMethod(this.player, "setInGameDirectionMode", new Object[] { false, true, false, false });
                }
                else {
                    for (ArtifactManager manager : MainManager.getInstance().getArtifactManagers().values())
                        if ((manager.isAuto) || (manager.id == this.id)) {
                            MSUtils.doMethod(manager.player, "setInGameDirectionMode", new Object[] { true, false, false, true });
                            module.showEffect(manager.player, mob);
                            MSUtils.doMethod(manager.player, "setInGameDirectionMode", new Object[] { false, true, false, false });
                        }
                }
            }
    }
}