package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CommandKick extends Command {

	public CommandKick() {
		name = "kick";
		help = "Kicks someone.";
		arguments = "<member>";
		guildOnly = true;
		botPermissions = new Permission[] {Permission.KICK_MEMBERS};
		userPermissions = new Permission[] {Permission.KICK_MEMBERS};
		category = Main.getCategory("Moderation");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot kick that user as they're higher in the hierarchy than I am.");
			else {
				event.getGuild().getController().kick(member).complete();
				event.reply("Successfully kicked " + member.getEffectiveName() + ".");
			}
		} else Main.sendCommandHelp(event, this);
	}

}
