package mod.sin.weapons.titan;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class WilhelmsWrath {
	public static final Logger logger = Logger.getLogger(WilhelmsWrath.class.getName());
	public static int templateId;
	private static final String NAME = "Wilhelm's Wrath";

	public void createTemplate() throws IOException {
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.titan.wilhelm.wrath");
			itemBuilder.name(NAME, "Wilhelm's Wraths", "A magnificent one-handed scythe, hand-crafted by Libila for her favoured champion, Wilhelm. To achieve it's full potential, one must wield it in combat.");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_NAMED,
					ItemTypes.ITEM_TYPE_METAL,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_WEAPON,
					ItemTypes.ITEM_TYPE_WEAPON_SLASH
			});
			itemBuilder.imageNumber((short) 753);
			itemBuilder.behaviourType((short) 35);
			itemBuilder.combatDamage(40);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(5, 10, 80);
			itemBuilder.primarySkill(SkillList.SCYTHE);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.weapon.scythe.");
			itemBuilder.difficulty(90.0f);
			itemBuilder.weightGrams(1000);
			itemBuilder.material(Materials.MATERIAL_ADAMANTINE);
			itemBuilder.value(1000000);

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
