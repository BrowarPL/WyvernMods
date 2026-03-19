package mod.sin.actions.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import mod.sin.items.EnchantOrb;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;


import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EnchantOrbAction implements ModAction {
	public static final Logger logger = Logger.getLogger(EnchantOrbAction.class.getName());

	private final short actionId;
	private final ActionEntry actionEntry;

	public EnchantOrbAction() {
		logger.info("EnchantOrbAction()");

		actionId = (short) ModActions.getNextActionId();
		actionEntry = ActionEntry.createEntry(
			actionId,
			"Transfer enchant",
			"transferring",
			new int[0]
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
				if(performer instanceof Player && source != null && object != null && source.getTemplateId() == EnchantOrb.templateId && source != object){
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
			// With activated object
			@Override
			public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
			{
				if(performer instanceof Player){
					Player player = (Player) performer;
					if(source.getTemplate().getTemplateId() != EnchantOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You must use an Enchant Orb to transfer enchants.");
						return true;
					}
					if(target.getTemplate().getTemplateId() == EnchantOrb.templateId){
						player.getCommunicator().sendNormalServerMessage("You cannot enchant an Enchant Orb with another.");
						return true;
					}
					ItemSpellEffects effs = source.getSpellEffects();
					if(effs == null){
						player.getCommunicator().sendNormalServerMessage("The "+source.getTemplate().getName()+" has no enchants.");
						return true;
					}
					SpellEffect[] sourceEffects = effs.getEffects();
					if(sourceEffects == null || sourceEffects.length == 0){
						player.getCommunicator().sendNormalServerMessage("The "+source.getTemplate().getName()+" has no enchants.");
						return true;
					}
					ItemSpellEffects teffs = target.getSpellEffects();
					if(teffs == null){
						teffs = new ItemSpellEffects(target.getWurmId());
					}
					for(SpellEffect eff : sourceEffects){
						Spell spell = Spells.getEnchantment(eff.type);
						boolean canEnchant = false;
						byte type = eff.type;
						if(spell == null){
							if(eff.type < -60){ // It's a rune
								if(teffs.getNumberOfRuneEffects() > 0){
									teffs.getRandomRuneEffect();
									player.getCommunicator().sendAlertServerMessage("The "+target.getTemplate().getName()+" already has a rune attached and resists the application of the "+eff.getName()+".");
									continue;
								}else{
									canEnchant = true;
								}
							}else{
								if(teffs.getSpellEffect(type) != null){
									float power = teffs.getSpellEffect(type).getPower();
									if(power >= 100f){
										player.getCommunicator().sendAlertServerMessage("The "+target.getTemplate().getName()+" already has the maximum power for "+eff.getName()+", and refuses to accept more.");
										continue;
									}else if(power + eff.getPower() > 100){
										float difference = 100-power;
										eff.setPower(eff.getPower()-difference);
										teffs.getSpellEffect(type).setPower(100);
										player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" transfers some of its power to the "+target.getTemplate().getName()+".");
										continue;
									}else{
										teffs.getSpellEffect(type).setPower(effs.getSpellEffect(type).getPower()+power);
										effs.removeSpellEffect(type);
										player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" fully transfers to the "+target.getTemplate().getName()+".");
										continue;
									}
								}else{
									canEnchant = true;
								}
							}
						}else {
							canEnchant = spell.isValidItemType(performer, target);
						}
						if(canEnchant){
							if(teffs.getSpellEffect(type) != null){
								if(teffs.getSpellEffect(type).getPower() >= eff.getPower()) {
									player.getCommunicator().sendAlertServerMessage("The " + target.getTemplate().getName() + " already has a more powerful " + eff.getName() + " and resists the transfer.");
								}else{
									teffs.getSpellEffect(type).setPower(eff.getPower());
									effs.removeSpellEffect(type);
									player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" replaces the existing enchant.");
								}
								continue;
							}
							SpellEffect newEff = new SpellEffect(target.getWurmId(), type, eff.getPower(), 20000000);
							teffs.addSpellEffect(newEff);
							effs.removeSpellEffect(type);
							player.getCommunicator().sendSafeServerMessage("The "+eff.getName()+" transfers to the "+target.getTemplate().getName()+".");
						} else {
							player.getCommunicator().sendAlertServerMessage("The "+target.getTemplate().getName()+" is not a valid target for " + eff.getName() + ".");
						}
					}
					SpellEffect[] finalEffects = effs.getEffects();
					if(finalEffects == null || finalEffects.length == 0){
						player.getCommunicator().sendSafeServerMessage("The "+source.getTemplate().getName()+" exhausts the last of its magic and vanishes.");
						Items.destroyItem(source.getWurmId());
					}
				}else{
					logger.info("Somehow a non-player activated an Enchant Orb...");
				}
				return true;
			}
		}; // ActionPerformer
	}
}