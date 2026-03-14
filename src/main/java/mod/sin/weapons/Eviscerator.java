package mod.sin.weapons;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

import mod.sin.weapons.titan.WilhelmsWrath;

public class Eviscerator {
	public static final Logger logger = Logger.getLogger(Eviscerator.class.getName());
	public static int templateId;
	private static final String NAME = "eviscerator";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.eviscerator");
			itemBuilder.name(NAME, "eviscerators", "A one-handed scythe of despair, optimal for the Genocide of entire species.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_WEAPON,
					ItemTypes.ITEM_TYPE_WEAPON_SLASH
			});
			itemBuilder.imageNumber((short) 753);
			itemBuilder.behaviourType((short) 35);
			itemBuilder.combatDamage(40);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 80);
			itemBuilder.primarySkill(SkillList.SCYTHE);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.weapon.scythe.");
			itemBuilder.difficulty(90.0f);
			itemBuilder.weightGrams(500);
			itemBuilder.material(Materials.MATERIAL_ADAMANTINE);
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
				if (WilhelmsWrath.templateId > 0) {
					CreationEntryCreator.createSimpleEntry(SkillList.GROUP_SMITHING_WEAPONSMITHING, WilhelmsWrath.templateId, WilhelmsWrath.templateId,
							templateId, true, true, 0.0f, false, false, CreationCategories.WEAPONS);
				} else {
					logger.warning("WilhelmsWrath template ID is not initialized, skipping creation entry");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating " + NAME + " creation entry", e);
			}
		} else {
			logger.warning(NAME + " does not have a template ID on creation entry.");
		}
	}
}
