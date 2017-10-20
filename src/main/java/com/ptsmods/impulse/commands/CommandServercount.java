package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandServercount extends Command {

	public CommandServercount() {
		name = "servercount";
		help = "Tells you how many servers the bot is currently in.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (Main.getShards().size() != 1)
			event.replyFormatted("This shard is currently in **%s** servers and can see **%s** users and **%s** private channels.\n"
					+ "This bot is currently in **%s** servers and can see **%s** users and **%s** private channels.",
					event.getJDA().getGuilds().size(), event.getJDA().getUsers().size(), event.getJDA().getPrivateChannels().size(),
					Main.getGuilds().size(), Main.getUsers().size(), Main.getPrivateChannels().size());
		else
			event.replyFormatted("This bot is currently in **%s** servers and can see **%s** users and **%s** private channels.",
					event.getJDA().getGuilds().size(), event.getJDA().getUsers().size(), event.getJDA().getPrivateChannels().size());
	}

}
