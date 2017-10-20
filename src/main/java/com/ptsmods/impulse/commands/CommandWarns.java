package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.entities.Member;

public class CommandWarns extends Command {

	private Map settings;

	public CommandWarns() {
		name = "warns";
		help = "Shows you how many warnings someone has.";
		guildOnly = true;
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
				int warns = ((Double) ((Map) settings.get(guildId)).get(userId)).intValue();
				event.reply(event.getGuild().getMemberById(userId).getEffectiveName() + " has " + warns + " warning" + (warns != 1 ? "s" : "") + ".");
			}
		} else Main.sendCommandHelp(event, this);
	}

}
