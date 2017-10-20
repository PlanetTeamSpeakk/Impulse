package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;

public class CommandSetMarryLimit extends Command {

	private static Map settings;

	public CommandSetMarryLimit() {
		name = "setmarrylimit";
		help = "Sets this server's current marry limit.";
		arguments = "<limit>";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
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
		if (!Main.isInteger(event.getArgs().split(" ")[0])) Main.sendCommandHelp(event, this);
		else {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
			((Map) settings.get(event.getGuild().getId())).put("marryLimit", Integer.parseInt(event.getArgs().split(" ")[0]));
			try {
				DataIO.saveJson(settings, "data/marriage/settings.json");
			} catch (IOException e) {
				event.reply("An unknown error occured while saving the data file.");
				e.printStackTrace();
				return;
			}
			event.reply("Successfully set the marrylimit of this server to " + event.getArgs().split(" ")[0] + ".");
		}
	}

}
