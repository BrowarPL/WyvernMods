package mod.sin.items;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.CreationCategories;
import com.wurmonline.server.items.CreationEntryCreator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.skills.SkillList;

public class SkeletonDecoration {
    public static final Logger logger = Logger.getLogger(SkeletonDecoration.class.getName());
    public static int templateId;
    private static final String NAME = "skeleton";
    public void createTemplate() throws IOException{
        try {
            ItemTemplateBuilder itemBuilder = new ItemTemplateBuilder("mod.item.skeleton");
            itemBuilder.name(NAME, "skeletons", "A skeleton.");
            itemBuilder.itemTypes(new short[]{
                    ItemTypes.ITEM_TYPE_NAMED,
                    ItemTypes.ITEM_TYPE_OWNER_DESTROYABLE,
                    ItemTypes.ITEM_TYPE_DESTROYABLE,
                    ItemTypes.ITEM_TYPE_TURNABLE,
                    ItemTypes.ITEM_TYPE_DECORATION,
                    ItemTypes.ITEM_TYPE_NOT_MISSION,
                    ItemTypes.ITEM_TYPE_REPAIRABLE,
                    ItemTypes.ITEM_TYPE_COLORABLE
            });
            itemBuilder.imageNumber((short) 40);
            itemBuilder.behaviourType((short) 1);
            itemBuilder.combatDamage(0);
            itemBuilder.decayTime(Long.MAX_VALUE);
            itemBuilder.dimensions(20, 50, 200);
            itemBuilder.primarySkill(SkillList.BUTCHERING);
            itemBuilder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemBuilder.modelName("model.corpse.human.butchered.");
            itemBuilder.difficulty(90.0f);
            itemBuilder.weightGrams(50000);
            itemBuilder.material(Materials.MATERIAL_FLESH);
            itemBuilder.value(1000);

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

    public void initCreationEntry(){
        logger.info("initCreationEntry()");
        if(templateId > 0){
            try {
                logger.info("Creating "+NAME+" creation entry, ID = "+templateId);
                CreationEntryCreator.createSimpleEntry(SkillList.BUTCHERING, ItemList.knifeButchering, 22760,
                        templateId, false, true, 0f, false, false, CreationCategories.DECORATION);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error creating creation entry for "+NAME, e);
            }
        }else{
            logger.log(Level.WARNING, NAME+" does not have a template ID on creation entry.");
        }
    }
}
