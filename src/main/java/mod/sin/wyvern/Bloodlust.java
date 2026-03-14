package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Bloodlust {
    public static final Logger logger = Logger.getLogger(Bloodlust.class.getName());

    protected static final HashMap<Long, Float> lusts = new HashMap<>();
    protected static final HashMap<Long, Long> lastLusted = new HashMap<>();

    @SuppressWarnings("unused")
    public static float lustUnique(Creature creature){
        if (creature == null) {
            return 1.0f;
        }

        long wurmid = creature.getWurmId();
        float currentLust = lusts.getOrDefault(wurmid, 0.0f);

        if(currentLust >= 1.0f){
            Server.getInstance().broadCastAction(creature.getName() + " becomes enraged!", creature, 50);
        }else if(currentLust >= 0.49f){
            Server.getInstance().broadCastAction(creature.getName() + " is becoming enraged!", creature, 50);
        }else{
            Server.getInstance().broadCastAction(creature.getName() + " is beginning to see red!", creature, 50);
        }

        currentLust += 0.01f;
        lusts.put(wurmid, currentLust);
        lastLusted.put(wurmid, System.currentTimeMillis());
        return 1.0f + currentLust;
    }

    @SuppressWarnings("unused")
    public static float getLustMult(Creature creature){
        if (creature == null) {
            return 1.0f;
        }

        long wurmid = creature.getWurmId();
        return 1.0f + lusts.getOrDefault(wurmid, 0.0f);
    }

    @SuppressWarnings("unused")
    public static void pollLusts(){
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Long, Long>> iterator = lastLusted.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            long wurmid = entry.getKey();
            long lastLustTime = entry.getValue();

            if(now >= lastLustTime + TimeConstants.MINUTE_MILLIS * 10){
                logger.info("Bloodlust for " + wurmid + " expired. Removing from lists.");
                Creature creature = Creatures.getInstance().getCreatureOrNull(wurmid);
                if(creature != null && !creature.isDead()){
                    Server.getInstance().broadCastAction(creature.getName() + " calms down and is no longer enraged.", creature, 50);
                }
                iterator.remove();
                lusts.remove(wurmid);
            }
        }
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<Bloodlust> thisClass = Bloodlust.class;
            String replace;

            Util.setReason("Hook for bloodlust system.");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctString = classPool.get("java.lang.String");
            CtClass ctBattle = classPool.get("com.wurmonline.server.combat.Battle");
            CtClass ctCombatEngine = classPool.get("com.wurmonline.server.combat.CombatEngine");

            CtClass[] params = {
                    ctCreature,
                    ctCreature,
                    CtClass.byteType,
                    CtClass.intType,
                    CtClass.doubleType,
                    CtClass.floatType,
                    ctString,
                    ctBattle,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.booleanType
            };
            String desc = Descriptor.ofMethod(CtClass.booleanType, params);

            replace =
                    "if($2 != null && $2.isDominated() && $1 != null && ($1.isUnique() || " + RareSpawns.class.getName() + ".isRareCreature($1))){" +
                            "  " + Bloodlust.class.getName() + ".lustUnique($1);" +
                            "}" +
                            "if($1 != null && ($1.isUnique() || " + RareSpawns.class.getName() + ".isRareCreature($1))){" +
                            "  $5 = $5 * " + Bloodlust.class.getName() + ".getLustMult($1);" +
                            "}";

            Util.insertBeforeDescribed(thisClass, ctCombatEngine, "addWound", desc, replace);
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}