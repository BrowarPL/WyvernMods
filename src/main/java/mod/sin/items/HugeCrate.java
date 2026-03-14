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

public class HugeCrate {
	public static final Logger logger = Logger.getLogger(HugeCrate.class.getName());
	public static int templateId;
	private static final String NAME = "huge crate";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.crate.huge");
			itemBuilder.name(NAME, "huge crates", "A huge crate made from planks, primarily used to transport goods.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
					ItemTypes.ITEM_TYPE_WOOD,
					ItemTypes.ITEM_TYPE_TURNABLE,
					ItemTypes.ITEM_TYPE_DECORATION,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_HOLLOW,
					ItemTypes.ITEM_TYPE_COLORABLE,
					ItemTypes.ITEM_TYPE_BULKCONTAINER,
					ItemTypes.ITEM_TYPE_TRANSPORTABLE
			});
			itemBuilder.imageNumber((short) 311);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(9072000);
			itemBuilder.dimensions(140, 140, 140);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.container.crate.large.");
			itemBuilder.difficulty(70.0f);
			itemBuilder.weightGrams(20000);
			itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
			itemBuilder.value(10000);
			itemBuilder.isTraded(false);

			ItemTemplate template = itemBuilder.build();
			if (template != null) {
				templateId = template.getTemplateId();
				logger.info(NAME+" TemplateID: "+ templateId);
			} else {
				logger.log(Level.SEVERE, "Failed to create item template for "+NAME);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating item template for "+NAME, e);
			throw e;
		}
	}
	public void initCreationEntry() {
		logger.info("initCreationEntry()");
		if (templateId > 0) {
			try {
				logger.info("Creating "+NAME+" creation entry, ID = "+templateId);
				final AdvancedCreationEntry entry = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE,
						ItemList.plank, ItemList.nailsIronLarge, templateId, false, false, 0f, true, false, CreationCategories.TOOLS);
				if (entry != null) {
					entry.addRequirement(new CreationRequirement(1, ItemList.plank, 20, true));
				} else {
					logger.log(Level.SEVERE, "Failed to create advanced creation entry for "+NAME);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for "+NAME, e);
			}
		} else {
			logger.log(Level.WARNING, NAME+" does not have a template ID on creation entry.");
		}
	}
}
