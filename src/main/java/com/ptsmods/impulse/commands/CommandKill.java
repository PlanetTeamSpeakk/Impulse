package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.Random;

public class CommandKill extends Command {

	private static String[] waysToKill = new String[] {
			"{KILLER} shoves a double barreled shotgun into {VICTIM}'s mouth and squeezes the trigger of the gun, causing {VICTIM}'s head to horrifically explode like a ripe pimple, splattering the young person's brain matter, gore, and bone fragments all over the walls and painting it a crimson red.",
			"Screaming in sheer terror and agony, {VICTIM} is horrifically dragged into the darkness by unseen forces, leaving nothing but bloody fingernails and a trail of scratch marks in the ground from which the young person had attempted to halt the dragging process.",
			"{KILLER} takes a machette and starts hacking away on {VICTIM}, chopping {VICTIM} into dozens of pieces.",
			"{KILLER} pours acid over {VICTIM}. *\"Well don't you look pretty right now?\"*",
			"{VICTIM} screams in terror as a giant creature with huge muscular arms grab {VICTIM}'s head; {VICTIM}'s screams of terror are cut off as the creature tears off the head with a sickening crunching sound. {VICTIM}'s spinal cord, which is still attached to the dismembered head, is used by the creature as a makeshift sword to slice a perfect asymmetrical line down {VICTIM}'s body, causing the organs to spill out as the two halves fall to their respective sides.",
			"{KILLER} grabs {VICTIM}'s head and tears it off with superhuman speed and efficiency. Using {VICTIM}'s head as a makeshift basketball, {KILLER} expertly slams dunk it into the basketball hoop, much to the applause of the audience watching the gruesome scene.",
			"{KILLER} uses a shiv to horrifically stab {VICTIM} multiple times in the chest and throat, causing {VICTIM} to gurgle up blood as the young person horrifically dies.",
			"{VICTIM} screams as Pyramid Head lifts Sarcen up using his superhuman strength. Before {VICTIM} can even utter a scream of terror, Pyramid Head uses his superhuman strength to horrifically tear {VICTIM} into two halves; {VICTIM} stares at the monstrosity in shock and disbelief as {VICTIM} gurgles up blood, the upper body organs spilling out of the dismembered torso, before the eyes roll backward into the skull.",
			"{VICTIM} steps on a land mine and is horrifically blown to multiple pieces as the device explodes, {VICTIM}'s entrails and gore flying up and splattering all around as if someone had thrown a watermelon onto the ground from the top of a multiple story building.",
	"{VICTIM} is killed instantly as the top half of his head is blown off by a Red Army sniper armed with a Mosin Nagant, {VICTIM}'s brains splattering everywhere in a horrific fashion."};

	public CommandKill() {
		name = "kill";
		help = "Kill someone.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("Fun");
	}

	@Override
	protected void execute(CommandEvent event) {
		Main.print(LogType.INFO, event.getGuild().getMembersByName(event.getArgs(), true));
		if (!event.getArgs().isEmpty() && event.getGuild().getMembersByName(event.getArgs(), true).get(0) == null) event.reply("That user could not be found.");
		else if (event.getArgs().length() != 0) event.reply(waysToKill[Random.randInt(waysToKill.length)].replaceAll("\\{KILLER\\}", event.getAuthor().getAsMention()).replaceAll("\\{VICTIM\\}", event.getMessage().getMentionedUsers().size() != 0 ? event.getMessage().getMentionedUsers().get(0).getAsMention() : event.getGuild().getMembersByName(event.getArgs(), true).get(0).getUser().getAsMention()));
		else Main.sendCommandHelp(event, this);
	}

}
