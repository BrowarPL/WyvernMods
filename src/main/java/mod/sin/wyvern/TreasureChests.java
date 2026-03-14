package mod.sin.wyvern;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.items.AffinityOrb;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TreasureChests {
	public static final Logger logger = Logger.getLogger(TreasureChests.class.getName());

	public static void doItemSpawn(Item inventory, int[] templateTypes, float startQl, float qlValRange, int maxNums) {
		if (inventory == null || templateTypes == null || templateTypes.length == 0 || maxNums <= 0) {
			return;
		}

		for (int templateType : templateTypes) {
			for (int nums = 0; nums < maxNums; ++nums) {
				try {
					byte rrarity;
					boolean isBoneCollar = templateType == ItemList.boneCollar;
					rrarity = (byte) (Server.rand.nextInt(100) == 0 || isBoneCollar ? 1 : 0);
					if (rrarity > 0) {
						rrarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 2 : 1);
					}
					if (rrarity > 1) {
						rrarity = (byte) (Server.rand.nextInt(100) == 0 && isBoneCollar ? 3 : 2);
					}
					float newql = startQl + Server.rand.nextFloat() * qlValRange;

					Item toInsert = ItemFactory.createItem(templateType, newql, rrarity, "");

					if (templateType == ItemList.statueHota) {
						toInsert.setAuxData((byte) Server.rand.nextInt(10));
						toInsert.setWeight(50000, true);
					}
					if (templateType == ItemList.eggLarge) {
						toInsert.setData1(CreatureTemplateCreator.getRandomDragonOrDrakeId());
					}
					if (templateType == ItemList.drakeHide) {
						int colorId = CreatureTemplateCreator.getRandomDrakeId();
						toInsert.setData1(colorId);
						CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
						String creatureName = cTemplate.getName().toLowerCase();
						if (!toInsert.getName().contains(creatureName)) {
							toInsert.setName(creatureName + " " + toInsert.getTemplate().getName());
						}
						toInsert.setWeight(50 + Server.rand.nextInt(100), true);
					}
					if (templateType == ItemList.dragonScale) {
						int[] dragonIds = new int[]{
								CreatureTemplateIds.DRAGON_BLACK_CID,
								CreatureTemplateIds.DRAGON_BLUE_CID,
								CreatureTemplateIds.DRAGON_GREEN_CID,
								CreatureTemplateIds.DRAGON_RED_CID,
								CreatureTemplateIds.DRAGON_WHITE_CID
						};
						int colorId = dragonIds[Server.rand.nextInt(dragonIds.length)];
						toInsert.setData1(colorId);
						CreatureTemplate cTemplate = CreatureTemplateFactory.getInstance().getTemplate(colorId);
						String creatureName = cTemplate.getName().toLowerCase();
						if (!toInsert.getName().contains(creatureName)) {
							toInsert.setName(creatureName + " " + toInsert.getTemplate().getName());
						}
						toInsert.setWeight(100 + Server.rand.nextInt(150), true);
					}
					if (templateType == ItemList.riftCrystal || templateType == ItemList.riftWood || templateType == ItemList.riftStone) {
						toInsert.setHasNoDecay(true);
					}
					inventory.insertItem(toInsert, true);
				} catch (NoSuchTemplateException | FailedException | NoSuchCreatureTemplateException e) {
					logger.log(Level.WARNING, "Failed in TreasureChests", e);
				}
			}
		}
	}

	public static void newFillTreasureChest(Item item, int auxdata) {
		int[] normalGems = new int[]{ItemList.emerald, ItemList.ruby, ItemList.opal, ItemList.diamond, ItemList.sapphire};
		int[] starGems = new int[]{375, 377, 379, 381, 383};
		int[] lumps = new int[]{44, 45, 46, 47, 48, 49, 205, 220, 221, 223, 694, 698, 837};
		int[] potions = new int[]{871, 874, 875, 876, 877, 878, 879, 881, 883};

		if (auxdata < 60) {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)1);
			}

			int[] templateTypes = new int[]{ItemList.sourceCrystal, ItemList.adamantineBar,
					ItemList.glimmerSteelBar, normalGems[Server.rand.nextInt(normalGems.length)]};
			doItemSpawn(item, templateTypes, 70.0f, 30.0f, 1);

			if (Server.rand.nextBoolean()) {
				doItemSpawn(item, new int[]{ItemList.seryllBar}, 60.0f, 20.0f, 1);
			}

			if (Server.rand.nextBoolean()) {
				doItemSpawn(item, new int[]{lumps[Server.rand.nextInt(lumps.length)]}, 80.0f, 20.0f, 1);
			}

			if (Server.rand.nextInt(5) == 0) {
				doItemSpawn(item, new int[]{ItemList.potionIllusion}, 10.0f, 90.0f, 1);
			}

			if (Server.rand.nextInt(20) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, auxdata, 60.0f - auxdata, 1);
			}

			if (Server.rand.nextInt(200 - auxdata) == 0){
				doItemSpawn(item, new int[]{AffinityOrb.templateId}, 80.0f, 10.0f, 1);
			}

			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{potions[Server.rand.nextInt(potions.length)]}, 50.0f, 50.0f, 1);
			}

			switch (Server.rand.nextInt(3)) {
				case 0:
					doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1);
					break;
				case 1:
					doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1);
					break;
				case 2:
					doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1);
					break;
			}

		} else if (auxdata < 90) {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)2);
			}

			int[] templateTypes = new int[]{ItemList.sourceCrystal, 374 + Server.rand.nextInt(10)};
			doItemSpawn(item, templateTypes, 80.0f, 20.0f, 1);

			int[] templateTypes2 = new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar};
			doItemSpawn(item, templateTypes2, 80.0f, 20.0f, 2 + Server.rand.nextInt(2));

			int[] templateTypes3 = new int[]{ItemList.seryllBar};
			doItemSpawn(item, templateTypes3, 80.0f, 20.0f, 1 + Server.rand.nextInt(3));

			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, auxdata, 90.0f - auxdata, 1);
			}

			if (Server.rand.nextInt(150 - auxdata) == 0){
				doItemSpawn(item, new int[]{AffinityOrb.templateId}, 90.0f, 5.0f, 1);
			}

			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{Server.rand.nextBoolean() ? ItemList.drakeHide : ItemList.dragonScale}, 80.0f, 20.0f, 1);
			}

			doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1);
			doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1);
			doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1);

		} else {
			if(item.getTemplateId() == ItemList.treasureChest){
				item.setRarity((byte)3);
			}

			int[] templateTypes = new int[]{ItemList.sourceCrystal, starGems[Server.rand.nextInt(starGems.length)]};
			doItemSpawn(item, templateTypes, 90.0f, 10.0f, 1);

			int[] templateTypes2 = new int[]{ItemList.adamantineBar, ItemList.glimmerSteelBar};
			doItemSpawn(item, templateTypes2, 80.0f, 20.0f, 3 + Server.rand.nextInt(3));

			int[] templateTypes3 = new int[]{ItemList.seryllBar};
			doItemSpawn(item, templateTypes3, 80.0f, 20.0f, 2 + Server.rand.nextInt(3));

			if(Server.rand.nextBoolean()){
				doItemSpawn(item, new int[]{AffinityOrb.templateId}, 99.0f, 1.0f, 1);
			}

			if (Server.rand.nextInt(5) == 0) {
				doItemSpawn(item, new int[]{ItemList.fireworks}, auxdata, 100.0f - auxdata, 1);
			}

			if (Server.rand.nextInt(10) == 0) {
				doItemSpawn(item, new int[]{Server.rand.nextBoolean() ? ItemList.drakeHide : ItemList.dragonScale}, 90.0f, 10.0f, 1);
			}

			if (Server.rand.nextInt(100) == 0) {
				doItemSpawn(item, new int[]{ItemList.spyglass}, 99.0f, 1.0f, 1);
			}

			if (Server.rand.nextInt(100) == 0) {
				doItemSpawn(item, new int[]{ItemList.bagKeeping}, 99.0f, 1.0f, 1);
			}

			doItemSpawn(item, new int[]{ItemList.riftStone}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));
			doItemSpawn(item, new int[]{ItemList.riftCrystal}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));
			doItemSpawn(item, new int[]{ItemList.riftWood}, 90.0f, 10.0f, 1 + Server.rand.nextInt(3));

			int rand = Server.rand.nextInt(500);
			int[] fantasticLoot;
			int amount = 1;
			if(rand < 249){
				fantasticLoot = new int[]{ItemList.drakeHide};
				amount = 3;
			} else if(rand < 349){
				fantasticLoot = new int[]{ItemList.dragonScale};
				amount = 3;
			} else if(rand < 414){
				fantasticLoot = new int[]{ItemList.statueHota};
			} else if (rand < 464) {
				fantasticLoot = new int[]{ItemList.boneCollar};
			} else if (rand < 490) {
				fantasticLoot = new int[]{795 + Server.rand.nextInt(16)};
			} else {
				fantasticLoot = new int[]{ItemList.eggLarge};
			}
			doItemSpawn(item, fantasticLoot, 99.0f, 1.0f, amount);
		}
	}

	public static void preInit() throws NotFoundException, CannotCompileException{
		ClassPool classPool = HookManager.getInstance().getClassPool();

		CtClass ctZone = classPool.get("com.wurmonline.server.zones.Zone");
		CtMethod ctCreateTreasureChest = ctZone.getDeclaredMethod("createTreasureChest");
		ctCreateTreasureChest.instrument(new ExprEditor(){
			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				if (m.getMethodName().equals("setAuxData")) {
					m.replace("$_ = $proceed((byte)(com.wurmonline.server.Server.rand.nextInt(100)));");
				}
			}
		});

		CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
		ctItem.getDeclaredMethod("fillTreasureChest").setBody("{"
				+ "  mod.sin.wyvern.TreasureChests.newFillTreasureChest(this, this.getAuxData());"
				+ "  logger.info(\"Spawned treasure chest level \"+this.getAuxData()+\" at \"+(this.getPosX()/4)+\", \"+(this.getPosY()/4));"
				+ "}");
	}
}