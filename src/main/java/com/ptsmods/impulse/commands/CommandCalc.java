package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandCalc extends Command {

	public CommandCalc() {
		name = "calc";
		help = "Calculates a math equation so you don't have to";
		arguments = "<equation>";
		guildOnly = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0)
			try {
				event.reply("`" + event.getArgs() + "` = `" + Main.eval(event.getArgs()) + "`");
			} catch (RuntimeException e) {
				event.reply(e.getMessage());
			}
		else Main.sendCommandHelp(event, this);
	}

}
