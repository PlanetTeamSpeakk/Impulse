package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

public class CommandDivorce extends Command {

	private static Map settings;

	public CommandDivorce() {
		name = "divorce";
		help = "Divorce your ex.";
		arguments = "<divorceId>";
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
		if (!event.getArgs().isEmpty() && Main.isLong(event.getArgs().split(" ")[0])) {
			MessageChannel marriageChannel = null;
			if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());
			if (marriageChannel == null) marriageChannel = event.getGuild().getTextChannelsByName("marriage", true).get(0);
			Role marriageRole = event.getGuild().getRoleById(event.getArgs().split(" ")[0]);
			if (marriageRole == null) event.reply("That divorce id could not be found.");
			else {
				marriageRole.delete().queue();
				event.reply("You're now divorced.");
				marriageChannel.sendMessageFormat("%s divorced id `%s`.", event.getAuthor().getName(), marriageRole.getId()).queue();
			}
		} else Main.sendCommandHelp(event, this);
	}

}
