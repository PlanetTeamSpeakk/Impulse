package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.entities.User;

public class CommandPenis extends Command {

	public CommandPenis() {
		name = "penis";
		help = "Tells you the size of someone's penis.\n\nThis is 100% accurate.";
		arguments = "<user>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("The given user could not be found.");
			else {
				Random.seed(user.getId());
				event.reply("Size: 8" + Main.multiplyString("=", Random.randInt(30)) + "D");
			}
		}
	}

}
