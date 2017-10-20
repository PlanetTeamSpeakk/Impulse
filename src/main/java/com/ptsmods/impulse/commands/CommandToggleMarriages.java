package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;

public class CommandToggleMarriages extends Command {

	private static Map settings;

	public CommandToggleMarriages() {
		name = "togglemarriages";
		help = "Toggles marriages in this server.";
		arguments = "";
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
		if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
		boolean disabled = (boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled");
		((Map) settings.get(event.getGuild().getId())).put("disabled", !disabled);
		try {
			DataIO.saveJson(settings, "data/marriage/settings.json");
		} catch (IOException e) {
			event.reply("An unknown error occured while saving the data file.");
			e.printStackTrace();
			return;
		}
		event.reply("Marriages in this server are now " + (disabled ? "enabled" : "disabled") + ".");
	}
}
