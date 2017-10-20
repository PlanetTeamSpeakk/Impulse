package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

public class CommandAnnounce extends Command {

	public CommandAnnounce() {
		name = "announce";
		help = "Announces a message to every server the bot is in.";
		arguments = "<message>";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Message status = event.getChannel().sendMessageFormat("Sending announcement to every guild...", Main.getGuilds().size()).complete();
			for (Guild guild : Main.getGuilds())
				guild.getDefaultChannel().sendMessageFormat("%s ~ %s#%s", event.getArgs(), event.getAuthor().getName(), event.getAuthor().getDiscriminator()).queue();
			status.editMessage("Announcement sent.").queue();
		}
	}

}
