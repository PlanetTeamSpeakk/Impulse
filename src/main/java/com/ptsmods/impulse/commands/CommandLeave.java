package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class CommandLeave extends Command {

	public CommandLeave() {
		name = "leave";
		help = "Leaves the server.";
		guildOnly = true;
		userPermissions = new Permission[] {Permission.KICK_MEMBERS};
		category = new Category("Moderation");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Are you sure you want me to go? (yes/no)");
		Message response = Main.waitForInput(event.getGuild().getMember(event.getAuthor()), event.getChannel(), 15000, event.getMessage().getCreationTime().toEpochSecond());
		if (response == null)
			event.reply("No response gotten, guess I'll stay.");
		else if (response.getContent().toLowerCase().startsWith("ye")) {
			event.reactSuccess();
			event.getGuild().leave().queue();
		} else event.reply("Guess I'll stay.");
	}

}
