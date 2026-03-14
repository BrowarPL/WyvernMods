package mod.sin.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SorceryFragment {
	public static final Logger logger = Logger.getLogger(SorceryFragment.class.getName());
	public static int templateId;
	private static final String NAME = "sorcery fragment [1/10]";

	public void createTemplate() throws IOException{
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.fragment.sorcery");
			itemBuilder.name(NAME, "sorcery fragments", "A scrap of a tome.");
			itemBuilder.itemTypes(new short[]{
				ItemTypes.ITEM_TYPE_MAGIC,
				ItemTypes.ITEM_TYPE_FULLPRICE,
				ItemTypes.ITEM_TYPE_NOSELLBACK,
				ItemTypes.ITEM_TYPE_SERVERBOUND,
				ItemTypes.ITEM_TYPE_ARTIFACT
			});
			itemBuilder.imageNumber((short) 331);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(1, 1, 1);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.artifact.scrollbind.paper.");
			itemBuilder.difficulty(5.0f);
			itemBuilder.weightGrams(250);
			itemBuilder.material(Materials.MATERIAL_CRYSTAL);
			itemBuilder.value(5000);
			itemBuilder.isTraded(true);

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
