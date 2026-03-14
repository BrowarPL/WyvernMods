package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class FriyanTablet {
	public static final Logger logger = Logger.getLogger(FriyanTablet.class.getName());
	public static int templateId;
	private static final String NAME = "Tablet of Friyan";
	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.tablet.friyan");
			itemBuilder.name(NAME, "tablets of friyan", "Once a great scholar and sorceress, Friyan's faith had reached the zenith. While in this world, she wrote her knowledge in tablets like these. Perhaps you may learn more of the gods from it...");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_STONE,
					ItemTypes.ITEM_TYPE_OUTSIDE_ONLY,
					ItemTypes.ITEM_TYPE_NOTAKE,
					ItemTypes.ITEM_TYPE_DECORATION,
					ItemTypes.ITEM_TYPE_INDESTRUCTIBLE,
					ItemTypes.ITEM_TYPE_USE_GROUND_ONLY,
					ItemTypes.ITEM_TYPE_NOMOVE,
					ItemTypes.ITEM_TYPE_NEVER_SHOW_CREATION_WINDOW_OPTION,
					ItemTypes.ITEM_TYPE_NOT_MISSION
			});
			itemBuilder.imageNumber((short) 60);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(500, 500, 1000);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.structure.portal.10.");
			itemBuilder.difficulty(99.0f);
			itemBuilder.weightGrams(2000000);
			itemBuilder.material(Materials.MATERIAL_STONE);
			itemBuilder.value(10000);

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
}
