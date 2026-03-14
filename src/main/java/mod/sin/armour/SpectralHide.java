package mod.sin.armour;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;

public class SpectralHide {
	public static final Logger logger = Logger.getLogger(SpectralHide.class.getName());
	public static int templateId;
	public void createTemplate() throws IOException{
		final String NAME = "spectral hide";
		try {
			ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("wyvern.spectral.hide");
			itemBuilder.name(NAME, "spectral hides", "Lightweight and transparent, this ethereal leather comes from another plane of existence. It is stronger than natural drake hide.");
			itemBuilder.descriptions("excellent", "good", "ok", "poor");
			itemBuilder.itemTypes(new short[]{
					ItemTypes.ITEM_TYPE_LEATHER,
					ItemTypes.ITEM_TYPE_HASDATA,
					ItemTypes.ITEM_TYPE_COMBINE
			});
			itemBuilder.imageNumber((short) 602);
			itemBuilder.behaviourType((short) 1);
			itemBuilder.combatDamage(0);
			itemBuilder.decayTime(Long.MAX_VALUE);
			itemBuilder.dimensions(10, 30, 30);
			itemBuilder.primarySkill((int) MiscConstants.NOID);
			itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			itemBuilder.modelName("model.resource.leather.dragon.");
			itemBuilder.difficulty(20.0f);
			itemBuilder.weightGrams(200);
			itemBuilder.material(Materials.MATERIAL_LEATHER);
			itemBuilder.value(200000);

			ItemTemplate template = itemBuilder.build();
			if (template == null) {
				logger.log(Level.SEVERE, "Failed to create item template for " + NAME);
				throw new IOException("Failed to create item template for " + NAME);
			}
			templateId = template.getTemplateId();
			logger.info(NAME + " TemplateID: " + templateId);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating template for spectral hide", e);
			throw e;
		}
	}
}
