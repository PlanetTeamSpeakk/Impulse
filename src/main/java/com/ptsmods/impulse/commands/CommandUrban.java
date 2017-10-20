package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.entities.Message;

public class CommandUrban extends Command {

	public CommandUrban() {
		name = "urban";
		help = "Searches for something on Urban Dictionary.";
		arguments = "<searchterm>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Message msg = event.getChannel().sendMessage("Getting data...").complete();
			String searchTerm = event.getArgs();
			String data;
			try {
				data = Main.getHTML("http://api.urbandictionary.com/v0/define?term=" + searchTerm.replaceAll(" ", "+"));
			} catch (IOException e) {
				msg.editMessage("An unknown error occured while trying to get the data, please try again.").queue();
				return;
			}
			if (data.contains("\"result_type\":\"no_results\""))
				msg.editMessageFormat("No results found for the search term **%s**.", searchTerm).queue();
			else {
				Gson gson = new Gson();
				Map dataMap = gson.fromJson(data, Map.class);
				List definitions = (List) dataMap.get("list");
				Map definitionsMap = (Map) definitions.get(0);
				String definition = Main.getCleanString((String) definitionsMap.get("definition")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
				String example = Main.getCleanString((String) definitionsMap.get("example")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
				Long thumbsUp = ((Double) definitionsMap.get("thumbs_up")).longValue();
				Long thumbsDown = ((Double) definitionsMap.get("thumbs_down")).longValue();
				String result = "Definition for **" + searchTerm + "**:\n" + definition + (!example.equals("") ? "\n\nExample:\n" + example : "") + "\n\nThumbs up: **" + thumbsUp + "**\nThumbs down: **" + thumbsDown + "**";
				msg.editMessage(result).queue();
			}
		} else Main.sendCommandHelp(event, this);
	}

}
