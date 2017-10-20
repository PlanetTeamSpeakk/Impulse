package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.Config;

import net.dv8tion.jda.core.entities.Game;

public class CommandToggleDevMode extends Command {

	public CommandToggleDevMode() {
		name = "toggledevmode";
		help = "Toggles developer mode, if this is enabled errors will not be sent to the owner and only the owner can use commands.";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
	}

	@Override
	protected void execute(CommandEvent event) {
		Main.devMode(!Main.devMode());
		event.getJDA().getPresence().setGame(Game.of(Main.devMode() ? "DEVELOPER MODE" : "try " + Config.getValue("prefix") + "help!"));
		event.reply("Developer mode has been " + (Main.devMode() ? "enabled" : "disabled") + ", " + (Main.devMode() ? "errors will no longer be sent privately and only you will now be able to use commands." :
				"errors will once again be sent privately and everyone will be able to use commands again."));
	}
}
