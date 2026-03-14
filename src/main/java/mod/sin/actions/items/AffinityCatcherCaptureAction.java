package mod.sin.actions.items;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import mod.sin.items.AffinityCatcher;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class AffinityCatcherCaptureAction implements ModAction {
	private static final Logger logger = Logger.getLogger(AffinityCatcherCaptureAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public AffinityCatcherCaptureAction() {
		logger.info("AffinityCatcherCaptureAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Capture affinity",
			"capturing",
			new int[] { 0 }
		);
		ModActions.registerAction(actionEntry);
	}
	public static boolean hasAffinityCatcher(Creature performer){
		if(performer.getInventory() == null){
			return false;
		}
		Set<Item> items = performer.getInventory().getItems();
		if(items == null){
			return false;
		}
		for(Item i : items){
			if(i.getTemplateId() == AffinityCatcher.templateId){
				return true;
			}
		}
		return false;
	}
	@Override
	public BehaviourProvider getBehaviourProvider()
	{
		return new BehaviourProvider() {
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Skill skill) {
				if(performer instanceof Player && skill.affinity > 0 && hasAffinityCatcher(performer)) {
					return Collections.singletonList(actionEntry);
				}

				return null;
			}

			// Never called
			@Override
			public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Skill skill) {
				if(performer instanceof Player && source != null && source.getTemplateId() == AffinityCatcher.templateId && source.getData() == 0) {
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
			@Override
			public boolean action(Action act, Creature performer, Skill skill, short action, float counter) {
				performer.getCommunicator().sendNormalServerMessage("Activate your affinity catcher to capture the affinity.");
				return true;
			}
			// Without activated object
			@Override
			public boolean action(Action act, Creature performer, Item source, Skill skill, short action, float counter) {
				if(performer instanceof Player){
					Player player = (Player) performer;
					if (source.getTemplate().getTemplateId() != AffinityCatcher.templateId){
						player.getCommunicator().sendSafeServerMessage("You must use an Affinity Catcher to capture an affinity.");
						return true;
					}
					if(source.getData() > 0){
						player.getCommunicator().sendSafeServerMessage("The catcher already has an affinity captured.");
						return true;
					}
					int skillNum = skill.getNumber();
					Affinity[] affs = Affinities.getAffinities(player.getWurmId());
					int affinityCount = 0;
					for (Affinity affinity : affs) {
						if(affinity.getNumber() > 0){
							logger.info("Adding "+affinity.getNumber()+" affinities to total due to skill "+SkillSystem.getNameFor(affinity.getSkillNumber()));
							affinityCount += affinity.getNumber();
						}
					}
					if(affinityCount < 3){
						logger.info("Player "+performer.getName()+" does not have enough affinities to utilize an affinity catcher.");
						performer.getCommunicator().sendNormalServerMessage("You must have at least 3 affinities in total before using an "+source.getName()+".");
						return true;
					}
					for (Affinity affinity : affs) {
						if (affinity.getSkillNumber() != skillNum) continue;
						if (affinity.getNumber() < 1){
							break;
						}
						Affinities.setAffinity(player.getWurmId(), skillNum, affinity.getNumber() - 1, false);
						logger.info("Setting "+source.getName()+" data to "+skillNum);
						source.setData(skill.getNumber());
						source.setName("captured "+skill.getName()+" affinity");
						performer.getCommunicator().sendSafeServerMessage("Your "+skill.getName()+" affinity is captured!");
						return true;
					}
					// Only called if the affinity is not found, or it breaks from having less than one.
					player.getCommunicator().sendNormalServerMessage("You must have an affinity in the skill to capture.");
				}else{
					logger.info("Somehow a non-player activated an Affinity Catcher...");
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