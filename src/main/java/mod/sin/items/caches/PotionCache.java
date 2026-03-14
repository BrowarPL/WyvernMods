package mod.sin.items.caches;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class PotionCache {
	public static final Logger logger = Logger.getLogger(PotionCache.class.getName());
	public static int templateId;
	private static final String NAME = "potion cache";

	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.cache.potion");
			itemBuilder.name(NAME, "potion caches", "A cache of potions.");
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
