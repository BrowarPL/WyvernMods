package mod.sin.wyvern;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.ModEntry;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPackHandler {
    public static final Logger logger = Logger.getLogger(ServerPackHandler.class.getName());

    /**
     * Serverpacks objects used through reflection.
     */
    private static Object serverPacks;
    private static Object optionPrepend;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static Object optionForce;
    private static Class<?> serverPackOptions;

    public static final Path serverPackPath = Paths.get("./mods/WyvernMods/wyvernmods-ty-pack.jar");

    public static void modInitialized(ModEntry<?> modEntry){
        if (!"serverpacks".equals(modEntry.getName())) {
            return;
        }

        try{
            serverPackOptions = modEntry.getModClassLoader().loadClass("org.gotti.wurmunlimited.mods.serverpacks.api.ServerPacks$ServerPackOptions");
            for (Object enumConstant : serverPackOptions.getEnumConstants()){
                if("PREPEND".equals(enumConstant.toString())) {
                    optionPrepend = enumConstant;
                } else if("FORCE".equals(enumConstant.toString())) {
                    optionForce = enumConstant;
                }
            }
            serverPacks = modEntry.getWurmMod();
            logger.info("Serverpacks integration initialized successfully.");
        } catch(ClassNotFoundException e){
            logger.log(Level.WARNING, "Failed to initialize serverpacks integration.", e);
            serverPackOptions = null;
            optionPrepend = null;
            optionForce = null;
            serverPacks = null;
        }
    }

    public static void onServerStarted(){
        if (serverPacks == null || serverPackOptions == null || optionPrepend == null) {
            logger.warning("Serverpack registration skipped because serverpacks integration is unavailable.");
            return;
        }

        try{
            logger.info("Registering serverpack");
            Object opts = Array.newInstance(serverPackOptions, 1);
            Array.set(opts, 0, optionPrepend);

            ReflectionUtil.getMethod(serverPacks.getClass(), "addServerPack", new Class[]{Path.class, opts.getClass()})
                    .invoke(serverPacks, serverPackPath, opts);

            logger.info("Successfully registered serverpack");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to register serverpack", e);
        }
    }
}