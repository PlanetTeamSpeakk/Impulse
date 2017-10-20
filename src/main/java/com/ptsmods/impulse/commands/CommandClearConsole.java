package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandClearConsole extends Command {

	public CommandClearConsole() {
		name = "clearconsole";
		help = "Clears the console.";
		arguments = "";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		for (int i : Main.range(20000)) System.out.println();
		event.reply("The console was cleared.");
	}

}
