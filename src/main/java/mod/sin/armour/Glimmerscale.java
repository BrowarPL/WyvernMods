package mod.sin.armour;

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

public class Glimmerscale {
	public static final Logger logger = Logger.getLogger(Glimmerscale.class.getName());
	public static int templateId;
	private static final String NAME = "glimmerscale";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("item.mod.glimmerscale.scale");
			itemBuilder.name(NAME, "glimmerscales", "Legendary scales, made from the careful combination of dragon scales and glimmersteel.");
			itemBuilder.descriptions("excellent", "good", "ok", "poor");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_BULK,
					ItemTypes.ITEM_TYPE_COMBINE,
					ItemTypes.ITEM_TYPE_NOT_MISSION
			});
			itemBuilder.imageNumber((short) 554);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(10, 30, 30);
			itemBuilder.primarySkill(-10);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.resource.scales.dragon.");
			itemBuilder.difficulty(60.0f);
			itemBuilder.weightGrams(400);
			itemBuilder.material(Materials.MATERIAL_UNDEFINED);
			itemBuilder.value(200000);

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
		if(templateId > 0){
			try {
				logger.info("Creating " + NAME + " creation entry, ID = " + templateId);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_METALLURGY, ItemList.dragonScale, ItemList.glimmerSteelBar,
						templateId, true, true, 0.0f, true, false, CreationCategories.RESOURCES);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for " + NAME, e);
			}
		}else{
			logger.log(Level.SEVERE, NAME + " does not have a valid template ID on creation entry.");
		}
	}
}
