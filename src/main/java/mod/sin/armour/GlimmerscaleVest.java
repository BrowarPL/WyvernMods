package mod.sin.armour;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mod.sin.wyvern.IconzzHandler;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class GlimmerscaleVest {
	public static final Logger logger = Logger.getLogger(GlimmerscaleVest.class.getName());
	public static int templateId;
	private static final String NAME = "glimmerscale vest";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.glimmerscale.vest");
			itemBuilder.name(NAME, "glimmerscale vests", "A glimmerscale vest.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_ARMOUR,
					ItemTypes.ITEM_TYPE_DRAGONARMOUR
			});
			itemBuilder.imageNumber(IconzzHandler.glimmerscaleVestId);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(2, 40, 40);
			itemBuilder.primarySkill(-10);
			itemBuilder.bodySpaces(new byte[]{2});
			itemBuilder.modelName("model.armour.torso.dragon.scale.leather.");
			itemBuilder.difficulty(80.0f);
			itemBuilder.weightGrams(4500);
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
