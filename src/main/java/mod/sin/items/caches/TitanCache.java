package mod.sin.items.caches;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TitanCache {
	public static final Logger logger = Logger.getLogger(TitanCache.class.getName());
	public static int templateId;
	private static final String NAME = "titan cache";

	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.cache.titan");
			itemBuilder.name(NAME, "titan caches", "A cache of treasures from a titan.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_MAGIC,
					ItemTypes.ITEM_TYPE_FULLPRICE,
					ItemTypes.ITEM_TYPE_NOSELLBACK,
					ItemTypes.ITEM_TYPE_ALWAYS_BANKABLE
			});
			itemBuilder.imageNumber((short) 243);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(1, 1, 1);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.container.giftbox.");
			itemBuilder.difficulty(5.0f);
			itemBuilder.weightGrams(500);
			itemBuilder.material(Materials.MATERIAL_GOLD);
			itemBuilder.value(1000);
			itemBuilder.isTraded(true);

			ItemTemplate template = itemBuilder.build();
			if (template != null) {
				templateId = template.getTemplateId();
				logger.info(NAME + " TemplateID: " + templateId);
			} else {
				logger.log(Level.SEVERE, "Failed to create item template for " + NAME);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating item template for " + NAME, e);
			throw e;
		}
	}
}
