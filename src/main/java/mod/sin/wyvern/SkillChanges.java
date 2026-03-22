package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.CannotCompileException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkillChanges {
    public static final Logger logger = Logger.getLogger(SkillChanges.class.getName());

    @SuppressWarnings("unused")
    public static double newDoSkillGainNew(Skill skill, double check, double power, double learnMod, float times, double skillDivider) {
        double bonus = 1.0;
        double diff = Math.abs(check - skill.getKnowledge());
        short sType = SkillSystem.getTypeFor(skill.getNumber());
        boolean awardBonus = sType != 1 && sType != 0;
        if (diff <= 15.0 && awardBonus) {
            bonus = 1.0 + (0.1 * (diff / 15.0));
        }

        try {
            Skills parent = ReflectionUtil.getPrivateField(skill, ReflectionUtil.getField(skill.getClass(), "parent"));
            double advanceMultiplicator = (100.0 - skill.getKnowledge()) /
                    (skill.getDifficulty(parent.priest) * skill.getKnowledge() * skill.getKnowledge()) *
                    learnMod * bonus;

            double negativeDecayRate = WyvernMods.hybridNegativeDecayRate;
            double positiveDecayRate = WyvernMods.hybridPositiveDecayRate;
            double valueAtZero = WyvernMods.hybridValueAtZero;
            double valueAtOneHundred = WyvernMods.hybridValueAtOneHundred;

            double mult = valueAtOneHundred * Math.pow(
                    valueAtZero / valueAtOneHundred,
                    (2 - Math.pow(100 / (100 + Math.max(-99, power)), negativeDecayRate)) *
                            Math.pow((100 - power) * 0.01, positiveDecayRate)
            );

            if(mult < 0.5 && skill.getKnowledge() < 20){
                advanceMultiplicator *= 0.5 + (Server.rand.nextDouble() * 0.5);
            }else if(skill.getNumber() == SkillList.MEDITATING || skill.getNumber() == SkillList.LOCKPICKING){
                advanceMultiplicator *= Math.max(mult, 0.8d);
            }else if(mult > 0.0001) {
                advanceMultiplicator *= mult;
            }else{
                advanceMultiplicator = 0;
            }

            return advanceMultiplicator;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "", e);
        }
        return 0;
    }

    private static SkillTemplate getSkillTemplateSafe(int id) {
        SkillTemplate skillTemplate = SkillSystem.templates.get(id);
        if (skillTemplate == null) {
            logger.warning("Could not find skill template for ID " + id + ".");
        }
        return skillTemplate;
    }

    public static void setSkillName(int id, String newName){
        SkillTemplate skillTemplate = getSkillTemplateSafe(id);
        if (skillTemplate == null) {
            return;
        }
        try {
            ReflectionUtil.setPrivateField(skillTemplate, ReflectionUtil.getField(skillTemplate.getClass(), "name"), newName);
            SkillSystem.skillNames.put(skillTemplate.getNumber(), newName);
            SkillSystem.namesToSkill.put(newName, skillTemplate.getNumber());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "Failed to rename skill with ID " + id + "!", e);
        }
    }

    public static void setSkillDifficulty(int id, float difficulty){
        SkillTemplate skillTemplate = getSkillTemplateSafe(id);
        if (skillTemplate == null) {
            return;
        }
        skillTemplate.setDifficulty(difficulty);
    }

    public static void setSkillTickTime(int id, long tickTime){
        SkillTemplate skillTemplate = getSkillTemplateSafe(id);
        if (skillTemplate == null) {
            return;
        }
        try {
            ReflectionUtil.setPrivateField(skillTemplate, ReflectionUtil.getField(skillTemplate.getClass(), "tickTime"), tickTime);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.log(Level.WARNING, "Failed to set tickTime for skill with ID " + id + "!", e);
        }
    }
    
    // Fallback manual title assignment to ensure level 70 titles are always awarded
    @SuppressWarnings("unused")
    public static void manualTitleCheck(Skill skill, double oldknowledge, double newknowledge) {
        try {
            if (oldknowledge < 70.0 && newknowledge >= 70.0) {
                Skills parent = ReflectionUtil.getPrivateField(skill, ReflectionUtil.getField(skill.getClass(), "parent"));
                if (parent != null) {
                    long pid = parent.getId();
                    if (WurmId.getType(pid) == 0) { // is player
                        Titles.Title title = Titles.Title.getTitle(skill.getNumber(), Titles.TitleType.MINOR);
                        if (title != null) {
                            Player p = Players.getInstance().getPlayerOrNull(pid);
                            if (p != null) {
                                p.addTitle(title);
                                p.achievement(564); // trigger minor achievement manually
                                if (skill.getNumber() == 10066) {
                                    p.maybeTriggerAchievement(633, true);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in manual title check for skill: " + skill.getName(), e);
        }
    }

    public static void onServerStarted(){
        for (Map.Entry<Integer, String> entry : WyvernMods.skillName.entrySet()){
            setSkillName(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Float> entry : WyvernMods.skillDifficulty.entrySet()){
            setSkillDifficulty(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Long> entry : WyvernMods.skillTickTime.entrySet()){
            setSkillTickTime(entry.getKey(), entry.getValue());
        }

        if (WyvernMods.changePreachingLocation) {
            SkillTemplate preaching = getSkillTemplateSafe(SkillList.PREACHING);
            if (preaching == null) {
                return;
            }
            int[] deps3 = {SkillList.MASONRY};
            try {
                ReflectionUtil.setPrivateField(preaching, ReflectionUtil.getField(preaching.getClass(), "dependencies"), deps3);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.log(Level.WARNING, "Failed to update preaching dependencies!", e);
            }
        }
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<SkillChanges> thisClass = SkillChanges.class;
            String replace;

            if (WyvernMods.enableHybridSkillGain) {
                Util.setReason("Add hybrid skill gain system hook.");
                CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
                replace = "{"
                        + "  double advanceMultiplicator = " + SkillChanges.class.getName() + ".newDoSkillGainNew($0, $1, $2, $3, $4, $5);"
                        + "  $0.alterSkill(advanceMultiplicator, false, $4, true, $5);"
                        + "}";
                Util.setBodyDeclared(thisClass, ctSkill, "doSkillGainNew", replace);
            }
            
            // Add safety net for level 70 titles
            CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
            ctSkill.getDeclaredMethod("checkTitleChange").insertAfter(SkillChanges.class.getName() + ".manualTitleCheck(this, $1, $2);");

        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}