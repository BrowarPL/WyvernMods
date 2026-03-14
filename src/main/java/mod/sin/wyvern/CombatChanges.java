package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class CombatChanges {
    public static final Logger logger = Logger.getLogger(CombatChanges.class.getName());

    @SuppressWarnings("unused")
    public static float combatRatingAdditive(float combatRating, Creature cret, Creature opponent){
        float add = 0.0f;
        if(cret != null && cret.isPlayer() && opponent != null && !opponent.isPlayer()){
            if(WyvernMods.royalExecutionerBonus && cret.isRoyalExecutioner()){
                add += 2.0f;
            }
        }
        return add;
    }

    @SuppressWarnings("unused")
    public static float combatRatingMultiplicative(float combatRating, Creature cret, Creature opponent){
        float mult = 1.0f;
        if(cret != null){
            if(WyvernMods.petSoulDepthScaling && cret.isDominated() && cret.getDominator() instanceof Player) {
                Player owner = (Player) cret.getDominator();
                double depth = owner.getSoulDepth().getKnowledge();
                mult *= depth * 0.02d;
            } else if (WyvernMods.petSoulDepthScaling && cret.isDominated() && cret.getDominator() != null) {
                logger.info("Somehow a pet is dominated by a non-player? (" + cret.getDominator().getName() + ")");
            }
            if(WyvernMods.vehicleCombatRatingPenalty && QualityOfLife.getVehicleSafe(cret) != null){
                mult *= 0.75f;
            }
        }
        return mult;
    }

    public static int getLifeTransferAmountModifier(Wound wound, int initial){
        if (wound == null) {
            return initial;
        }
        byte type = wound.getType();
        if(type == Wound.TYPE_ACID || type == Wound.TYPE_BURN || type == Wound.TYPE_COLD){
            initial *= 0.5;
        }else if(type == Wound.TYPE_INTERNAL || type == Wound.TYPE_INFECTION || type == Wound.TYPE_POISON){
            initial *= 0.3;
        }
        return initial;
    }

    public static float getLifeTransferModifier(Creature creature, Creature defender){
        if(creature == null || defender == null){
            return 1.0f;
        }
        if(Servers.localServer.PVPSERVER && (defender.isDominated() || defender.isPlayer()) && creature.isPlayer()){
            return 0.5f;
        }
        return 1.0f;
    }

    @SuppressWarnings("unused")
    public static void doLifeTransfer(Creature creature, Creature defender, Item attWeapon, double defdamage, float armourMod){
        if (creature == null || attWeapon == null) {
            return;
        }
        float lifeTransfer = attWeapon.getSpellLifeTransferModifier() * getLifeTransferModifier(creature, defender);
        Wound[] wounds;
        if (lifeTransfer > 0.0f
                && defdamage * armourMod * lifeTransfer / (creature.isChampion() ? 1000.0 : 500.0) > 500.0
                && creature.getBody() != null
                && creature.getBody().getWounds() != null
                && (wounds = creature.getBody().getWounds().getWounds()).length > 0) {
            int amount = -(int)(defdamage * lifeTransfer / (creature.isChampion() ? 1000.0 : (creature.getCultist() != null && creature.getCultist().healsFaster() ? 250.0 : 500.0)));
            amount = getLifeTransferAmountModifier(wounds[0], amount);
            wounds[0].modifySeverity(amount);
        }
    }

    protected static ArrayList<Creature> uniques = new ArrayList<>();

    public static void pollUniqueCollection(){
        for(Creature creature : Creatures.getInstance().getCreatures()){
            if(creature.isUnique() && !uniques.contains(creature)){
                logger.info("Found unique not in unique list, adding now: " + creature.getName());
                uniques.add(creature);
            }
        }
        for(Iterator<Creature> it = uniques.iterator(); it.hasNext();){
            Creature current = it.next();
            if(current == null || current.isDead() || !current.isUnique()){
                if (current != null) {
                    logger.info("Unique was found dead (" + current.getName() + "). Removing from uniques list.");
                }
                it.remove();
            }
        }
    }

    public static void pollUniqueRegeneration(){
        if(!uniques.isEmpty()) {
            for (Creature creature : uniques) {
                if (creature == null || creature.getBody() == null || !creature.getBody().isWounded()) {
                    continue;
                }
                Wounds wounds = creature.getBody().getWounds();
                if (wounds == null || wounds.getWounds() == null || wounds.getWounds().length == 0) {
                    continue;
                }
                int toHeal = 75;
                Wound wound = wounds.getWounds()[Server.rand.nextInt(wounds.getWounds().length)];
                if (wound.getSeverity() > toHeal) {
                    wound.modifySeverity(-toHeal);
                    break;
                } else {
                    wound.heal();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static boolean canDoDamage(double damage, Creature attacker, Creature defender) {
        return damage > 1D;
    }

    public static void pollCreatureActionStacks(){
        for(Creature creature : Creatures.getInstance().getCreatures()){
            if(creature.isFighting()) {
                creature.getActions().poll(creature);
            }
        }
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<CombatChanges> thisClass = CombatChanges.class;
            String replace;

            CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass ctAttackAction = classPool.get("com.wurmonline.server.creatures.AttackAction");

            if (WyvernMods.enableCombatRatingAdjustments) {
                Util.setReason("Make custom combat rating changes.");
                replace = "combatRating += " + CombatChanges.class.getName() + ".combatRatingAdditive(combatRating, this.creature, $1);"
                        + "crmod *= " + CombatChanges.class.getName() + ".combatRatingMultiplicative(combatRating, this.creature, $1);"
                        + "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getFlankingModifier", replace);
            }

            if (WyvernMods.fixMagranonDamageStacking) {
                CtClass[] params2 = {
                        ctCreature,
                        ctItem,
                        ctCreature
                };
                String desc2 = Descriptor.ofMethod(CtClass.doubleType, params2);

                Util.setReason("Fix magranon damage bonus stacking.");
                replace = "if(mildStack){"
                        + "  $_ = $proceed($$) * 8 / 5;"
                        + "}else{"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc2, "getModifiedFloatEffect", replace);

                CtClass[] params3 = {
                        ctCreature,
                        ctAttackAction,
                        ctCreature
                };
                String desc3 = Descriptor.ofMethod(CtClass.doubleType, params3);

                Util.setReason("Fix magranon damage bonus stacking.");
                replace = "if(mildStack){"
                        + "  $_ = $proceed($$) * 8 / 5;"
                        + "}else{"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDescribed(thisClass, ctCombatHandler, "getDamage", desc3, "getModifiedFloatEffect", replace);
            }

            if (WyvernMods.adjustCombatRatingSpellPower) {
                Util.setReason("Nerf truehit/excel.");
                replace = "$_ = $proceed($$) * 0.5f;";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getBonusForSpellEffect", replace);
            }

            if (WyvernMods.disableLegendaryRegeneration) {
                Util.setReason("Disable natural regeneration on legendary creatures.");
                CtClass ctWound = classPool.get("com.wurmonline.server.bodys.Wound");
                replace = "if(!this.creature.isUnique()){"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctWound, "poll", "modifySeverity", replace);
                Util.setReason("Disable natural regeneration on legendary creatures.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkInfection", replace);
                Util.setReason("Disable natural regeneration on legendary creatures.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkPoison", replace);
            }

        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}