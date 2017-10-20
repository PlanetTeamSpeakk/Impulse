package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandSay extends Command {

	public CommandSay() {
		name = "say";
		help = "Says something.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) event.reply(event.getArgs().replaceAll("@everyone", "\\@everyone").replaceAll("@here", "\\@here"));
		else Main.sendCommandHelp(event, this);
	}

}
