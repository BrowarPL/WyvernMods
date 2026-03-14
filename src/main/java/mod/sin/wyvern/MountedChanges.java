package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.shared.constants.BodyPartConstants;
import com.wurmonline.shared.constants.Enchants;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MountedChanges {
    public static final Logger logger = Logger.getLogger(MountedChanges.class.getName());

    private static void addShoeIfPresent(Creature creature, byte bodyPart, ArrayList<Item> gear) {
        try {
            Item item = creature.getEquippedItem(bodyPart);
            if (item != null) {
                item.setDamage(item.getDamage() + (item.getDamageModifier() * 0.002f));
                gear.add(item);
            }
        } catch (NoSpaceException ignored) {
        }
    }

    public static float newCalcHorseShoeBonus(Creature creature){
        float factor = 1.0f;
        ArrayList<Item> gear = new ArrayList<>();

        addShoeIfPresent(creature, BodyPartConstants.LEFT_FOOT, gear);
        addShoeIfPresent(creature, BodyPartConstants.RIGHT_FOOT, gear);
        addShoeIfPresent(creature, BodyPartConstants.LEFT_HAND, gear);
        addShoeIfPresent(creature, BodyPartConstants.RIGHT_HAND, gear);

        for(Item shoe : gear){
            factor += Math.max(10f, shoe.getCurrentQualityLevel()) / 2000f;
            factor += shoe.getSpellSpeedBonus() / 2000f;
            factor += shoe.getRarity() * 0.03f;
        }
        return factor;
    }

    public static float newMountSpeedMultiplier(Creature creature, boolean mounting){
        if (creature == null || creature.getStatus() == null) {
            return 1.0f;
        }

        float hunger = creature.getStatus().getHunger() / 65535f;
        float damage = creature.getStatus().damage / 65535f;
        float factor = ((((1f - damage * damage) * (1f - damage) + (1f - 2f * damage) * damage) * (1f - damage) + (1f - damage) * damage) * (1f - 0.4f * hunger * hunger));

        try {
            float traitMove = ReflectionUtil.callPrivateMethod(creature, ReflectionUtil.getMethod(creature.getClass(), "getTraitMovePercent"), mounting);
            factor += traitMove;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            WyvernMods.logger.log(Level.WARNING, "", e);
        }

        if(creature.isHorse() || creature.isUnicorn()) {
            factor *= newCalcHorseShoeBonus(creature);
        }

        if (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) > 0.0f) {
            factor *= 1f - (0.3f * (creature.getBonusForSpellEffect(Enchants.CRET_OAKSHELL) / 100.0f));
        }

        if(creature.isRidden()){
            try {
                Item saddle = creature.getEquippedItem(BodyPartConstants.TORSO);
                if(saddle != null) {
                    saddle.setDamage(saddle.getDamage() + (saddle.getDamageModifier() * 0.001f));
                    float saddleFactor = 1.0f;
                    saddleFactor += Math.max(10f, saddle.getCurrentQualityLevel()) / 2000f;
                    saddleFactor += saddle.getSpellSpeedBonus() / 2000f;
                    saddleFactor += saddle.getRarity() * 0.03f;
                    factor *= saddleFactor;
                }
            } catch (NoSpaceException ignored) {
            }
            if (creature.getMovementScheme() != null) {
                factor *= creature.getMovementScheme().getSpeedModifier();
            }
        }
        return factor;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<MountedChanges> thisClass = MountedChanges.class;
            String replace;

            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

            if (WyvernMods.newMountSpeedScaling) {
                Util.setReason("New mount speed scaling.");
                replace = "{ return " + MountedChanges.class.getName() + ".newMountSpeedMultiplier(this, $1); }";
                Util.setBodyDeclared(thisClass, ctCreature, "getMountSpeedPercent", replace);
            }

            if (WyvernMods.updateMountSpeedOnDamage) {
                Util.setReason("Force mount speed change check on damage.");
                replace = "forceMountSpeedChange();";
                Util.insertBeforeDeclared(thisClass, ctCreature, "setWounded", replace);
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}