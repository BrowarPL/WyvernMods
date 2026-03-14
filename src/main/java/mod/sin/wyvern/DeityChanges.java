package mod.sin.wyvern;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

import java.util.logging.Logger;

public class DeityChanges {
    public static final Logger logger = Logger.getLogger(DeityChanges.class.getName());

    private static Deity getDeitySafe(int deityId) {
        return Deities.getDeity(deityId);
    }

    public static void onServerStarted(){
        Deity thelastdab = getDeitySafe(101);
        if(thelastdab != null){
            thelastdab.setTemplateDeity(Deities.DEITY_MAGRANON);
            thelastdab.setMountainGod(true);
            thelastdab.setHateGod(false);
            thelastdab.setMetalAffinity(true);
            thelastdab.setDeathProtector(true);
            thelastdab.setWarrior(true);
            thelastdab.setLearner(false);
            thelastdab.setRepairer(false);
            thelastdab.setBefriendCreature(false);
            thelastdab.setHealer(false);
            thelastdab.setClayAffinity(false);
            thelastdab.setWaterGod(false);
        }

        Deity reevi = getDeitySafe(102);
        if(reevi != null){
            reevi.setTemplateDeity(Deities.DEITY_MAGRANON);
            reevi.setMountainGod(true);
            reevi.setWaterGod(false);
        }
    }
}