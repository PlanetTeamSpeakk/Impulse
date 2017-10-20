package com.ptsmods.impulse.commands;

import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

public class CommandForceDivorce extends Command {

	public CommandForceDivorce() {
		name = "forcedivorce";
		help = "Forcibly divorce a couple.\nExample: [p]forcedivorce My homie #1; My homie #36";
		arguments = "<user1>; <user2>";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getArgs().split(";").length > 1 || event.getArgs().split("; ").length > 1) {
			String[] usernames = event.getArgs().contains("; ") ? event.getArgs().split("; ") : event.getArgs().split(";");
			List<Member> members = new ArrayList<>();
			MessageChannel marriageChannel = null;
			if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());
			if (marriageChannel == null) marriageChannel = event.getGuild().getTextChannelsByName("marriage", true).get(0);
			for (String username : usernames)
				if (!event.getGuild().getMembersByName(username, true).isEmpty()) members.add(event.getGuild().getMembersByName(username, true).get(0));
				else {
					event.reply("The user '" + username + "' could not be found.");
					return;
				}
			for (Role role : event.getGuild().getRoles())
				if (role.getName().contains(members.get(0).getUser().getName()) && role.getName().contains(CommandMarry.heart) && role.getName().contains(members.get(1).getUser().getName())) {
					role.delete().queue();
					event.replyFormatted("Successfully deleted the marriage role between %s and %s.", members.get(0).getAsMention(), members.get(1).getAsMention());
					marriageChannel.sendMessageFormat("%s was forced to divorce %s.", members.get(0).getAsMention(), members.get(1).getAsMention()).queue();
					return;
				}
			event.reply("A marriage role between those 2 members could not be found.");
		} else Main.sendCommandHelp(event, this);
	}

}
