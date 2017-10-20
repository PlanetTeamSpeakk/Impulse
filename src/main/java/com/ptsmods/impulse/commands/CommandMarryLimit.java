package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

public class CommandMarryLimit extends Command {

	private static Map settings;

	public CommandMarryLimit() {
		name = "marrylimit";
		help = "Shows you this server's current marry limit.";
		arguments = "";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
		try {
			settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the data file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		try {
			settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the data file.", e);
		}
		if (!settings.containsKey(event.getGuild().getId()) || Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) < 0) event.reply("This server has no marrylimit.");
		else event.reply("This server's marry limit is currently set to " + Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) + ".");
	}

}
