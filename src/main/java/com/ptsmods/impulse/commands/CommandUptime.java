package com.ptsmods.impulse.commands;

import java.util.Date;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class CommandUptime extends Command {

	public CommandUptime() {
		name = "uptime";
		help = "Shows you how long this bot has been up for.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("I have been up for " + Main.formatMillis(new Date().getTime()-Main.started.getTime()) + ".");
	}

}
