package mod.sin.weapons;

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

public class BattleYoyo {
	public static final Logger logger = Logger.getLogger(BattleYoyo.class.getName());
	public static int templateId;
	private static final String NAME = "battle yoyo";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.battle.yoyo");
			itemBuilder.name(NAME, "battle yoyos", "A reinforced yoyo meant for combat. Designed to see whether walking the dog is an effective murder technique.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_WOOD,
					ItemTypes.ITEM_TYPE_WEAPON,
					ItemTypes.ITEM_TYPE_WEAPON_CRUSH
			});
			itemBuilder.imageNumber((short) 761);
			itemBuilder.behaviourType((short) 35);
			itemBuilder.combatDamage(35);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 20);
			itemBuilder.primarySkill(SkillList.YOYO);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.toy.yoyo.");
			itemBuilder.difficulty(40.0f);
			itemBuilder.weightGrams(1000);
			itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
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
				CreationEntryCreator.createSimpleEntry(SkillList.TOYMAKING, ItemList.clothString, ItemList.shaft,
						templateId, false, true, 0.0f, false, false, CreationCategories.WEAPONS);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating " + NAME + " creation entry", e);
			}
		} else {
			logger.warning(NAME + " does not have a template ID on creation entry.");
		}
	}
}
