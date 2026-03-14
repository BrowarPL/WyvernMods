package mod.sin.wyvern.invasion;

import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Server;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;

@SuppressWarnings("unused")
public class Invasion {
	public static final Logger logger = Logger.getLogger(Invasion.class.getName());
	public static boolean active = false;
	public static long lastInvasionPoll = 0;
	public static final HashSet<InvasionEvent> invasion = new HashSet<>();

	@SuppressWarnings("unused")
	public static void pollInvasions(){
		long now = System.currentTimeMillis();
		if (now - lastInvasionPoll > 360000) { // 6 minutes (360000ms)
			Random rand = new Random();
			Village v = null;
			int startX = 0;
			int startY = 0;
			int serverSizeX = Zones.worldTileSizeX;
			int serverSizeY = Zones.worldTileSizeY;
			int tries = 100000;
			while (v == null && --tries > 0) {
				startX = rand.nextInt(serverSizeX);
				startY = rand.nextInt(serverSizeY);
				v = Villages.getVillage(startX, startY, true);

				for (int x = -50; x < 50 && v == null; x += 5) {
					for (int y = -50; y < 50 && (v = Villages.getVillage(startX + x, startY + y, true)) == null; y += 5) {
						// Village search loop
					}
				}
			}
			if (v == null) {
				logger.severe("Could not find village while polling for invasion.");
				return;
			}
			try {
				int minion1Id = 555;
				int minion2Id = 666;
				int bossId = 777;
				String villageName = v.getName();
				InvasionEvent event = new InvasionEvent(startX, startY, villageName, bossId, minion1Id, minion2Id, rand.nextFloat() * 100f);
				invasion.add(event);
				Server.getInstance().broadCastNormal("Whispers of a " + event.getPowerString() + " Necromancer circulate the area around " + villageName + "...");
				HistoryManager.addHistory("A " + event.getPowerString() + " Necromancer", "invades the area surrounding " + villageName + "!");
				active = true;
				lastInvasionPoll = now;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error creating invasion event", e);
			}
		}
	}
}
