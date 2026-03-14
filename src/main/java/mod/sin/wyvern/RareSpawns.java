package mod.sin.wyvern;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import mod.sin.creatures.Reaper;
import mod.sin.creatures.SpectralDrake;
import mod.sin.creatures.WyvernBlue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RareSpawns {
    public static final Logger logger = Logger.getLogger(RareSpawns.class.getName());

    public static boolean isRareCreature(int templateId){
        return templateId == SpectralDrake.templateId || templateId == Reaper.templateId;
    }

    public static boolean isRareCreature(Creature creature){
        return creature != null && isRareCreature(creature.getTemplate().getTemplateId());
    }

    public static void spawnRandomLocationCreature(int templateId){
        final int maxAttempts = 10000;
        int spawnX = 2048;
        int spawnY = 2048;
        boolean found = false;

        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            int x = Server.rand.nextInt(Server.surfaceMesh.getSize());
            int y = Server.rand.nextInt(Server.surfaceMesh.getSize());
            short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
            if(height > 0 && height < 1000 && Creature.getTileSteepness(x, y, true)[1] < 30){
                Village v = Villages.getVillage(x, y, true);
                for (int vx = -50; vx < 50 && v == null; vx += 5) {
                    for (int vy = -50; vy < 50 && (v = Villages.getVillage(x + vx, y + vy, true)) == null; vy += 5) {
                    }
                }
                if(v != null){
                    continue;
                }
                spawnX = x * 4;
                spawnY = y * 4;
                found = true;
                break;
            }
        }

        if (!found) {
            logger.warning("Failed to find a random spawn location for rare creature " + templateId + ". Using fallback coordinates.");
        }

        try {
            logger.info("Spawning new rare creature at " + (spawnX * 0.25f) + ", " + (spawnY * 0.25f));
            Creature.doNew(templateId, spawnX, spawnY, 360f * Server.rand.nextFloat(), 0, "", Server.rand.nextBoolean() ? (byte) 0 : (byte) 1);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create Rare Spawn.", e);
        }
    }

    public static ArrayList<Creature> rares = new ArrayList<>();

    public static void pollRareSpawns(){
        Creature[] creatures = Creatures.getInstance().getCreatures();
        for(Creature creature : creatures){
            if(isRareCreature(creature) && !rares.contains(creature)){
                rares.add(creature);
                logger.info("Existing rare spawn identified (" + creature.getName() + "). Adding to rares list.");
            }
        }

        for(Iterator<Creature> it = rares.iterator(); it.hasNext(); ){
            Creature current = it.next();
            if(current == null || current.isDead() || !isRareCreature(current)){
                if (current != null) {
                    logger.info("Rare spawn was found invalid or dead (" + current.getName() + "). Removing from rares list.");
                } else {
                    logger.info("Null rare spawn reference found. Removing from rares list.");
                }
                it.remove();
            }
        }

        if(rares.isEmpty()){
            logger.info("No rare spawn was found. Spawning a new one.");
            int[] rareTemplates = {Reaper.templateId, SpectralDrake.templateId};
            int rareTemplateId = rareTemplates[Server.rand.nextInt(rareTemplates.length)];
            spawnRandomLocationCreature(rareTemplateId);

            if(WyvernBlue.templateId > 0) {
                spawnRandomLocationCreature(WyvernBlue.templateId);
            }
        }
    }
}