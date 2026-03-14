package mod.sin.wyvern;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.creatures.titans.Ifrit;
import mod.sin.creatures.titans.IfritFiend;
import mod.sin.creatures.titans.IfritSpider;
import mod.sin.creatures.titans.Lilith;
import mod.sin.creatures.titans.LilithWraith;
import mod.sin.creatures.titans.LilithZombie;
import mod.sin.items.caches.ArtifactCache;
import mod.sin.items.caches.TreasureMapCache;
import mod.sin.lib.Util;
import mod.sin.wyvern.util.ItemUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Titans {
    public static final Logger logger = Logger.getLogger(Titans.class.getName());
    protected static boolean initializedTitans = false;

    public static void updateLastSpawnedTitan(){
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement("UPDATE ObjectiveTimers SET TIMER = ? WHERE ID = ?");
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, "TITAN");
            int updated = ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = null;
            if (updated == 0) {
                ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES (?, ?)");
                ps.setString(1, "TITAN");
                ps.setLong(2, System.currentTimeMillis());
                ps.executeUpdate();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    public static void initializeTitanTimer(){
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement("SELECT * FROM ObjectiveTimers WHERE ID = ?");
            ps.setString(1, "TITAN");
            rs = ps.executeQuery();
            if(rs.next()) {
                lastSpawnedTitan = rs.getLong("TIMER");
            } else {
                lastSpawnedTitan = 0;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        logger.info("Initialized Titan timer: " + lastSpawnedTitan);
        initializedTitans = true;
    }

    public static void addTitanLoot(Creature titan){
        Item inv = titan.getInventory();
        int i = 0;
        while(i < 3) {
            Item sorcery = ItemUtil.createRandomSorcery((byte) 1);
            if (sorcery != null) {
                inv.insertItem(sorcery, true);
            }
            ++i;
        }

        try {
            Item cache = ItemFactory.createItem(Server.rand.nextBoolean() ? TreasureMapCache.templateId : ArtifactCache.templateId, 90f + (10f * Server.rand.nextFloat()), titan.getName());
            inv.insertItem(cache, true);
        } catch (FailedException | NoSuchTemplateException e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    public static void checkDestroyMineDoor(Creature titan, int x, int y){
        int tile = Server.surfaceMesh.getTile(x, y);
        if(Tiles.isMineDoor(Tiles.decodeType(tile))){
            if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)), Tiles.Tile.TILE_HOLE.id, (byte) 0);
            } else {
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)), Tiles.Tile.TILE_ROCK.id, (byte) 0);
            }
            Players.getInstance().sendChangedTile(x, y, true, true);
            MineDoorPermission.deleteMineDoor(x, y);
            Server.getInstance().broadCastAction(titan.getName() + "'s ability destroys a mine door!", titan, 50);
        }
    }

    public static Creature[] getUndergroundCreatures(int x, int y){
        VolaTile tCave = Zones.getOrCreateTile(x, y, false);
        if(tCave == null){
            return null;
        }
        int tileCave = Server.caveMesh.getTile(x, y);
        byte typeCave = Tiles.decodeType(tileCave);
        if(typeCave != Tiles.Tile.TILE_CAVE.id && typeCave != Tiles.Tile.TILE_CAVE_EXIT.id && typeCave != Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id && typeCave != Tiles.Tile.TILE_CAVE_PREPATED_FLOOR_REINFORCED.id){
            return null;
        }
        return tCave.getCreatures();
    }

    public static boolean isTitan(int templateId){
        return templateId == Lilith.templateId
                || templateId == Ifrit.templateId;
    }

    public static boolean isTitan(Creature creature){
        return isTitan(creature.getTemplate().getTemplateId());
    }

    public static boolean isTitanMinion(int templateId){
        return templateId == LilithWraith.templateId
                || templateId == LilithZombie.templateId
                || templateId == IfritFiend.templateId
                || templateId == IfritSpider.templateId;
    }

    public static boolean isTitanMinion(Creature creature){
        return isTitanMinion(creature.getTemplate().getTemplateId());
    }

    public static void lilithMyceliumVoidAttack(Creature titan, Creature lCret, int tilex, int tiley){
        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)){
            return;
        }
        lCret.addWoundOfType(lCret, Wound.TYPE_INFECTION, 1, true, 1.0f, true, 50000f, 0f, 0f, true, true);
    }

    public static void ifritMassIncinerateAttack(Creature titan, Creature lCret){
        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)){
            return;
        }
        SpellEffect eff;
        SpellEffects effs = lCret.getSpellEffects();
        if (effs == null) {
            effs = lCret.createSpellEffects();
        }
        eff = effs.getSpellEffect((byte) 94);
        if (eff == null) {
            lCret.getCommunicator().sendAlertServerMessage("You are engulfed by the flames of Ifrit!", (byte) 4);
            eff = new SpellEffect(lCret.getWurmId(), (byte) 94, 80f, 180, (byte) 9, (byte) 1, true);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(titan.getName() + " has engulfed " + lCret.getNameWithGenus() + " in flames!", titan, 50);
        } else {
            lCret.getCommunicator().sendAlertServerMessage("The heat around you increases. The pain is excruciating!", (byte) 4);
            eff.setPower(eff.getPower() + 200f);
            eff.setTimeleft(180);
            lCret.sendUpdateSpellEffect(eff);
            Server.getInstance().broadCastAction(titan.getName() + " has engulfed " + lCret.getNameWithGenus() + " in flames again, increasing the intensity!", titan, 50);
        }
    }

    public static void performAdvancedAbility(Creature titan, int range, int radius){
        int tilex = titan.getTileX();
        int tiley = titan.getTileY();
        if(titan.getTemplate().getTemplateId() == Lilith.templateId){
            int tarx = (tilex - range) + (Server.rand.nextInt(1 + (range * 2)));
            int tary = (tiley - range) + (Server.rand.nextInt(1 + (range * 2)));
            int sx = Zones.safeTileX(tarx - radius);
            int ex = Zones.safeTileX(tarx + radius);
            int sy = Zones.safeTileY(tary - radius);
            int ey = Zones.safeTileY(tary + radius);
            Zones.flash(tarx, tary, false);
            Server.getInstance().broadCastAction(titan.getName() + " casts Mycelium Void, turning the earth to fungus and pulling enemies to " + titan.getHimHerItString() + "!", titan, 50);
            for (int x = sx; x <= ex; ++x) {
                for (int y = sy; y <= ey; ++y) {
                    VolaTile t = Zones.getOrCreateTile(x, y, true);
                    if (t == null){
                        continue;
                    }
                    checkDestroyMineDoor(titan, x, y);
                    int tile = Server.surfaceMesh.getTile(x, y);
                    byte type = Tiles.decodeType(tile);
                    Tiles.Tile theTile = Tiles.getTile(type);
                    byte data = Tiles.decodeData(tile);
                    if (type == Tiles.Tile.TILE_FIELD.id
                            || type == Tiles.Tile.TILE_FIELD2.id
                            || type == Tiles.Tile.TILE_GRASS.id
                            || type == Tiles.Tile.TILE_REED.id
                            || type == Tiles.Tile.TILE_DIRT.id
                            || type == Tiles.Tile.TILE_LAWN.id
                            || type == Tiles.Tile.TILE_STEPPE.id
                            || theTile.isNormalTree()
                            || theTile.isEnchanted()
                            || theTile.isNormalBush()){
                        if (theTile.isNormalTree()) {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getTreeType(data).asMyceliumTree(), data);
                        } else if (theTile.isEnchantedTree()) {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getTreeType(data).asNormalTree(), data);
                        } else if (theTile.isNormalBush()) {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getBushType(data).asMyceliumBush(), data);
                        } else if (theTile.isEnchantedBush()) {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), theTile.getBushType(data).asNormalBush(), data);
                        } else if (type == Tiles.Tile.TILE_LAWN.id) {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM_LAWN.id, (byte) 0);
                        } else {
                            Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte) 0);
                        }
                        Players.getInstance().sendChangedTile(x, y, true, false);
                    }
                    Creature[] crets2 = t.getCreatures();
                    for (Creature lCret : crets2) {
                        lilithMyceliumVoidAttack(titan, lCret, tilex, tiley);
                    }
                    VolaTile tCave = Zones.getOrCreateTile(x, y, false);
                    if(tCave == null){
                        continue;
                    }
                    int tileCave = Server.caveMesh.getTile(x, y);
                    byte typeCave = Tiles.decodeType(tileCave);
                    if(typeCave != Tiles.Tile.TILE_CAVE.id && typeCave != Tiles.Tile.TILE_CAVE_EXIT.id && typeCave != Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id && typeCave != Tiles.Tile.TILE_CAVE_PREPATED_FLOOR_REINFORCED.id){
                        continue;
                    }
                    Creature[] crets3 = tCave.getCreatures();
                    for (Creature lCret : crets3) {
                        lilithMyceliumVoidAttack(titan, lCret, tilex, tiley);
                    }
                }
            }
        }else if(titan.getTemplate().getTemplateId() == Ifrit.templateId){
            int tarx = (tilex - range) + (Server.rand.nextInt(1 + (range * 2)));
            int tary = (tiley - range) + (Server.rand.nextInt(1 + (range * 2)));
            int sx = Zones.safeTileX(tarx - radius);
            int ex = Zones.safeTileX(tarx + radius);
            int sy = Zones.safeTileY(tary - radius);
            int ey = Zones.safeTileY(tary + radius);
            Zones.flash(tarx, tary, false);
            Server.getInstance().broadCastAction(titan.getName() + " casts Mass Incinerate, burning enemies near " + titan.getHimHerItString() + "!", titan, 50);
            for (int x = sx; x <= ex; ++x) {
                for (int y = sy; y <= ey; ++y) {
                    VolaTile t = Zones.getOrCreateTile(x, y, true);
                    if (t == null){
                        continue;
                    }
                    checkDestroyMineDoor(titan, x, y);
                    new AreaSpellEffect(titan.getWurmId(), x, y, titan.getLayer(), (byte) 35, System.currentTimeMillis() + 5000, 200.0f, titan.getLayer(), 0, true);
                    Creature[] crets2 = t.getCreatures();
                    for (Creature lCret : crets2) {
                        ifritMassIncinerateAttack(titan, lCret);
                    }
                    Creature[] undergroundCreatures = getUndergroundCreatures(x, y);
                    if(undergroundCreatures != null){
                        for(Creature lCret : undergroundCreatures){
                            ifritMassIncinerateAttack(titan, lCret);
                        }
                    }
                }
            }
        }
    }

    public static void lilithPainRainAttack(Creature titan, Creature lCret, VolaTile t){
        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)){
            return;
        }
        t.sendAttachCreatureEffect(lCret, (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
        try {
            if (lCret.addWoundOfType(titan, Wound.TYPE_INFECTION, lCret.getBody().getRandomWoundPos(), false, 1.0f, false, 25000.0 * (double)lCret.addSpellResistance((short) 448), 0f, 0f, true, true)){
                return;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "", e);
        }
        lCret.setTarget(titan.getWurmId(), false);
    }

    public static void performBasicAbility(Creature titan){
        int tilex = titan.getTileX();
        int tiley = titan.getTileY();
        if(titan.getTemplate().getTemplateId() == Lilith.templateId){
            int sx = Zones.safeTileX(tilex - 10);
            int sy = Zones.safeTileY(tiley - 10);
            int ex = Zones.safeTileX(tilex + 10);
            int ey = Zones.safeTileY(tiley + 10);
            int x, y;
            Server.getInstance().broadCastAction(titan.getName() + " casts Pain Rain, harming all around " + titan.getHimHerItString() + "!", titan, 50);
            for (x = sx; x <= ex; ++x) {
                for (y = sy; y <= ey; ++y) {
                    VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                    if (t == null){
                        continue;
                    }
                    Creature[] crets2 = t.getCreatures();
                    for (Creature lCret : crets2) {
                        lilithPainRainAttack(titan, lCret, t);
                    }
                    Creature[] undergroundCreatures = getUndergroundCreatures(x, y);
                    if(undergroundCreatures != null){
                        for(Creature lCret : undergroundCreatures){
                            lilithPainRainAttack(titan, lCret, t);
                        }
                    }
                }
            }
        }else if(titan.getTemplate().getTemplateId() == Ifrit.templateId){
            int sx = Zones.safeTileX(tilex - 10);
            int sy = Zones.safeTileY(tiley - 10);
            int ex = Zones.safeTileX(tilex + 10);
            int ey = Zones.safeTileY(tiley + 10);
            int x, y;
            ArrayList<Creature> targets = new ArrayList<>();
            for (x = sx; x <= ex; ++x) {
                for (y = sy; y <= ey; ++y) {
                    VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                    if (t == null){
                        continue;
                    }
                    Creature[] crets2 = t.getCreatures();
                    for (Creature lCret : crets2) {
                        if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)){
                            continue;
                        }
                        targets.add(lCret);
                    }
                    Creature[] undergroundCreatures = getUndergroundCreatures(x, y);
                    if(undergroundCreatures != null){
                        for(Creature lCret : undergroundCreatures){
                            if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)){
                                continue;
                            }
                            targets.add(lCret);
                        }
                    }
                }
            }
            if(!targets.isEmpty()){
                Creature target = null;
                for(Creature cret : targets){
                    if(cret.isHitched() || cret.isRidden()){
                        target = cret;
                        break;
                    }
                }
                if(target == null){
                    for(Creature cret : targets){
                        if(cret.isPlayer()){
                            target = cret;
                            break;
                        }
                    }
                }
                if(target == null) {
                    target = targets.get(Server.rand.nextInt(targets.size()));
                }
                if(target == null){
                    logger.info("Something went absolutely horribly wrong and there is no target for the Titan.");
                }
                int damage = target.getStatus().damage;
                int minhealth = 65435;
                float maxdam = (float)Math.max(0, minhealth - damage);
                if (maxdam > 500.0f) {
                    Server.getInstance().broadCastAction(titan.getName() + " picks a target at random and Smites " + target.getName() + "!", titan, 50);
                    target.getCommunicator().sendAlertServerMessage(titan.getName() + " smites you.", (byte) 4);
                    try {
                        target.addWoundOfType(titan, Wound.TYPE_BURN, target.getBody().getRandomWoundPos(), false, 1.0f, false, maxdam, 0f, 0f, true, true);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "", e);
                    }
                }
            }
        }
    }

    public static void summonChampions(Creature titan, int nums){
        int templateType = -10;
        String spellName = "";
        if(titan.getTemplate().getTemplateId() == Lilith.templateId){
            templateType = LilithWraith.templateId;
            spellName = "Raise Wraith";
        }else if(titan.getTemplate().getTemplateId() == Ifrit.templateId){
            templateType = IfritFiend.templateId;
            spellName = "Summon Fiend";
        }
        if(templateType == -10){
            logger.severe("[ERROR]: Template type not set in summonChampions()");
            return;
        }
        try {
            Server.getInstance().broadCastAction(titan.getName() + " casts " + spellName + ", calling champions to " + titan.getHimHerItString() + " aid!", titan, 50);
            for(int i = 0; i < nums; ++i){
                int tilex = ((titan.getTileX() * 4) + 3) - Server.rand.nextInt(7);
                int tiley = ((titan.getTileY() * 4) + 3) - Server.rand.nextInt(7);
                int sx = Zones.safeTileX(tilex - 2);
                int sy = Zones.safeTileY(tiley - 2);
                int ex = Zones.safeTileX(tilex + 2);
                int ey = Zones.safeTileY(tiley + 2);
                Creature target = null;
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                        if (t == null){
                            continue;
                        }
                        Creature[] crets2 = t.getCreatures();
                        for (Creature lCret : crets2) {
                            if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)) {
                                continue;
                            }
                            if(Server.rand.nextInt(3) == 0){
                                target = lCret;
                                break;
                            }
                        }
                        Creature[] undergroundCreatures = getUndergroundCreatures(x, y);
                        if(undergroundCreatures != null){
                            for(Creature lCret : undergroundCreatures){
                                if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)) {
                                    continue;
                                }
                                if(Server.rand.nextInt(3) == 0){
                                    target = lCret;
                                    break;
                                }
                            }
                        }
                        if(target != null){
                            break;
                        }
                    }
                    if(target != null){
                        break;
                    }
                }
                Creature champion = Creature.doNew(templateType, tilex, tiley, 360f * Server.rand.nextFloat(), titan.getLayer(), "", (byte)0);
                if(target != null){
                    champion.setOpponent(target);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    public static void summonMinions(Creature titan, int nums){
        int templateType = -10;
        String spellName = "";
        if(titan.getTemplate().getTemplateId() == Lilith.templateId){
            templateType = LilithZombie.templateId;
            spellName = "Raise Zombie";
        }else if(titan.getTemplate().getTemplateId() == Ifrit.templateId){
            templateType = IfritSpider.templateId;
            spellName = "Summon Spider";
        }
        if(templateType == -10){
            logger.severe("[ERROR]: Template type not set in summonMinions()");
            return;
        }
        try {
            Server.getInstance().broadCastAction(titan.getName() + " casts " + spellName + ", calling minions to " + titan.getHimHerItString() + " aid!", titan, 50);
            for(int i = 0; i < nums; ++i){
                int tilex = ((titan.getTileX() * 4) + 3) - Server.rand.nextInt(7);
                int tiley = ((titan.getTileY() * 4) + 3) - Server.rand.nextInt(7);
                int sx = Zones.safeTileX(tilex - 10);
                int sy = Zones.safeTileY(tiley - 10);
                int ex = Zones.safeTileX(tilex + 10);
                int ey = Zones.safeTileY(tiley + 10);
                Creature target = null;
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        VolaTile t = Zones.getTileOrNull(x, y, titan.isOnSurface());
                        if (t == null){
                            continue;
                        }
                        Creature[] crets2 = t.getCreatures();
                        for (Creature lCret : crets2) {
                            if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)) {
                                continue;
                            }
                            if(Server.rand.nextInt(3) == 0){
                                target = lCret;
                                break;
                            }
                        }
                        Creature[] undergroundCreatures = getUndergroundCreatures(x, y);
                        if(undergroundCreatures != null){
                            for(Creature lCret : undergroundCreatures){
                                if (lCret.isUnique() || lCret.isInvulnerable() || lCret == titan || isTitanMinion(lCret)) {
                                    continue;
                                }
                                if(Server.rand.nextInt(3) == 0){
                                    target = lCret;
                                    break;
                                }
                            }
                        }
                        if(target != null){
                            break;
                        }
                    }
                    if(target != null){
                        break;
                    }
                }
                Creature minion = Creature.doNew(templateType, tilex, tiley, 360f * Server.rand.nextFloat(), titan.getLayer(), "", (byte)0);
                if(target != null){
                    minion.setOpponent(target);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    public static HashMap<Creature, Integer> titanDamage = new HashMap<>();
    protected static HashMap<Long, Integer> titanAdvancedTimed = new HashMap<>();

    protected static void pollTimeMechanics(Creature titan){
        int currentDamage = titan.getStatus().damage;
        long wurmid = titan.getWurmId();
        if(currentDamage > 0) {
            if (titan.isOnSurface()) {
                int chance;
                int range;
                int radius;
                if (currentDamage > 52428) {
                    chance = 40;
                    range = 7;
                    radius = 2;
                } else if (currentDamage > 32767) {
                    chance = 45;
                    range = 5;
                    radius = 1;
                } else if (currentDamage > 16383) {
                    chance = 55;
                    range = 4;
                    radius = 1;
                } else {
                    chance = 60;
                    range = 3;
                    radius = 0;
                }
                if (titanAdvancedTimed.containsKey(wurmid)) {
                    int currentChance = Math.max(1, titanAdvancedTimed.get(wurmid));
                    boolean success = Server.rand.nextInt(currentChance) == 0;
                    if (success) {
                        performAdvancedAbility(titan, range, radius);
                        titanAdvancedTimed.put(wurmid, currentChance + chance - 1);
                    } else {
                        titanAdvancedTimed.put(wurmid, Math.max(1, currentChance - 1));
                    }
                } else {
                    titanAdvancedTimed.put(wurmid, chance);
                }
            } else if (Server.rand.nextInt(20) == 0) {
                performAdvancedAbility(titan, 3, 3);
            }
        }
    }

    protected static void pollDamageMechanics(Creature titan){
        int prevDamage = titanDamage.get(titan);
        int currentDamage = titan.getStatus().damage;
        if(currentDamage > 0 && prevDamage == 0){
            String msg = "<" + titan.getName() + " [100%]> Mere mortals dare to face me?";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
        }
        if(currentDamage > 8191 && prevDamage < 8191){
            String msg = "<" + titan.getName() + " [88%]> You actually think you can defeat me?";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
        }
        if(currentDamage > 16383 && prevDamage < 16383){
            String msg = "<" + titan.getName() + " [75%]> I am not alone.";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            summonMinions(titan, Server.rand.nextInt(2) + 2);
        }
        if(currentDamage > 26214 && prevDamage < 26214){
            String msg = "<" + titan.getName() + " [60%]> You will feel my wrath!";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            performBasicAbility(titan);
        }
        if(currentDamage > 32767 && prevDamage < 32767){
            String msg = "<" + titan.getName() + " [50%]> I've had enough of you. Minions, assemble!";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            summonMinions(titan, Server.rand.nextInt(4) + 4);
            performBasicAbility(titan);
        }
        if(currentDamage > 39321 && prevDamage < 39321){
            String msg = "<" + titan.getName() + " [40%]> Let's try something new, shall we?";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            performAdvancedAbility(titan, 7, 2);
            performAdvancedAbility(titan, 7, 2);
        }
        if(currentDamage > 45874 && prevDamage < 45874){
            String msg = "<" + titan.getName() + " [30%]> Perhaps minions aren't enough. Now, try my champions!";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            summonChampions(titan, Server.rand.nextInt(2) + 2);
            performBasicAbility(titan);
        }
        if(currentDamage > 52428 && prevDamage < 52428){
            String msg = "<" + titan.getName() + " [20%]> Enough! I will end you!";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            performBasicAbility(titan);
            performAdvancedAbility(titan, 5, 3);
        }
        if(currentDamage > 58981 && prevDamage < 58981){
            String msg = "<" + titan.getName() + " [10%]> Minions... Champions... Only one way to win a battle: An army!";
            MiscChanges.sendGlobalFreedomChat(titan, msg, 255, 105, 180);
            MiscChanges.sendServerTabMessage("titan", msg, 255, 105, 180);
            Zones.flash(titan.getTileX(), titan.getTileY(), false);
            summonMinions(titan, Server.rand.nextInt(5) + 7);
            summonChampions(titan, Server.rand.nextInt(3) + 3);
            performBasicAbility(titan);
            performAdvancedAbility(titan, 4, 3);
        }
        if(currentDamage > 16383 && Server.rand.nextInt(10) == 0){
            if(currentDamage > 45874){
                summonMinions(titan, Server.rand.nextInt(2) + 2);
            }else if(currentDamage > 32767){
                summonMinions(titan, Server.rand.nextInt(3) + 1);
            }else{
                summonMinions(titan, Server.rand.nextInt(2) + 1);
            }
        }
        if(currentDamage > 16383 && Server.rand.nextInt(15) == 0){
            if(currentDamage > 45874){
                if(Server.rand.nextInt(10) == 0){
                    performBasicAbility(titan);
                }
            }else if(currentDamage > 32767){
                if(Server.rand.nextInt(12) == 0){
                    performBasicAbility(titan);
                }
            }else{
                if(Server.rand.nextInt(10) == 0){
                    performBasicAbility(titan);
                }
            }
        }
        if(currentDamage > 58981 && Server.rand.nextInt(30) == 0){
            summonChampions(titan, 1);
        }
        titanDamage.put(titan, currentDamage);
    }

    public static void pollTitanRegeneration(){
        if(!titans.isEmpty()) {
            for (Creature cret : titans) {
                if (cret.getBody().isWounded()) {
                    Wounds tWounds = cret.getBody().getWounds();
                    int toHeal = 5;
                    Wound w = tWounds.getWounds()[Server.rand.nextInt(tWounds.getWounds().length)];
                    if (w.getSeverity() > toHeal) {
                        w.modifySeverity(-toHeal);
                        break;
                    } else {
                        w.heal();
                    }
                }
            }
        }
    }

    public static void pollTitan(Creature titan){
        if(titanDamage.containsKey(titan)){
            int prevDamage = titanDamage.get(titan);
            int currentDamage = titan.getStatus().damage;
            pollTimeMechanics(titan);
            if(currentDamage > prevDamage){
                pollDamageMechanics(titan);
            }
        }else{
            titanDamage.put(titan, titan.getStatus().damage);
        }
    }

    public static void pollTitans(){
        for(Creature c : titans){
            if(isTitan(c)){
                pollTitan(c);
            }
        }
        pollTitanRegeneration();
    }

    public static ArrayList<Creature> titans = new ArrayList<>();
    public static long lastSpawnedTitan = 0;

    public static void addTitan(Creature mob){
        if(isTitan(mob) && !titans.contains(mob)){
            titans.add(mob);
        }
    }

    public static void removeTitan(Creature mob){
        if(isTitan(mob)){
            titans.remove(mob);
            titanDamage.remove(mob);
            titanAdvancedTimed.remove(mob.getWurmId());
        }
    }

    public static void pollTitanSpawn(){
        if(!initializedTitans){
            return;
        }
        Creature[] crets = Creatures.getInstance().getCreatures();
        for(Creature c : crets){
            if(isTitan(c) && !titans.contains(c)){
                titans.add(c);
                logger.info("Existing titan identified (" + c.getName() + "). Adding to titan list.");
            }
        }
        for(Iterator<Creature> it = titans.iterator(); it.hasNext(); ){
            Creature current = it.next();
            if(current.isDead()){
                logger.info("Titan was found dead (" + current.getName() + "). Removing from titan list.");
                titanDamage.remove(current);
                titanAdvancedTimed.remove(current.getWurmId());
                it.remove();
            }
        }
        if(titans.isEmpty()){
            if(lastSpawnedTitan + WyvernMods.titanRespawnTime < System.currentTimeMillis()){
                logger.info("No Titan was found, and the timer has expired. Spawning a new one.");
                boolean found = false;
                int spawnX = 2048;
                int spawnY = 2048;
                while(!found){
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
                    }
                }
                int[] titanTemplates = {Lilith.templateId, Ifrit.templateId};
                try {
                    Creature.doNew(titanTemplates[Server.rand.nextInt(titanTemplates.length)], spawnX, spawnY, 360f * Server.rand.nextFloat(), 0, "", (byte)0);
                    lastSpawnedTitan = System.currentTimeMillis();
                    updateLastSpawnedTitan();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to create Titan.", e);
                }
            }
        }else{
            for(Creature c : titans){
                c.healRandomWound(1000);
            }
        }
    }

    public static void preInit(){
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            Class<Titans> thisClass = Titans.class;
            String replace;

            CtClass ctWound = classPool.get("com.wurmonline.server.bodys.Wound");

            if (WyvernMods.disableTitanNaturalRegeneration) {
                Util.setReason("Disable natural regeneration on titans.");
                replace = "if(!" + Titans.class.getName() + ".isTitan(this.creature)){"
                        + "  $_ = $proceed($$);"
                        + "}";
                Util.instrumentDeclared(thisClass, ctWound, "poll", "modifySeverity", replace);
                Util.setReason("Disable natural regeneration on titans.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkInfection", replace);
                Util.setReason("Disable natural regeneration on titans.");
                Util.instrumentDeclared(thisClass, ctWound, "poll", "checkPoison", replace);
            }

        }catch (NotFoundException e) {
            throw new HookException(e);
        }
    }
}