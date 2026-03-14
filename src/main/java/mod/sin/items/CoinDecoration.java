package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class CoinDecoration {
	public static final Logger logger = Logger.getLogger(CoinDecoration.class.getName());
	public static int templateId;
	private static final String NAME = "coin pile";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.coin.pile");
			itemBuilder.name(NAME, "coin piles", "A pile of decorative coins.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
					ItemTypes.ITEM_TYPE_DESTROYABLE,
					ItemTypes.ITEM_TYPE_TURNABLE,
					ItemTypes.ITEM_TYPE_DECORATION,
					ItemTypes.ITEM_TYPE_NOT_MISSION,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_COLORABLE
			});
			itemBuilder.imageNumber((short) 572);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 5, 5);
			itemBuilder.primarySkill(SkillList.MISCELLANEOUS);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.pile.coin.");
			itemBuilder.difficulty(70.0f);
			itemBuilder.weightGrams(1000);
			itemBuilder.material(Materials.MATERIAL_COPPER);
			itemBuilder.value(100);

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
				final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.MISCELLANEOUS,
						ItemList.coinCopper, ItemList.coinCopper, templateId, false, false, 0f, true, false, CreationCategories.DECORATION);
				if (entry != null) {
					entry.addRequirement(new CreationRequirement(1, ItemList.coinCopper, 3, true));
				} else {
					logger.warning("Failed to create " + NAME + " creation entry: entry is null");
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating " + NAME + " creation entry", e);
			}
		} else {
			logger.warning(NAME + " does not have a template ID on creation entry.");
		}
	}
}
