package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandShutdown extends Command {

	public CommandShutdown() {
		name = "shutdown";
		help = "Shuts down the bot.";
		ownerCommand = true;
		guildOnly = false;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Kk, shutting down...");
		Main.print(LogType.WARN, event.getAuthor().getName() + " requested the bot to be shut down.");
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {}
		Main.shutdown(0);
	}

}
