package mod.sin.wyvern.bounty;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.*;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.items.*;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.piddagoras.duskombat.DamageEngine;
import mod.sin.creatures.Reaper;
import mod.sin.creatures.SpectralDrake;
import mod.sin.items.AffinityOrb;
import mod.sin.items.FriyanTablet;
import mod.sin.items.caches.*;
import mod.sin.wyvern.MiscChanges;
import mod.sin.wyvern.Titans;
import mod.sin.wyvern.util.ItemUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LootBounty {
	public static final Logger logger = Logger.getLogger(LootBounty.class.getName());
	protected static final Random random = new Random();

	public static void displayLootAssistance(Creature mob){
		if (mob == null || DamageEngine.dealtDamage == null) {
			return;
		}
		if (DamageEngine.dealtDamage.containsKey(mob.getWurmId())) {
			logger.info("Found the damageDealt entry, parsing...");
			ArrayList<String> names = new ArrayList<>();
			ArrayList<Double> damages = new ArrayList<>();
			if (DamageEngine.dealtDamage.get(mob.getWurmId()) == null) {
				logger.warning("Damage map for creature is null");
				return;
			}
			for (long creatureId : DamageEngine.dealtDamage.get(mob.getWurmId()).keySet()) {
				Creature player = Players.getInstance().getPlayerOrNull(creatureId);
				if (player != null) {
					names.add(player.getName());
					Double damage = DamageEngine.dealtDamage.get(mob.getWurmId()).get(creatureId);
					if (damage != null) {
						damages.add(damage);
					}
				} else {
					Creature creature = Creatures.getInstance().getCreatureOrNull(creatureId);
					if (creature != null) {
						logger.info("Skipping creature " + creature.getName() + " in loot assistance.");
					}
				}
			}
			if (names.isEmpty()) {
				logger.warning("No valid players found for loot assistance for " + mob.getName());
				return;
			}
			logger.info("Names have been added: " + names);
			StringBuilder strBuilder = new StringBuilder("Loot Assistance <Damagers> (" + mob.getName() + "): ");
			DecimalFormat formatter = new DecimalFormat("#,###,###");
			while (!names.isEmpty()) {
				int index = Server.rand.nextInt(names.size());
				strBuilder.append(names.get(index));
				strBuilder.append(" [");
				strBuilder.append(formatter.format(Math.round(damages.get(index))));
				strBuilder.append("]");
				names.remove(index);
				damages.remove(index);
				if (!names.isEmpty()) {
					strBuilder.append(", ");
				}
			}
			MiscChanges.sendServerTabMessage("event", strBuilder.toString(), 0, 128, 255);
			logger.info("Broadcast loot assistance message success [Damage].");
		} else {
			logger.warning("Powerful creature " + mob.getName() + " died, but no players were credited to its death [Damage].");
		}
	}
	public static int doRollingCrystalReward(Creature mob, Item corpse, double cretStr, int templateId, int chance, double reductionPerRoll){
		if (mob == null || corpse == null || chance <= 0 || reductionPerRoll <= 0) {
			return 0;
		}
		try {
			int addedCrystals = 0;
			for (double rollingCounter = cretStr; rollingCounter > 0; rollingCounter -= reductionPerRoll) {
				int adjustedChance = chance + addedCrystals;
				if (adjustedChance > 0 && random.nextInt(adjustedChance) == 0) {
					Item chaosCrystal = ItemFactory.createItem(templateId, (float) (random.nextFloat() * Math.min(100, Math.cbrt(rollingCounter))), "");
					if (random.nextInt(40) == 0) {
						chaosCrystal.setRarity((byte) 1);
					} else if (mob.isUnique() && random.nextInt(5) == 0) {
						if (random.nextInt(5) == 0) {
							chaosCrystal.setRarity((byte) 2);
						} else {
							chaosCrystal.setRarity((byte) 1);
						}
					}
					corpse.insertItem(chaosCrystal);
					++addedCrystals;
				}
			}
			return addedCrystals;
		} catch (FailedException | NoSuchTemplateException e) {
			logger.log(Level.WARNING, "Error creating rolling crystal reward", e);
		}
		return 0;
	}
	public static void insertUniqueLoot(Creature mob, Item corpse){
		if (mob == null || corpse == null || mob.getTemplate() == null) {
			return;
		}
		try {
			Item affinityOrb = ItemFactory.createItem(AffinityOrb.templateId, 90 + (10 * random.nextFloat()), "");
			corpse.insertItem(affinityOrb);
			int[] cacheIds = {
					ArtifactCache.templateId,
					CrystalCache.templateId, CrystalCache.templateId,
					DragonCache.templateId, DragonCache.templateId,
					MoonCache.templateId, MoonCache.templateId,
					RiftCache.templateId,
					TreasureMapCache.templateId
			};
			for (int i = 1 + Server.rand.nextInt(3); i > 0; --i) {
				Item cache = ItemFactory.createItem(cacheIds[Server.rand.nextInt(cacheIds.length)], 50 + (30 * random.nextFloat()), "");
				if (Server.rand.nextInt(5) == 0) {
					cache.setRarity((byte) 1);
				}
				corpse.insertItem(cache);
			}
			if (mob.isDragon()) {
				int mTemplate = mob.getTemplate().getTemplateId();
				int lootTemplate = ItemList.drakeHide;
				if (mTemplate == CreatureTemplateFactory.DRAGON_BLACK_CID || mTemplate == CreatureTemplateFactory.DRAGON_BLUE_CID || mTemplate == CreatureTemplateFactory.DRAGON_GREEN_CID
						|| mTemplate == CreatureTemplateFactory.DRAGON_RED_CID || mTemplate == CreatureTemplateFactory.DRAGON_WHITE_CID) {
					lootTemplate = ItemList.dragonScale;
				}
				logger.info("Generating extra hide & scale to insert on the corpse of " + mob.getName() + ".");
				ItemTemplate itemTemplate = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
				for (int i = 0; i < 2; ++i) {
					Item loot = ItemFactory.createItem(lootTemplate, 80 + (15 * random.nextFloat()), "");
					String creatureName = mob.getTemplate().getName().toLowerCase();
					String lootName = loot.getName();
					if (lootName != null && !lootName.contains(creatureName)) {
						loot.setName(creatureName + " " + itemTemplate.getName());
					}
					loot.setData2(mTemplate);
					int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == ItemList.drakeHide ? 3 : 1);
					loot.setWeight((int) ((weightGrams * 0.02f) + (weightGrams * 0.02f * random.nextFloat())), true);
					corpse.insertItem(loot);
				}
			}
		} catch (FailedException | NoSuchTemplateException e) {
			logger.log(Level.WARNING, "Error inserting unique loot", e);
		}
	}
	public static void blessWorldWithMoonVeins(Creature mob){
		if (mob == null || mob.getTemplate() == null || Server.surfaceMesh == null || Server.caveMesh == null) {
			return;
		}
		for (int i = 8 + Server.rand.nextInt(5); i > 0; --i) {
			int x = random.nextInt(Server.surfaceMesh.getSize());
			int y = random.nextInt(Server.surfaceMesh.getSize());
			short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
			int type = Tiles.decodeType(Server.caveMesh.getTile(x, y));
			if (height >= 100 && (type == Tiles.Tile.TILE_CAVE_WALL.id || type == Tiles.Tile.TILE_CAVE.id)) {
				Tiles.Tile tileType = random.nextBoolean() ? Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE : Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL;
				Server.caveMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(Server.caveMesh.getTile(x, y)), tileType.id, Tiles.decodeData(Server.caveMesh.getTile(x, y))));
				Players.getInstance().sendChangedTile(x, y, false, true);
				Server.setCaveResource(x, y, 400 + random.nextInt(600));
				Village v = Villages.getVillage(x, y, true);
				for (int vx = -20; vx < 20 && v == null; vx += 5) {
					for (int vy = -20; vy < 20 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
						// Search loop
					}
				}
				if (v != null) {
					HistoryManager.addHistory(mob.getTemplate().getName(), "blesses the world with a " + tileType.getName() + " near " + v.getName() + "!");
					MiscChanges.sendServerTabMessage("rumors", mob.getTemplate().getName() + " blesses the world with a " + tileType.getName() + " near " + v.getName() + "!", 255, 255, 255);
				}
				logger.info("Placed a " + tileType.getName() + " at " + x + ", " + y + " - " + height + " height");
			}
		}
		Server.getInstance().broadCastAlert("The death of the " + mob.getTemplate().getName() + " has blessed the world with valuable ores!");
	}
	public static void spawnFriyanTablets(){
		if (Server.surfaceMesh == null) {
			return;
		}
		for (int i = 5 + random.nextInt(5); i > 0; --i) {
			int x = random.nextInt(Server.surfaceMesh.getSize());
			int y = random.nextInt(Server.surfaceMesh.getSize());
			short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
			if (height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30) {
				try {
					ItemFactory.createItem(FriyanTablet.templateId, 80f + random.nextInt(20), (float)x * 4, (float)y * 4, random.nextFloat() * 360f, true, (byte)0, -10, "Friyanouce");
					logger.info("Created a Tablet of Friyan at " + x + ", " + y + ".");
				} catch (NoSuchTemplateException | FailedException e) {
					logger.log(Level.WARNING, "Error spawning Friyan tablet", e);
				}
			}
		}
	}
	public static void handleDragonLoot(Creature mob, Item corpse){
		if (mob == null || mob.getTemplate() == null || corpse == null) {
			return;
		}
		try {
			int mTemplate = mob.getTemplate().getTemplateId();
			int lootTemplate = ItemList.drakeHide;
			byte ctype;
			if (mTemplate == CreatureTemplateFactory.DRAGON_BLACK_CID || mTemplate == CreatureTemplateFactory.DRAGON_BLUE_CID || mTemplate == CreatureTemplateFactory.DRAGON_GREEN_CID
					|| mTemplate == CreatureTemplateFactory.DRAGON_RED_CID || mTemplate == CreatureTemplateFactory.DRAGON_WHITE_CID) {
				ctype = 99;
				lootTemplate = ItemList.dragonScale;
			} else {
				ctype = (byte)Math.max(0, Server.rand.nextInt(17) - 5);
			}

			float x = mob.getPosX();
			float y = mob.getPosY();

			CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(SpectralDrake.templateId);
			Creature spectralDrake = Creature.doNew(template.getTemplateId(), true, x, y, random.nextFloat() * 360.0f, mob.getLayer(),
					template.getName(), (byte)0, mob.getKingdomId(), ctype, false, (byte)150);
			Server.getInstance().broadCastAction("The spirit of the " + mob.getTemplate().getName() + " is released into the world!", mob, 20);
			Server.getInstance().broadCastAlert(spectralDrake.getName() + " is released from the soul of the " + mob.getTemplate().getName() + ", seeking vengeance for its physical form!");

			logger.info("Generating extra hide & scale to insert on the corpse of " + mob.getName() + ".");
			ItemTemplate itemTemplate = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
			for (int i = 0; i < 2; ++i) {
				Item loot = ItemFactory.createItem(lootTemplate, 80 + (15 * random.nextFloat()), "");
				String creatureName = mob.getTemplate().getName().toLowerCase();
				String lootName = loot.getName();
				if (lootName != null && !lootName.contains(creatureName)) {
					loot.setName(creatureName + " " + itemTemplate.getName());
				}
				loot.setData2(mTemplate);
				int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == ItemList.drakeHide ? 3 : 1);
				loot.setWeight((int)((weightGrams * 0.1f) + (weightGrams * 0.1f * random.nextFloat())), true);
				corpse.insertItem(loot);
			}
			if (spectralDrake != null && spectralDrake.getInventory() != null) {
				for (int i = 0; i < 4; ++i) {
					Item loot = ItemFactory.createItem(lootTemplate, 80 + (15 * random.nextFloat()), "");
					String creatureName = mob.getTemplate().getName().toLowerCase();
					String lootName = loot.getName();
					if (lootName != null && !lootName.contains(creatureName)) {
						loot.setName(creatureName + " " + itemTemplate.getName());
					}
					loot.setData2(mTemplate);
					int weightGrams = itemTemplate.getWeightGrams() * (lootTemplate == ItemList.drakeHide ? 3 : 1);
					loot.setWeight((int)((weightGrams * 0.05f) + (weightGrams * 0.05f * random.nextFloat())), true);
					spectralDrake.getInventory().insertItem(loot);
				}
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error handling dragon loot", e);
		}
	}
	public static void handleChampionLoot(Item corpse){
		if (corpse == null) {
			return;
		}
		try {
			Item tool = ItemUtil.createRandomLootTool();
			if (tool != null) {
				corpse.insertItem(tool, true);
			}
			if (random.nextInt(100) < 75) {
				corpse.insertItem(ItemFactory.createItem((random.nextBoolean() ? ItemList.adamantineBar : ItemList.glimmerSteelBar), 30 + (30 * random.nextFloat()), ""));
			}
			if (random.nextInt(100) < 5) {
				int[] maskTemplates = {
						ItemList.maskEnlightended,
						ItemList.maskRavager,
						ItemList.maskPale,
						ItemList.maskShadow,
						ItemList.maskChallenge,
						ItemList.maskIsles,
						ItemList.maskOfTheReturner
				};
				corpse.insertItem(ItemFactory.createItem(maskTemplates[random.nextInt(maskTemplates.length)], 90 + (9 * random.nextFloat()), ""));
			}
			if (random.nextInt(100) < 1) {
				Item bone = ItemFactory.createItem(867, 90 + (10 * random.nextFloat()), "");
				bone.setRarity((byte)1);
				if (random.nextInt(100) < 1) {
					bone.setRarity((byte)2);
				}
				corpse.insertItem(bone);
			}
		} catch (FailedException | NoSuchTemplateException e) {
			logger.log(Level.WARNING, "Error handling champion loot", e);
		}
	}
	public static void checkLootTable(Creature mob, Item corpse){
		if (mob == null || corpse == null || mob.getTemplate() == null) {
			return;
		}
		if (mob.isReborn() || mob.isBred()) {
			return;
		}
		//double cretStr = Bounty.getCreatureStrength(mob);
		//int numCrystals = 0;
    	/*double crystalStr = cretStr;
    	if(mob.isUnique()){ // Uniques will drop 3x as many, and have special properties to enable dropping rare and possibly supreme versions as well.
    		crystalStr *= 3;
    	}else if(Servers.localServer.PVPSERVER){ // Arena gives double the amount of crystals.
    		crystalStr *= 1.5;
    	}*/
		// Award chaos crystals if the strength is high enough:
	    /*if(crystalStr > 3000){ // 30 copper
	    	numCrystals += doRollingCrystalReward(mob, corpse, crystalStr, ChaosCrystal.templateId, 4, 5000);
	    }
	    if(crystalStr > 10000){ // 1 silver
	    	numCrystals += doRollingCrystalReward(mob, corpse, crystalStr, EnchantersCrystal.templateId, 5, 20000);
	    }*/

		boolean sendLootHelp = false;
		// Begin loot table drops
		int templateId = mob.getTemplate().getTemplateId();

		if (Servers.localServer.PVPSERVER && mob.isPlayer()) {
			if (mob.isDeathProtected()) {
				logger.info("Death protection was active for " + mob.getName() + ". Inserting silver coin reward.");
				try {
					Item silver = ItemFactory.createItem(ItemList.coinSilver, 99f, null);
					corpse.insertItem(silver, true);
				} catch (FailedException | NoSuchTemplateException e) {
					logger.log(Level.WARNING, "Error creating silver coin reward", e);
				}
			}
			Item[] items = mob.getAllItems();
			if (items != null) {
				for (Item item : items) {
					if (item != null && item.isRepairable()) {
						item.setDamage(Math.min(99f, item.getDamage() + Math.max(10f + (Server.rand.nextFloat() * 5f), 10f * item.getDamageModifier(false))));
					}
				}
			}
		}
		if (templateId == Reaper.templateId || templateId == SpectralDrake.templateId) {
			Server.getInstance().broadCastAlert("The " + mob.getName() + " has been slain. A new creature shall enter the realm shortly.");
			sendLootHelp = true;
		} else if (Titans.isTitan(mob)) {
			Server.getInstance().broadCastAlert("The Titan " + mob.getName() + " has been defeated!");
			MiscChanges.sendGlobalFreedomChat(mob, "The Titan " + mob.getName() + " has been defeated!", 255, 105, 180);
			MiscChanges.sendServerTabMessage("titan", "The Titan " + mob.getName() + " has been defeated!", 255, 105, 180);
			Item armour = ItemUtil.createRandomPlateChain(50f, 80f, Materials.MATERIAL_SERYLL, mob.getName());
			if (armour != null) {
				ItemUtil.applyEnchant(armour, (byte) 110, 80f + (Server.rand.nextInt(40)));
				corpse.insertItem(armour, true);
			}
			Titans.removeTitan(mob);
			sendLootHelp = true;
		}
		if (mob.getTemplate().getTemplateId() == CreatureTemplateFactory.GOBLIN_CID) {
			try {
				int[] lumpIds = {
						ItemList.adamantineBar,
						ItemList.brassBar,
						ItemList.bronzeBar,
						ItemList.copperBar,
						ItemList.glimmerSteelBar,
						ItemList.goldBar,
						ItemList.ironBar,
						ItemList.leadBar,
						ItemList.silverBar,
						ItemList.steelBar,
						ItemList.zincBar
				};
				Item randomLump = ItemFactory.createItem(lumpIds[random.nextInt(lumpIds.length)], 20 + (60 * random.nextFloat()), "");
				corpse.insertItem(randomLump);
			} catch (FailedException | NoSuchTemplateException e) {
				logger.log(Level.WARNING, "Error creating goblin loot", e);
			}
		}
		if (mob.isUnique()) {
			blessWorldWithMoonVeins(mob);
			spawnFriyanTablets();
			insertUniqueLoot(mob, corpse);
			sendLootHelp = true;
		}
		if (mob.getStatus() != null && mob.getStatus().isChampion()) {
			handleChampionLoot(corpse);
		}
		if (sendLootHelp) {
			logger.info("Beginning loot assistance message generation...");
			displayLootAssistance(mob);
		}
	}
}
