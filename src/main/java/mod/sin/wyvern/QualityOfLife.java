package mod.sin.wyvern;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class QualityOfLife {
    public static final Logger logger = Logger.getLogger(QualityOfLife.class.getName());

    public static boolean insertItemIntoVehicle(Item item, Item vehicle, Creature performer) {
        // If can put into crates, try that
        if (item.getTemplate().isBulk() && item.getRarity() == 0) {
            for (Item container : vehicle.getAllItems(true)) {
                if (container.getTemplateId() == ItemList.bulkContainer) {
                    if (container.getFreeVolume() >= item.getVolume()) {
                        if (item.AddBulkItem(performer, container)) {
                            performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s in your %s.", item.getName(), container.getName(), vehicle.getName()));
                            return true;
                        }
                    }
                }
            }
            for (Item container : vehicle.getAllItems(true)) {
                if (container.isCrate() && container.canAddToCrate(item)) {
                    if (item.AddBulkItemToCrate(performer, container)) {
                        performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s in your %s.", item.getName(), container.getName(), vehicle.getName()));
                        return true;
                    }
                }
            }
        }
        // No empty crates or disabled, try the vehicle itself
        if (vehicle.getNumItemsNotCoins() < 100 && vehicle.getFreeVolume() >= item.getVolume() && vehicle.insertItem(item)) {
            performer.getCommunicator().sendNormalServerMessage(String.format("You put the %s in the %s.", item.getName(), vehicle.getName()));
            return true;
        } else {
            // Send message if the vehicle is too full
            performer.getCommunicator().sendNormalServerMessage(String.format("The %s is too full to hold the %s.", vehicle.getName(), item.getName()));
            return false;
        }
    }
    public static Item getVehicleSafe(Creature pilot) {
        try {
            if (pilot.getVehicle() != -10)
                return Items.getItem(pilot.getVehicle());
        } catch (NoSuchItemException ignored) {
        }
        return null;
    }
    @SuppressWarnings("unused")
    public static boolean vehicleHook(Creature performer, Item item) {
        Item vehicleItem = getVehicleSafe(performer);

        // Simplified boolean return to satisfy IDE and Clean Code rules
        return vehicleItem != null && vehicleItem.isHollow() && insertItemIntoVehicle(item, vehicleItem, performer);
    }
    public static void preInit() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();
            final Class<QualityOfLife> thisClass = QualityOfLife.class;
            String replace;

            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            CtClass ctCaveWallBehaviour = classPool.get("com.wurmonline.server.behaviours.CaveWallBehaviour");
            CtClass[] params1 = {
                    ctAction, ctCreature, ctItem, CtClass.intType, CtClass.intType,
                    CtClass.booleanType, CtClass.intType, CtClass.intType,
                    CtClass.intType, CtClass.shortType, CtClass.floatType
            };
            String desc1 = Descriptor.ofMethod(CtClass.booleanType, params1);

            if (WyvernMods.mineCaveToVehicle) {
                Util.setReason("Allow players to mine directly into vehicles.");
                replace = "if (!" + QualityOfLife.class.getName() + ".vehicleHook((com.wurmonline.server.creatures.Creature)$1, $0)) { $_ = $proceed($$); }";
                Util.instrumentDescribed(thisClass, ctCaveWallBehaviour, "action", desc1, "putItemInfrontof", replace);
            }

            CtClass ctTileRockBehaviour = classPool.get("com.wurmonline.server.behaviours.TileRockBehaviour");
            if (WyvernMods.mineSurfaceToVehicle) {
                Util.setReason("Allow players to surface mine directly into vehicles.");
                replace = "$_ = $proceed($$); " + QualityOfLife.class.getName() + ".vehicleHook(performer, $0);";
                Util.instrumentDeclared(thisClass, ctTileRockBehaviour, "mine", "setDataXY", replace);
            }

            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            if (WyvernMods.chopLogsToVehicle) {
                Util.setReason("Allow players to chop logs directly into vehicles.");
                replace = "if (!" + QualityOfLife.class.getName() + ".vehicleHook((com.wurmonline.server.creatures.Creature)$1, $0)) { $_ = $proceed($$); }";
                Util.instrumentDeclared(thisClass, ctMethodsItems, "chop", "putItemInfrontof", replace);
            }

            if (WyvernMods.statuetteAnyMaterial) {
                Util.setReason("Allow statuettes to be used when not gold/silver.");
                String desc100 = Descriptor.ofMethod(CtClass.booleanType, new CtClass[]{});
                replace = "{ return this.template.holyItem; }";
                Util.setBodyDescribed(thisClass, ctItem, "isHolyItem", desc100, replace);
            }

            if (WyvernMods.mineGemsToVehicle) {
                Util.setReason("Send gems, source crystals, flint, etc. into vehicle.");
                CtClass[] params2 = {
                        CtClass.intType, CtClass.intType, CtClass.intType, CtClass.intType,
                        ctCreature, CtClass.doubleType, CtClass.booleanType, ctAction
                };
                String desc2 = Descriptor.ofMethod(ctItem, params2);
                replace = "if (!" + QualityOfLife.class.getName() + ".vehicleHook((com.wurmonline.server.creatures.Creature)$1, $0)) { $_ = $proceed($$); }";
                Util.instrumentDescribed(thisClass, ctTileRockBehaviour, "createGem", desc2, "putItemInfrontof", replace);
            }

            if (WyvernMods.regenerateStaminaOnVehicleAnySlope) {
                CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
                ctPlayer.getMethod("poll", "()Z").instrument(new ExprEditor() {
                    @Override
                    public void edit(FieldAccess f) throws CannotCompileException {
                        if (f.getFieldName().equals("vehicle") && f.isReader())
                            f.replace("$_ = -10L;");
                    }
                });
            }
        } catch (NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        } catch (CannotCompileException e) {
            logger.log(Level.WARNING, "Failed to compile Javassist hook in QualityOfLife", e);
        }
    }
}
