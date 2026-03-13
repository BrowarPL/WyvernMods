package mod.sin.armour;

import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class SpectralGlove {
	public static final Logger logger = Logger.getLogger(SpectralGlove.class.getName());
	public static int templateId;
	private final String name = "spectral glove";
	public void createTemplate() throws IOException{
		ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.spectral.glove");
		itemBuilder.name(name, "spectral gloves", "A spectral glove.");
		itemBuilder.itemTypes(new short[]{ // {108, 44, 23, 4, 99} - Drake hide jacket
				ItemTypes.ITEM_TYPE_NAMED,
				ItemTypes.ITEM_TYPE_REPAIRABLE,
				ItemTypes.ITEM_TYPE_LEATHER,
				ItemTypes.ITEM_TYPE_ARMOUR,
				ItemTypes.ITEM_TYPE_DRAGONARMOUR
		});
		itemBuilder.imageNumber((short) 1064);
		itemBuilder.behaviourType((short) 1);
		itemBuilder.combatDamage(0);
		itemBuilder.decayTime(Long.MAX_VALUE);
		itemBuilder.dimensions(2, 40, 40);
		itemBuilder.primarySkill(-10);
		itemBuilder.bodySpaces(new byte[]{13, 14});
		itemBuilder.modelName("model.armour.hand.dragon.");
		itemBuilder.difficulty(68.0f);
		itemBuilder.weightGrams(250);
		itemBuilder.material(Materials.MATERIAL_LEATHER);
		itemBuilder.value(1000000);

		ItemTemplate template = itemBuilder.build();
		templateId = template.getTemplateId();
		logger.info(name+" TemplateID: "+templateId);
	}

	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			logger.info("Creating "+name+" creation entry, ID = "+templateId);
			CreationEntryCreator.createSimpleEntry(SkillList.LEATHERWORKING, ItemList.needleIron, SpectralHide.templateId,
					templateId, false, true, 0.0f, false, false, CreationCategories.ARMOUR);
			CreationEntryCreator.createSimpleEntry(SkillList.LEATHERWORKING, ItemList.needleCopper, SpectralHide.templateId,
					templateId, false, true, 0.0f, false, false, CreationCategories.ARMOUR);
		}else{
			logger.info(name+" does not have a template ID on creation entry.");
		}
	}
}
