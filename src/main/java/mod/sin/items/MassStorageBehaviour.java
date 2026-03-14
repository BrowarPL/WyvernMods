package mod.sin.items;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.vehicles.ModVehicleBehaviour;
import org.gotti.wurmunlimited.modsupport.vehicles.VehicleFacade;

import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public class MassStorageBehaviour extends ModVehicleBehaviour {
	public static final Logger logger = Logger.getLogger(MassStorageBehaviour.class.getName());
	public void setSettingsForVehicle(final Creature creature, final Vehicle vehicle){
		if (creature == null || vehicle == null) {
			logger.log(Level.WARNING, "setSettingsForVehicle called with null creature or vehicle");
        }
	}
	public void setSettingsForVehicle(final Item item, final Vehicle v) {
		if (item == null || v == null) {
			logger.log(Level.WARNING, "setSettingsForVehicle called with null item or vehicle");
			return;
		}

		try {
			logger.info("Setting vehicle behaviour for item "+item.getTemplate().getTemplateId());

			VehicleFacade vehicle = wrap(v);
			if (vehicle == null) {
				logger.log(Level.SEVERE, "Failed to wrap vehicle for item "+item.getTemplateId());
				return;
			}

			vehicle.createPassengerSeats(0);
			vehicle.setSeatFightMod(0, 0.7f, 0.4f);
			vehicle.setSeatOffset(0, 0f, 1.5f, -0.2f);
			vehicle.setCreature(false);
			vehicle.setEmbarkString("enter");
			vehicle.setName(item.getName());
			vehicle.setMaxDepth(9000f);
			vehicle.setMaxHeightDiff(0.00f);
			vehicle.setCommandType((byte)2);

			final Seat[] hitches = { createSeat(Seat.TYPE_HITCHED) };
			hitches[0].offx = 3.0f;
			hitches[0].offy = 0.0f;
			vehicle.addHitchSeats(hitches);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error setting up vehicle behaviour for item "+(item != null ? item.getTemplateId() : "null"), e);
		}
	}
}
