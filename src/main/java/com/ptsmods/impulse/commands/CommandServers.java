package com.ptsmods.impulse.commands;

import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import net.dv8tion.jda.core.entities.Guild;

public class CommandServers extends Command {

	private static Map<Integer, String> pages = new HashMap<>();

	public CommandServers() {
		name = "servers";
		help = "Lists all servers the bot is in.";
		arguments = "[page]";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		int counter = 0;
		int counter1 = 0;
		String page = "```css\n[Page 1/%s]\n\t";
		int id = 0;
		for (Guild guild : Main.getGuilds()) {
			page += guild.getName() + "\n\t";
			counter += 1;
			counter1 += 1;
			if (counter == 10 || counter1 == Main.getGuilds().size()) {
				counter = 0;
				id += 1;
				pages.put(id, page.trim() + "```");
				page = "```css\n[Page " + (id+1) + "/%s]\n\t";
			}
		}
		int pageN = 1;
		if (event.getArgs().length() != 0 && Main.isInteger(event.getArgs().split(" ")[0])) pageN = Integer.parseInt(event.getArgs().split(" ")[0]);
		if (pageN >= pages.size()+1) event.reply("The maximum page is " + (pages.size()-1) + ".");
		else event.replyFormatted(pages.get(pageN), pages.size());
	}

}
