package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.Random;

public class CommandRoll extends Command {

	public CommandRoll() {
		name = "roll";
		help = "Rolls a random number between 0 and user's choice (defaults to 100).";
		arguments = "[amount]";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		int max = 100;
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs())) max = Integer.parseInt(event.getArgs());
		event.reply(String.format("I picked **%s**.", Random.randInt(max)));
	}
}
