package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

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
		String searchTerm = event.getArgs();
		String data;
		try {
			data = Main.getHTML("http://api.urbandictionary.com/v0/define?term=" + searchTerm.replaceAll(" ", "+"));
		} catch (IOException e) {
			event.reply("An unknown error occured while trying to get the data, please try again.");
			return;
		}
		if (data.contains("\"result_type\":\"no_results\""))
			event.replyFormatted("No results found for the search term **%s**.", searchTerm);
		else {
			Gson gson = new Gson();
			Map dataMap = gson.fromJson(data, Map.class);
			List definitions = (List) dataMap.get("list");
			Map definitionsMap = (Map) definitions.toArray()[0];
			String definition = Main.getCleanString((String) definitionsMap.get("definition")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
			String example = Main.getCleanString((String) definitionsMap.get("example")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
			Long thumbsUp = ((Double) definitionsMap.get("thumbs_up")).longValue();
			Long thumbsDown = ((Double) definitionsMap.get("thumbs_down")).longValue();
			String result = "Definition for **" + searchTerm + "**:\n" + definition + (!example.equals("") ? "\n\nExample:\n" + example : "") + "\n\nThumbs up: **" + thumbsUp + "**\nThumbs down: **" + thumbsDown + "**";
			event.reply(result);
		}
	}

}
