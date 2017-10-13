package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CommandMute extends Command {

	public CommandMute() {
		name = "mute";
		help = "Mutes someone.";
		guildOnly = true;
		arguments = "<member>";
		botPermissions = new Permission[] {Permission.MANAGE_ROLES};
		userPermissions = new Permission[] {Permission.KICK_MEMBERS};
		category = Main.getCategory("Moderation");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot mute that user as they're higher in the hierarchy than I am.");
			else {
				Main.mute(member);
				event.reply("Successfully muted " + member.getEffectiveName() + ".");
			}
		} else Main.sendCommandHelp(event, this);
	}

}
