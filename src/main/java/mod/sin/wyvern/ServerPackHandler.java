package mod.sin.wyvern;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.ModEntry;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class ServerPackHandler {
    public static final Logger logger = Logger.getLogger(ServerPackHandler.class.getName());
    /**
     * Serverpacks Objects which we can run using ReflectionUtil
     */
    private static Object serverPacks, optionPrepend, optionForce;
    private static Class<?> serverPackOptions;

    public static final Path serverPackPath = Paths.get("./mods/WyvernMods/wyvernmods-ty-pack.jar");

    public static void modInitialized(ModEntry<?> modEntry){
        // Get serverpacks class Objects we'll call later
        if(modEntry.getName().equals("serverpacks")){
            try{
                serverPackOptions = modEntry.getModClassLoader().loadClass("org.gotti.wurmunlimited.mods.serverpacks.api.ServerPacks$ServerPackOptions");
                for (Object enumConstant : serverPackOptions.getEnumConstants()){
                    if(enumConstant.toString().equals("PREPEND"))
                        optionPrepend = enumConstant;
                    else if(enumConstant.toString().equals("FORCE"))
                        optionForce = enumConstant;
                }
                serverPacks = modEntry.getWurmMod();
            }
            catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }
    }

    public static void onServerStarted(){
        try{
            logger.info("Registering serverpack");
            Object opts = Array.newInstance(serverPackOptions, 2);
            Array.set(opts, 0, optionPrepend);
            // Array.set(opts, 1, optionForce);
            ReflectionUtil.getMethod(serverPacks.getClass(), "addServerPack", new Class[]{Path.class, opts.getClass()})
                    .invoke(serverPacks, serverPackPath, opts);
            logger.info("Successfully registered serverpack");
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
