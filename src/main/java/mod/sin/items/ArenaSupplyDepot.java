package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class ArenaSupplyDepot {
	public static final Logger logger = Logger.getLogger(ArenaSupplyDepot.class.getName());
	public static int templateId;
	private static final String NAME = "arena depot";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.arena.depot");
			itemBuilder.name(NAME, "arena depots", "Contains a valuable cache of treasures.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_WOOD,
					ItemTypes.ITEM_TYPE_NOTAKE,
					ItemTypes.ITEM_TYPE_LOCKABLE,
					ItemTypes.ITEM_TYPE_DECORATION,
					ItemTypes.ITEM_TYPE_ONE_PER_TILE,
					ItemTypes.ITEM_TYPE_OWNER_TURNABLE,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_MISSION,
					ItemTypes.ITEM_TYPE_PLANTABLE
			});
			itemBuilder.imageNumber((short) 462);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(300, 300, 300);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.structure.war.supplydepot.2.0.");
			itemBuilder.difficulty(5.0f);
			itemBuilder.weightGrams(50000);
			itemBuilder.material(Materials.MATERIAL_WOOD_BIRCH);
			itemBuilder.value(5000);

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
