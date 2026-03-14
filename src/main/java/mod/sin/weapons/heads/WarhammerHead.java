package mod.sin.weapons.heads;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class WarhammerHead {
	public static final Logger logger = Logger.getLogger(WarhammerHead.class.getName());
	public static int templateId;
	private static final String NAME = "warhammer head";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.warhammer.head");
			itemBuilder.name(NAME, "warhammer heads", "A warhammer head.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_METAL
			});
			itemBuilder.imageNumber((short) 1232);
			itemBuilder.behaviourType((short) 35);
			itemBuilder.combatDamage(40);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 80);
			itemBuilder.primarySkill(SkillList.WARHAMMER);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.artifact.hammerhuge.");
			itemBuilder.difficulty(25.0f);
			itemBuilder.weightGrams(6000);
			itemBuilder.material(Materials.MATERIAL_IRON);
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
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.ironBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.steelBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.silverBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.copperBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.tinBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.goldBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.zincBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.bronzeBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.brassBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.adamantineBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.glimmerSteelBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_WEAPON_HEADS, ItemList.anvilLarge, ItemList.seryllBar,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPON_HEADS);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating " + NAME + " creation entries", e);
			}
		} else {
			logger.warning(NAME + " does not have a template ID on creation entry.");
		}
	}
}
