package mod.sin.wyvern.invasion;

import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.server.creatures.Creature;

public class InvasionEvent{
	private static final Logger logger = Logger.getLogger(InvasionEvent.class.getName());
	protected final Random rand = new Random();
	protected String villageName;
	protected float power;
	protected Creature invasionBoss;
	protected final HashSet<Creature> minions = new HashSet<>();

	public InvasionEvent(int x, int y, String villageName, int bossId, int templateId1, int templateId2, float power) throws Exception{
		if (villageName == null) {
			throw new IllegalArgumentException("Village name cannot be null");
		}
		this.villageName = villageName;
		this.power = power;
		try {
			this.invasionBoss = Creature.doNew(bossId, x, y, rand.nextFloat() * 360f, 0, "Necromancer", (rand.nextBoolean() ? (byte)0 : (byte)1));
			float halfPower = power / 2f;
			int minionCount = (int) ((1f + rand.nextFloat()) * halfPower / 5f);
			for (int i = 0; i < minionCount; ++i) {
				Creature minion = Creature.doNew((rand.nextBoolean() ? templateId1 : templateId2), x, y, rand.nextFloat() * 360f, 0, "Minion", (rand.nextBoolean() ? (byte)0 : (byte)1));
				if (minion != null) {
					minions.add(minion);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error creating invasion event creatures", e);
			throw e;
		}
	}
	public String getPowerString(){
		if (power > 95) {
			return "Legendary";
		} else if (power > 80) {
			return "Powerful";
		} else if (power > 60) {
			return "Strong";
		} else if (power > 40) {
			return "Mediocre";
		} else if (power > 20) {
			return "Weak";
		} else {
			return "Pathetic";
		}
	}

	public String getVillageName() {
		return villageName;
	}

	public float getPower() {
		return power;
	}

	public Creature getInvasionBoss() {
		return invasionBoss;
	}

	public HashSet<Creature> getMinions() {
		return new HashSet<>(minions);
	}
}
