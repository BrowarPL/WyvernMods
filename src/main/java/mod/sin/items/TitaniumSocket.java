package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class TitaniumSocket {
	private static final Logger logger = Logger.getLogger(TitaniumSocket.class.getName());
	public static int templateId;
	private static final String NAME = "titanium socket";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.socket.titanium");
			itemBuilder.name(NAME, "titanium sockets", "A socket for a gem, designed for insertion into an item.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_METAL
			});
			itemBuilder.imageNumber((short) 250);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(3, 3, 3);
			itemBuilder.primarySkill(-10);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.resource.lump.");
			itemBuilder.difficulty(40.0f);
			itemBuilder.weightGrams(1000);
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
				CreationEntryCreator.createSimpleEntry(SkillList.SMITHING_GOLDSMITHING, ItemList.anvilSmall, TitaniumLump.templateId,
						templateId, false, true, 0.0f, true, false, CreationCategories.JEWELRY);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for "+NAME, e);
			}
		}else{
			logger.log(Level.WARNING, NAME+" does not have a template ID on creation entry.");
		}
	}
}
