package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.entities.User;

public class CommandPastNames extends Command {

	public CommandPastNames() {
		name = "pastnames";
		help = "Shows you all the past names of a user.";
		arguments = "<user>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("Moderation");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("That user could not be found.");
			else
				try {
					Map data = DataIO.loadJson("data/mod/pastNames.json", Map.class);
					data = data == null ? new HashMap<>() : data;
					if (!data.containsKey(user.getId())) event.reply("No data could be found for that user.");
					else event.reply("Past names:\n" + Main.joinCustomChar(", ", ((List<String>) data.get(user.getId())).toArray(new String[0])));
				} catch (IOException e) {
					event.reply("An unknown error occured while trying to load the data file.");
				}
		}
	}

}
