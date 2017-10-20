package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandUsage extends Command {

	public CommandUsage() {
		name = "usage";
		help = "Shows you the RAM usage.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.replyFormatted("RAM used: **%s**, RAM allocated: **%s**, cores: **%s**.",
				Main.formatFileSize(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()),
				Main.formatFileSize(Runtime.getRuntime().totalMemory()),
				Runtime.getRuntime().availableProcessors());
	}

}
