package mod.sin.wyvern;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import net.bdew.wurm.tools.server.ModTitles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerTitles {
    public static final Logger logger = Logger.getLogger(PlayerTitles.class.getName());

    protected static HashMap<String, String> playerTitles = new HashMap<>();

    // Event Title ID's
    public static final int TITAN_SLAYER = 10000;
    public static final int SPECTRAL = 10001;

    public static void init(){
        for (WyvernMods.CustomTitle title : WyvernMods.customTitles){
            createTitle(title.getTitleId(), title.getMaleTitle(), title.getFemaleTitle(), title.getSkillId(), title.getType());
        }

        createTitle(TITAN_SLAYER, "Titanslayer", "Titanslayer", -1, "NORMAL");
        createTitle(SPECTRAL, "Spectral", "Spectral", -1, "NORMAL");
    }

    private static void createTitle(int id, String titleMale, String titleFemale, int skillId, String type) {
        ModTitles.addTitle(id, titleMale, titleFemale, skillId, type);
        logger.log(Level.INFO, String.format("Created new title with ID #%d: [\"%s\", \"%s\"]", id, titleMale, titleFemale));
    }

    public static boolean hasCustomTitle(Creature creature){
        if(creature instanceof Player){
            Player p = (Player) creature;
            return playerTitles.containsKey(p.getName());
        }
        return false;
    }

    public static String getCustomTitle(Creature creature){
        if(creature instanceof Player){
            Player p = (Player) creature;
            String customTitle = playerTitles.get(p.getName());
            if (customTitle != null && !customTitle.isEmpty()) {
                return " <" + customTitle + ">";
            }
        }
        return "";
    }

    public static void awardCustomTitles(Player player){
        String name = player.getName();
        for (Map.Entry<Integer, ArrayList<String>> entry : WyvernMods.awardTitles.entrySet()){
            int titleId = entry.getKey();
            ArrayList<String> playerList = entry.getValue();
            if (playerList == null || !playerList.contains(name)) {
                continue;
            }

            try {
                Titles.Title theTitle = Titles.Title.getTitle(titleId);
                if (theTitle != null) {
                    player.addTitle(theTitle);
                } else {
                    logger.warning("Failed to find title with ID " + titleId);
                }
            } catch(Exception e){
                logger.log(Level.WARNING, "Failed to award title with ID " + titleId, e);
            }
        }
    }
}