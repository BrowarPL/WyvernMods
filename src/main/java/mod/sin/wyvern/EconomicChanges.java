package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.items.SealedMap;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Logger;

public class EconomicChanges {
    public static final Logger logger = Logger.getLogger(EconomicChanges.class.getName());

    @SuppressWarnings("unused")
    public static int getNewVillageTiles(int tiles){
        float power = 2f;
        float changeRate = 1000;
        float maxNumTiles = 50000;
        return (int) ((float) tiles * (1 - Math.pow((float) tiles / maxNumTiles, power))
                + (Math.sqrt((float) tiles) * changeRate) * Math.pow((float) tiles / maxNumTiles, power));
    }

    @SuppressWarnings("unused")
    public static long getNewDisbandMoney(GuardPlan gp, Village v){
        int tiles = v.getDiameterX() * v.getDiameterY();
        long tileCost = (long) tiles * Villages.TILE_COST;
        long perimeterCost = (long) v.getPerimeterSize() * Villages.PERIMETER_COST;
        return gp.moneyLeft + tileCost + perimeterCost;
    }

    private static final float PRICE_MARKUP = 1f / 1.4f;

    @SuppressWarnings("unused")
    public static int getNewValue(Item item){
        if(item.getTemplateId() == SealedMap.templateId){
            float qual = item.getQualityLevel();
            float dam = item.getDamage();
            float initialValue = ((float) item.getTemplate().getValue()) * qual * qual / 10000f;
            float baseCost = 50000f;
            float power = 6.0f;
            return (int) (((baseCost + (initialValue / 4.5f)) * (1f - Math.pow(qual / 100f, power))
                    + initialValue * Math.pow(qual / 100f, power)) * ((100f - dam) / 100f) * PRICE_MARKUP);
        }
        return -10;
    }

    @SuppressWarnings("unused")
    public static long getNewShopDiff(Trade trade, long money, long shopDiff){
        if (trade == null) {
            logger.warning("Trade was null in getNewShopDiff.");
            return money;
        }

        Shop shop = null;
        Creature creatureOne = trade.creatureOne;
        Creature creatureTwo = trade.creatureTwo;

        if (creatureOne != null && creatureOne.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(creatureOne);
        }
        if (shop == null && creatureTwo != null && creatureTwo.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(creatureTwo);
        }
        if(shop == null){
            logger.info("Something went horribly wrong and the shop is null.");
            return money;
        }

        logger.info("Money = " + money + ", shopDiff = " + shopDiff);
        if(!shop.isPersonal() && money > 0){
            logger.info("We're adding money. Testing to see how much difference there is.");
            if(money + shopDiff > 0){
                logger.info("Player actually purchased something. Reducing the income.");
                long newDiff = money + shopDiff;
                logger.info("Actual difference in currency: " + Economy.getEconomy().getChangeFor(newDiff).getChangeString());
                newDiff = Math.round(newDiff * 0.2);
                logger.info("After 80% void: " + Economy.getEconomy().getChangeFor(newDiff).getChangeString());
                logger.info("Returning the following amount of money to incur the change: " + (-shopDiff + newDiff));
                return -shopDiff + newDiff;
            }
        }
        return money;
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<EconomicChanges> thisClass = EconomicChanges.class;
            String replace;

            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctTrade = classPool.get("com.wurmonline.server.items.Trade");

            if (WyvernMods.adjustSealedMapValue) {
                Util.setReason("Adjust value for certain items.");
                replace = "int newVal = " + EconomicChanges.class.getName() + ".getNewValue(this);"
                        + "if(newVal > 0){"
                        + "  return newVal;"
                        + "}";
                Util.insertBeforeDeclared(thisClass, ctItem, "getValue", replace);
            }

            if (WyvernMods.disableTraderRefill) {
                Util.setReason("Remove trader refilling off kings coffers.");
                replace = "$_ = 1;";
                Util.instrumentDeclared(thisClass, ctCreature, "removeRandomItems", "nextInt", replace);
            }

            if (WyvernMods.voidTraderMoney) {
                Util.setReason("Void 80% of all currency put into traders.");
                replace = "$1 = " + EconomicChanges.class.getName() + ".getNewShopDiff($0, $1, $0.shopDiff);";
                Util.insertBeforeDeclared(thisClass, ctTrade, "addShopDiff", replace);
            }

        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        }
    }
}