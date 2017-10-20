package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandShard extends Command {

	public CommandShard() {
		name = "shard";
		help = "Tells you which shard is currently assigned to the server";
		guildOnly = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Shard " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal() + ".");
	}

}
