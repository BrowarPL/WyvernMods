package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.CreationRequirement;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class MassStorageUnit {
	public static final Logger logger = Logger.getLogger(MassStorageUnit.class.getName());
	public static int templateId;
	private static final String NAME = "mass storage unit";

	public void createTemplate() throws IOException{
		try {
			ModVehicleBehaviours.init();
			ItemTemplateBuilder builder = new ItemTemplateBuilder("mod.item.mass.storage");
			builder.name(NAME, "mass storage units", "A massive storage unit able to be loaded with containers.");
			builder.descriptions("almost full", "somewhat occupied", "half-full", "almost empty");
			builder.itemTypes(new short[] {
					ItemTypes.ITEM_TYPE_WOOD,
					ItemTypes.ITEM_TYPE_NOTAKE,
					ItemTypes.ITEM_TYPE_REPAIRABLE,
					ItemTypes.ITEM_TYPE_TURNABLE,
					ItemTypes.ITEM_TYPE_DECORATION,
					ItemTypes.ITEM_TYPE_DESTROYABLE,
					ItemTypes.ITEM_TYPE_ONE_PER_TILE,
					ItemTypes.ITEM_TYPE_LOCKABLE,
					ItemTypes.ITEM_TYPE_HOLLOW,
					ItemTypes.ITEM_TYPE_VEHICLE,
					ItemTypes.ITEM_TYPE_IMPROVEITEM,
					ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
					ItemTypes.ITEM_TYPE_USES_SPECIFIED_CONTAINER_VOLUME,
					ItemTypes.ITEM_TYPE_OWNER_TURNABLE,
					ItemTypes.ITEM_TYPE_CART,
					ItemTypes.ITEM_TYPE_OWNER_MOVEABLE
			});
			builder.imageNumber((short) 60);
			builder.behaviourType((short) 41);
			builder.combatDamage(0);
			builder.decayTime(9072000L);
			builder.dimensions(400, 300, 1000);
			builder.primarySkill(-10);
			builder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
			builder.modelName("model.furniture.wooden.storageunit.");

			builder.difficulty(80.0F);
			builder.weightGrams(300000);
			builder.material(Materials.MATERIAL_WOOD_BIRCH);

			ItemTemplate template = builder.build();
			if (template != null) {
				templateId = template.getTemplateId();
				template.setContainerSize(300, 300, 600);

				try {
					MassStorageBehaviour massStorageBehaviour = new MassStorageBehaviour();
					ModVehicleBehaviours.addItemVehicle(templateId, massStorageBehaviour);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error setting up vehicle behaviour for "+NAME, e);
				}

				logger.info(NAME+" TemplateID: "+templateId);
			} else {
				logger.log(Level.SEVERE, "Failed to create item template for "+NAME);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error creating item template for "+NAME, e);
			throw e;
		}
	}
	public void initCreationEntry(){
		logger.info("initCreationEntry()");
		if(templateId > 0){
			try {
				logger.info("Creating "+NAME+" creation entry, ID = "+templateId);
				AdvancedCreationEntry massStorage = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY_FINE,
						ItemList.woodBeam, ItemList.woodBeam, templateId,
						false, false, 0.0F, true, true, 0, 70.0D, CreationCategories.STORAGE);

				if (massStorage != null) {
					massStorage.addRequirement(new CreationRequirement(1, ItemList.plank, 500, true));
					massStorage.addRequirement(new CreationRequirement(2, ItemList.shaft, 200, true));
					massStorage.addRequirement(new CreationRequirement(3, ItemList.ironBand, 50, true));
					massStorage.addRequirement(new CreationRequirement(4, ItemList.nailsIronLarge, 100, true));
				} else {
					logger.log(Level.SEVERE, "Failed to create advanced creation entry for "+NAME);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating creation entry for "+NAME, e);
			}
		} else {
			logger.log(Level.WARNING, NAME+" does not have a template ID on creation entry.");
		}
	}
}
