package mod.sin.wyvern;

import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import mod.sin.wyvern.bounty.LootBounty;
import mod.sin.wyvern.bounty.PlayerBounty;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bounty {
	public static final Logger logger = Logger.getLogger(Bounty.class.getName());
	public static final HashMap<String, Integer> reward = new HashMap<>();

	@SuppressWarnings("unused")
	public static long lastAttacked(Map<Long, Long> attackers, long playerId){
		if (attackers == null || !attackers.containsKey(playerId)) {
			return Long.MAX_VALUE;
		}
		return System.currentTimeMillis() - attackers.get(playerId);
	}

	public static boolean isCombatant(Map<Long, Long> attackers, long playerId){
		if (attackers == null || !attackers.containsKey(playerId)) {
			return false;
		}
		long now = System.currentTimeMillis();
		long delta = now - attackers.get(playerId);
		return delta < TimeConstants.MINUTE_MILLIS * 2;
	}

	public static Map<Long, Long> getAttackers(Creature mob){
		if (mob == null) {
			return null;
		}
		try {
			return ReflectionUtil.getPrivateField(mob, ReflectionUtil.getField(mob.getClass(), "attackers"));
		} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
			logger.log(Level.WARNING, "", e);
		}
		return null;
	}

	public static double getCreatureStrength(Creature mob){
		if (mob == null) {
			return 0D;
		}

		float combatRating = mob.getBaseCombatRating() + mob.getBonusCombatRating();
		float maxDamage = Math.max(mob.getTemplate().getBreathDamage(), mob.getHandDamage());
		maxDamage = Math.max(maxDamage, mob.getBiteDamage());
		maxDamage = Math.max(maxDamage, mob.getKickDamage());
		maxDamage = Math.max(maxDamage, mob.getHeadButtDamage());

		double fighting = mob.getFightingSkill().getKnowledge();
		double weaponlessFighting = mob.getWeaponLessFightingSkill().getKnowledge();
		double fightingSkill = Math.max(fighting, weaponlessFighting);
		double bodyStrength = mob.getBodyStrength().getKnowledge();

		fightingSkill /= mob.getArmourMod();

		double creatureStrength = 100D + (combatRating * Math.cbrt(maxDamage) * Math.cbrt(fightingSkill) * Math.cbrt(bodyStrength));
		creatureStrength *= 0.8d;

		double k = 100000d;
		creatureStrength = (creatureStrength * Math.pow(2, (-(creatureStrength / k))) + k * (1 - Math.pow(2, -creatureStrength / k)))
				/ (1 + Math.pow(2, -creatureStrength / k));

		if(mob.isAggHuman() && creatureStrength < 100D){
			creatureStrength *= 1 + (Server.rand.nextFloat() * 0.2f);
			creatureStrength = Math.max(creatureStrength, 100D);
		}else if(!mob.isAggHuman() && creatureStrength < 300D){
			creatureStrength *= 0.4f;
			creatureStrength *= 1 + (Server.rand.nextFloat() * 0.2f);
			creatureStrength = Math.max(creatureStrength, 10D);
		}

		return creatureStrength;
	}

	public static void init(){
		try {
			ClassPool classPool = HookManager.getInstance().getClassPool();
			final Class<Bounty> thisClass = Bounty.class;
			String replace;

			CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");

			if (WyvernMods.usePlayerBounty) {
				Util.setReason("Hook for Player Bounty.");
				replace = PlayerBounty.class.getName() + ".checkPlayerBounty(player, this);"
						+ "$_ = $proceed($$);";
				Util.instrumentDeclared(thisClass, ctCreature, "modifyFightSkill", "checkCoinAward", replace);
			}

			CtClass ctString = classPool.get("java.lang.String");
			CtClass[] params1 = new CtClass[]{
					CtClass.booleanType,
					ctString,
					CtClass.booleanType
			};
			String desc1 = Descriptor.ofMethod(CtClass.voidType, params1);

			replace = "$_ = $proceed($$);"
					+ LootBounty.class.getName() + ".checkLootTable(this, corpse);";
			Util.instrumentDescribed(thisClass, ctCreature, "die", desc1, "setRotation", replace);

		} catch (NotFoundException e) {
			throw new HookException(e);
		}
	}
}