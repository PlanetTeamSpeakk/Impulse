package com.ptsmods.impulse.commands;

import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

public class CommandHelp extends Command {

	public CommandHelp() {
		name = "help";
		help = "Shows you all the commands";
		guildOnly = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			List<String> msgs = new ArrayList<>();
			String msg = "**" + event.getJDA().getSelfUser().getName() + "** commands:\n\n";
			for (String category : Main.getCategories()) {
				msg += "**" + category + "**\n\t";
				for (Command cmd : Main.getCommands())
					if (cmd.getCategory().getName().equals(category))
						msg += "**" + cmd.getName() + "**" + (cmd.getHelp() != null && !cmd.getHelp().isEmpty() && !cmd.getHelp().toLowerCase().equals("no help available") ? ": " + cmd.getHelp() : "") + "\n\t";
				msg = msg.trim() + "\n";
				if (msg.length() >= 1750) {
					msgs.add(msg);
					msg = "";
				}
			}
			msgs.add(msg);
			for (String msg1 : msgs)
				Main.sendPrivateMessage(event.getAuthor(), msg1.trim());
			event.reactSuccess();
		} else {
			for (Command cmd : Main.getCommands()) {
				String[] args = event.getArgs().split(" ");
				if (cmd.getName().equals(args[0])) {
					args = Main.removeArg(args, 0);
					while (args.length > 0) {
						for (Command scmd : cmd.getChildren())
							if (scmd.getName().equals(args[0])) {
								cmd = scmd;
								break;
							}
						args = Main.removeArg(args, 0);
					}
					Main.sendCommandHelp(event, cmd);
					return;
				}
			}
			if (event.getArgs().split(" ").length > 1) event.reply("That subcommand could not be found.");
			else event.reply("That command could not be found.");
		}
	}
}
