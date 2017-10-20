package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandFlip extends Command {

	public CommandFlip() {
		name = "flip";
		help = "Flips text.";
		arguments = "<text>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() == 0) Main.sendCommandHelp(event, this);
		else event.reply(Main.flipString(event.getArgs()));
	}

}
