package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.entities.User;

public class CommandAvatar extends Command {

	public CommandAvatar() {
		name = "avatar";
		help = "Gives you the avatar of a user.";
		arguments = "<user>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			event.reply(user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
		} else Main.sendCommandHelp(event, this);
	}

}
