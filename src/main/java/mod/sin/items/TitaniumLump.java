package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TitaniumLump {
	public static final Logger logger = Logger.getLogger(TitaniumLump.class.getName());
	public static int templateId;
	private static final String NAME = "lump, titanium";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.lump.titanium");
			itemBuilder.name(NAME, "titanium lumps", "A lightweight lump of glistening titanium.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_BULK,
					ItemTypes.ITEM_TYPE_COMBINE
			});
			itemBuilder.imageNumber((short) 638);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(3, 3, 3);
			itemBuilder.primarySkill(-10);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.resource.lump.");
			itemBuilder.difficulty(40.0f);
			itemBuilder.weightGrams(400);
			itemBuilder.material(Materials.MATERIAL_UNDEFINED);
			itemBuilder.value(200);

			ItemTemplate template = itemBuilder.build();
			if (template != null) {
				templateId = template.getTemplateId();
				logger.info(NAME+" TemplateID: "+templateId);
			} else {
				logger.log(Level.SEVERE, "Failed to create item template for "+NAME);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating item template for "+NAME, e);
			throw e;
		}
	}
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			try {
				logger.info("Creating "+NAME+" creation entry, ID = "+templateId);
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_METALLURGY, ItemList.adamantineBar, ItemList.glimmerSteelBar,
						templateId, true, true, 0.0f, true, false, CreationCategories.RESOURCES);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for "+NAME, e);
			}
		}else{
			logger.log(Level.WARNING, NAME+" does not have a template ID on creation entry.");
		}
	}
}
