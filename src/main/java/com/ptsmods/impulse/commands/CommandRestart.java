package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandRestart extends Command {

	public CommandRestart() {
		name = "restart";
		help = "Restarts the bot.";
		arguments = "";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (Main.devMode()) event.reply("This command does not work in a development environment.");
		else {
			event.reply("I'll be right back!");
			Main.restart();
		}
	}
}
