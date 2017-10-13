package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

public class CommandResetWarns extends Command {

	private Map settings;

	public CommandResetWarns() {
		name = "resetwarns";
		help = "Resets a user's warnings.";
		guildOnly = true;
		arguments = "<user>";
		userPermissions = new Permission[] {Permission.KICK_MEMBERS};
		category = Main.getCategory("Moderation");
		try {
			settings = DataIO.loadJson("data/warner/settings.json", Map.class);
		} catch (IOException e) {
			throw new RuntimeException("There was an error while loading the file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			try {
				settings = DataIO.loadJson("data/warner/settings.json", Map.class);
			} catch (IOException e) {
				throw new RuntimeException("There was an error while loading the file.", e);
			}
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!settings.containsKey(guildId)) event.reply("No one in this server currently has any warnings.");
			else if (!((Map) settings.get(guildId)).containsKey(userId)) event.reply("That user has no warnings.");
			else {
				((Map) settings.get(guildId)).remove(userId);
				if (((Map) settings.get(guildId)).isEmpty()) settings.remove(guildId);
				try {
					Main.saveSettings(settings, "data/warner/settings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the data file.");
					return;
				}
				event.reply(event.getGuild().getMemberById(userId).getEffectiveName() + "'s warnings have been reset.");
			}
		} else Main.sendCommandHelp(event, this);
	}

}
