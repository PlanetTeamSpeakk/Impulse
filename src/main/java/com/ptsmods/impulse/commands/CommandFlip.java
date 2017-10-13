package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

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
		event.reply(Main.flipString(event.getArgs()));
	}

}
