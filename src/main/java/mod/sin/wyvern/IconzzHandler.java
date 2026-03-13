package mod.sin.wyvern;

import org.gotti.wurmunlimited.modloader.interfaces.ModEntry;
import org.tyoda.wurm.Iconzz.Iconzz;

import java.util.logging.Logger;

public class IconzzHandler {
    public static final Logger logger = Logger.getLogger(IconzzHandler.class.getName());
    private static boolean foundIconzz = false;
    public static short knucklesIconId = (short) 4; // Hand Icon
    public static short glimmerscaleBootId = (short) 1025; // Dragon scale
    public static short glimmerscaleHelmetId = (short) 968; // Dragon scale
    public static short glimmerscaleGloveId = (short) 1024; // Dragon scale
    public static short glimmerscaleHoseId = (short) 1021; // Dragon scale
    public static short glimmerscaleSleeveId = (short) 1022; // Dragon scale
    public static short glimmerscaleVestId = (short) 1020; // Dragon scale
    public static void modInitialized(ModEntry<?> modEntry){
        // logger.info(modEntry.getName() + " == Iconzz: "+modEntry.getName().equals("Iconzz"));
        if(modEntry.getName().equals("Iconzz")){
            foundIconzz = true;
            Iconzz iconzz = Iconzz.getInstance();
            if(WyvernMods.useKnucklesIcon) {
                logger.info("Registering knuckles");
                knucklesIconId = iconzz.addIcon("mod.sin.wyvern.knuckles", "mods/WyvernMods/icons/knuckles.png");
            }

            if(WyvernMods.useGlimmerscaleIcons){
                glimmerscaleBootId   = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.boot", "mods/WyvernMods/icons/glimmerscale_boot.png");
                glimmerscaleHelmetId = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.helmet", "mods/WyvernMods/icons/glimmerscale_helmet.png");
                glimmerscaleGloveId  = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.glove", "mods/WyvernMods/icons/glimmerscale_glove.png");
                glimmerscaleHoseId   = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.hose", "mods/WyvernMods/icons/glimmerscale_hose.png");
                glimmerscaleSleeveId = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.sleeve", "mods/WyvernMods/icons/glimmerscale_sleeve.png");
                glimmerscaleVestId   = iconzz.addIcon("mod.sin.wyvern.armour.glimmerscale.vest", "mods/WyvernMods/icons/glimmerscale_vest.png");
            }
        }
    }

    public static void onItemTemplatesCreated(){
        if(!foundIconzz){
            logger.info("Iconzz mod not found. Not using custom icons.");
        }
    }
}
