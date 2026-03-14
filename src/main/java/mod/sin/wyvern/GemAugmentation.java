package mod.sin.wyvern;

import com.wurmonline.server.items.Item;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class GemAugmentation {
    public static final Logger logger = Logger.getLogger(GemAugmentation.class.getName());

    /**
     * WU 1.9-safe gem augmentation behavior:
     * Instead of depending on fragile local variables from improve/polish/temper,
     * intercept the requested quality level directly and boost only the gained amount
     * using the item's material improvement bonus.
     */
    @SuppressWarnings("unused")
    public static boolean applyGemAugmentation(Item target, float requestedQuality){
        if (target == null) {
            return false;
        }

        float currentQuality = target.getQualityLevel();
        float attemptedGain = requestedQuality - currentQuality;

        if (attemptedGain <= 0f) {
            return target.setQualityLevel(Math.min(999.9f, requestedQuality));
        }

        float materialBonus = target.getMaterialImpBonus();
        if (materialBonus <= 0f) {
            materialBonus = 1.0f;
        }

        float boostedQuality = currentQuality + (attemptedGain * materialBonus);
        boostedQuality = Math.min(999.9f, boostedQuality);

        return target.setQualityLevel(boostedQuality);
    }

    public static void preInit(){
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            Class<GemAugmentation> thisClass = GemAugmentation.class;
            String replace;

            Util.setReason("Disable Gem Augmentation skill from converting.");
            CtClass ctMethodsReligion = classPool.get("com.wurmonline.server.behaviours.MethodsReligion");
            replace = "$_ = $proceed($1, $2, true, $4);";
            Util.instrumentDeclared(thisClass, ctMethodsReligion, "listen", "skillCheck", replace);

            Util.setReason("Primary Gem Augmentation Hook.");
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            replace = "$_ = " + GemAugmentation.class.getName() + ".applyGemAugmentation($0, $1);";
            Util.instrumentDeclared(thisClass, ctMethodsItems, "improveItem", "setQualityLevel", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "polishItem", "setQualityLevel", replace);
            Util.instrumentDeclared(thisClass, ctMethodsItems, "temper", "setQualityLevel", replace);

            Util.setReason("Allow Gem Augmentation to exceed normal quality caps.");
            CtClass ctDbItem = classPool.get("com.wurmonline.server.items.DbItem");
            replace = "$_ = $proceed(9999.9f);";
            Util.instrumentDeclared(thisClass, ctDbItem, "setQualityLevel", "min", replace);

            logger.info("GemAugmentation hooks initialized using WU 1.9-safe quality interception.");
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}