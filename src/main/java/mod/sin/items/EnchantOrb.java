package mod.sin.items;

import java.io.IOException;
import java.util.logging.Logger;

import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;

public class EnchantOrb {
	public static final Logger logger = Logger.getLogger(EnchantOrb.class.getName());
	public static int templateId;

	public void createTemplate() throws IOException{
		String name = "enchant orb";
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.enchantorb");
		itemBuilder.name(name, "enchant orbs", "It shimmers lightly, the magic inside waiting for a proper vessel.");
		itemBuilder.descriptions("vibrant", "glowing", "faint", "empty");
		itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_MAGIC,
				ItemTypes.ITEM_TYPE_INDESTRUCTIBLE
		});
		itemBuilder.imageNumber((short) 819);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(1, 1, 1);
		itemBuilder.primarySkill((int) MiscConstants.NOID);
		itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
		itemBuilder.modelName("model.artifact.orbdoom");
		itemBuilder.difficulty(5.0f);
		itemBuilder.weightGrams(500);
		itemBuilder.material(Materials.MATERIAL_CRYSTAL);
		itemBuilder.value(50000);
		itemBuilder.isTraded(true);

		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}
}
