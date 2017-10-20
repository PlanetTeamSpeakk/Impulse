package com.ptsmods.impulse.commands;

import java.util.Date;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandPing extends Command {

	public CommandPing() {
		name = "ping";
		help = "Pong!";
		guildOnly = false;
		category = new Category("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		long nanos = System.nanoTime();
		event.getChannel().sendTyping().complete();
		long replyTime = new Date().getTime()/1000-event.getMessage().getCreationTime().toEpochSecond();
		replyTime = replyTime < 0 ? 0 : replyTime;
		event.reply("Ping: **" + (System.nanoTime()-nanos)/1000000F + " milliseconds**, took **" + replyTime + " second" + (replyTime != 1 ? "s" : "") + "** to reply.");
	}

}
