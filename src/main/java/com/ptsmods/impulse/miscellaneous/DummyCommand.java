package com.ptsmods.impulse.miscellaneous;

import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class DummyCommand extends Command {

	public DummyCommand() {
		name = "NAME";
		help = "USAGE";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("CATEGORY");
	}

	@Override
	protected void execute(CommandEvent event) {

	}

}
