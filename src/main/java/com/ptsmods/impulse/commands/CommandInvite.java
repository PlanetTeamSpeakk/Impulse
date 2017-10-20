package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.Permission;

public class CommandInvite extends Command {

	public CommandInvite() {
		name = "invite";
		help = "Generates an invite for this bot.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("If you want to invite me to your server, click this link: <" + event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ">.");
	}

}
