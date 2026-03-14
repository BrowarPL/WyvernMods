package mod.sin.actions;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class MissionAddAction implements ModAction {
	public static final Logger logger = Logger.getLogger(MissionAddAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public MissionAddAction() {
		logger.info("UnequipAllAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Add Epic Mission",
			"generating",
			new int[] { 0 }
		);
		ModActions.registerAction(actionEntry);
	}
	@Override
	public BehaviourProvider getBehaviourProvider()
	{
		return new BehaviourProvider() {
			// Menu with activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
			{
				return this.getBehavioursFor(performer, object);
			}
			// Menu without activated object
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
			{
				if(performer instanceof Player && object != null && (object.getTemplateId() == ItemList.bodyBody || object.getTemplateId() == ItemList.bodyHand) && performer.getPower() >= 5) {
					return Collections.singletonList(actionEntry);
				}
				return null;
			}
		};
	}
	@Override
	public ActionPerformer getActionPerformer()
	{
		return new ActionPerformer() {
			@Override
			public short getActionId() {
				return actionId;
			}
			// Without activated object
			@Override
			public boolean action(Action act, Creature performer, Item target, short action, float counter)
			{
				if(performer instanceof Player){
					if(performer.getPower() < 5){
						performer.getCommunicator().sendNormalServerMessage("You do not have permission to do that.");
						return true;
					}
					int[] deityNums = {
							1, 2, 3, 4, // Original Gods
							6, 7, 8, 9, 10, 11, 12 // Valrei Entities
					};
					if(EpicServerStatus.getCurrentEpicMissions().length >= deityNums.length){
						logger.info("All entities already have a mission. Aborting.");
						return true;
					}
					int tries = 0;
					int number = 1;
					while(++tries <= 10) {
						number = deityNums[Server.rand.nextInt(deityNums.length)];
						logger.info("Testing number "+number);
						if(EpicServerStatus.getEpicMissionForEntity(number) == null){
							logger.info("Has no mission, breaking loop.");
							break;
						}else{
							logger.info("Has mission, finding new number.");
						}
					}
					if(tries == 11){
						logger.info("Ran through 10 possible entities and could not find empty mission. Cancelling.");
						return true;
					}
					logger.info("Entity number = "+number);
					String entityName = Deities.getDeityName(number);
					logger.info("Entity name = "+entityName);
					int time = 604800;
					logger.info("Current epic missions: "+EpicServerStatus.getCurrentEpicMissions().length);
					EpicServerStatus es = new EpicServerStatus();
					if (EpicServerStatus.getCurrentScenario() != null) {
						es.generateNewMissionForEpicEntity(number, entityName, -1, time, EpicServerStatus.getCurrentScenario().getScenarioName(), EpicServerStatus.getCurrentScenario().getScenarioNumber(), EpicServerStatus.getCurrentScenario().getScenarioQuest(), true);
					}
				}else{
					logger.info("Somehow a non-player activated an Affinity Orb...");
				}
				return true;
			}
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				return this.action(act, performer, target, action, counter);
			}
		}; // ActionPerformer
	}
}