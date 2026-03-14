package mod.sin.weapons;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mod.sin.wyvern.IconzzHandler;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class Knuckles {
	public static final Logger logger = Logger.getLogger(Knuckles.class.getName());
	public static int templateId;
	private static final String NAME = "knuckles";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.knuckles");
			itemBuilder.name(NAME, "knuckles", "A classic weapon used in hand-to-hand combat.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_WEAPON,
					ItemTypes.ITEM_TYPE_WEAPON_CRUSH
			});
			itemBuilder.imageNumber(IconzzHandler.knucklesIconId);
			itemBuilder.behaviourType((short) 35);
			itemBuilder.combatDamage(40);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 80);
			itemBuilder.primarySkill(SkillList.WEAPONLESS_FIGHTING);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.decoration.ring.rift.2.");
			itemBuilder.difficulty(40.0f);
			itemBuilder.weightGrams(800);
			itemBuilder.material(Materials.MATERIAL_BRASS);
			itemBuilder.value(1000);

			ItemTemplate template = itemBuilder.build();
			if (template != null) {
				templateId = template.getTemplateId();
				logger.info(NAME + " TemplateID: " + templateId);
			} else {
				logger.warning("Failed to create " + NAME + " template: build returned null");
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error creating " + NAME + " template", e);
			throw new IOException("Failed to create " + NAME + " template", e);
		}
	}

	public void initCreationEntry() {
		logger.info("initCreationEntry()");
		if (templateId > 0) {
			logger.info("Creating " + NAME + " creation entry, ID = " + templateId);
			try {
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.brassBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.ironBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.steelBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.goldBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.silverBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.zincBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.tinBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.bronzeBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.leadBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.copperBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.adamantineBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.glimmerSteelBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilSmall, ItemList.seryllBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating " + NAME + " creation entries", e);
			}
		} else {
			logger.warning(NAME + " does not have a template ID on creation entry.");
		}
	}
}
