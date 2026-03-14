package mod.sin.armour;

import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import mod.sin.wyvern.IconzzHandler;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlimmerscaleSleeve {
	public static final Logger logger = Logger.getLogger(GlimmerscaleSleeve.class.getName());
	public static int templateId;
	private static final String NAME = "glimmerscale sleeve";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.glimmerscale.sleeve");
			itemBuilder.name(NAME, "glimmerscale sleeves", "A glimmerscale sleeve.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_ARMOUR,
					ItemTypes.ITEM_TYPE_DRAGONARMOUR
			});
			itemBuilder.imageNumber(IconzzHandler.glimmerscaleSleeveId);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(2, 40, 40);
			itemBuilder.primarySkill(-10);
			itemBuilder.bodySpaces(new byte[]{3, 4});
			itemBuilder.modelName("model.armour.sleeve.dragon.scale.leather.");
			itemBuilder.difficulty(74.0f);
			itemBuilder.weightGrams(1800);
			itemBuilder.material(Materials.MATERIAL_GLIMMERSTEEL);
			itemBuilder.value(1000000);

			ItemTemplate template = itemBuilder.build();
			if (template == null) {
				logger.log(Level.SEVERE, "Failed to create item template for " + NAME);
				throw new IOException("Failed to create item template for " + NAME);
			}
			templateId = template.getTemplateId();
			logger.info(NAME + " TemplateID: " + templateId);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating template for " + NAME, e);
			throw e;
		}
	}
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0 && Glimmerscale.templateId > 0){
			try {
				logger.info("Creating " + NAME + " creation entry, ID = " + templateId);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_ARMOUR_PLATE, ItemList.anvilLarge, Glimmerscale.templateId,
						templateId, false, true, 0.0f, false, false, CreationCategories.ARMOUR);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for " + NAME, e);
			}
		}else{
			logger.log(Level.SEVERE, NAME + " or Glimmerscale does not have a valid template ID on creation entry.");
		}
	}
}
