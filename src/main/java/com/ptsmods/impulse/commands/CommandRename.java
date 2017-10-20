package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CommandRename extends Command {

	public CommandRename() {
		name = "rename";
		help = "Renames a user.";
		arguments = "<user> <newName>";
		guildOnly = false;
		ownerCommand = false;
		userPermissions = new Permission[] {Permission.NICKNAME_MANAGE};
		botPermissions = new Permission[] {Permission.NICKNAME_MANAGE};
		category = Main.getCategory("Moderation");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That member could not be found.");
			else if (!PermissionUtil.canInteract(event.getSelfMember(), member)) event.reply("I cannot rename that user as they're higher in the hierarchy than I am.");
			else {
				String newName = "";
				if (event.getMessage().getMentionedUsers().isEmpty())
					newName = Main.join(Main.removeArgs(event.getArgs().split(" "), Main.range(member.getUser().getName().split(" ").length)));
				else
					newName = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
				event.getGuild().getController().setNickname(member, newName).queue();
				event.replyFormatted("%s's nickname %s.", member.getAsMention(), newName.isEmpty() ? "has been reset" : "has been changed to " + newName);
			}
		}
	}

}
