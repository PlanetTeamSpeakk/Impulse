package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandContact extends Command {

	public CommandContact() {
		name = "contact";
		help = "Sends a message to my owner.";
		arguments = "<message>";
		guildOnly = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Main.sendPrivateMessage(Main.getOwner(), String.format("**%s#%s** (%s) has sent you a message from **%s** (%s):\n\n",
					event.getAuthor().getName(),
					event.getAuthor().getDiscriminator(),
					event.getAuthor().getId(),
					event.getGuild().getName(),
					event.getGuild().getId()) + event.getArgs());
			event.reply("Successfully sent your message to my owner!");
		} else Main.sendCommandHelp(event, this);
	}

}
