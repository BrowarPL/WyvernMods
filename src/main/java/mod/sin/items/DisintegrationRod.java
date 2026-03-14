package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class DisintegrationRod {
	public static final Logger logger = Logger.getLogger(DisintegrationRod.class.getName());
	public static int templateId;
	private static final String NAME = "Rod of Disintegration";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("item.mod.rod.disintegration");
			itemBuilder.name(NAME, "rods of disintegration", "A rod designed for removal of ore veins and cave walls.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_FULLPRICE,
					ItemTypes.ITEM_TYPE_NOSELLBACK,
					ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
			});
			itemBuilder.imageNumber((short) 1259);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 60);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.tool.rodtrans.");
			itemBuilder.difficulty(300.0f);
			itemBuilder.weightGrams(1000);
			itemBuilder.material(Materials.MATERIAL_STONE);
			itemBuilder.value(20000);
			itemBuilder.isTraded(true);

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
}
