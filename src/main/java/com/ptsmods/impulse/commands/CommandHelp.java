package com.ptsmods.impulse.commands;

import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

public class CommandHelp extends Command {

	public CommandHelp() {
		name = "help";
		help = "Shows you all the commands";
		guildOnly = false;
		arguments = "[command or category]";
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			List<String> msgs = new ArrayList<>();
			String msg = "**" + event.getJDA().getSelfUser().getName() + "** commands:\n\n";
			for (String category : Main.sort(Main.setToList(Main.getCategories(), String[].class))) {
				msg += "**" + category + "**\n\t";
				for (String cmdName : Main.sort(Main.getCommandNames())) {
					Command cmd = Main.getCommandByName(cmdName);
					if (cmd.getCategory().getName().equals(category))
						msg += "**" + cmd.getName() + "**" + (cmd.getHelp() != null && !cmd.getHelp().isEmpty() && !cmd.getHelp().toLowerCase().equals("no help available") ? ": " + cmd.getHelp().split("\n")[0] : "") + "\n\t";
					if (msg.length() >= 1750) {
						msgs.add(msg);
						msg = "";
					}
				}
				msg = msg.trim() + "\n";
			}
			if (!msg.trim().isEmpty()) msgs.add(msg);
			for (String msg1 : msgs)
				Main.sendPrivateMessage(event.getAuthor(), msg1.trim());
			event.reactSuccess();
		} else {
			for (Command cmd : Main.getCommands()) {
				String[] args = event.getArgs().split(" ");
				if (cmd.getName().equals(args[0])) {
					args = Main.removeArg(args, 0);
					while (args.length > 0) {
						for (com.jagrosh.jdautilities.commandclient.Command scmd : cmd.getChildren())
							if (scmd.getName().equals(args[0])) {
								cmd = (Command) scmd;
								break;
							}
						args = Main.removeArg(args, 0);
					}
					Main.sendCommandHelp(event, cmd);
					return;
				}
			}
			for (String category : Main.getCategories())
				if (event.getArgs().split(" ")[0].equals(category)) {
					List<String> msgs = new ArrayList<>();
					String msg = "";
					msg += "**" + category + "**\n\t";
					for (String cmdName : Main.sort(Main.getCommandNames())) {
						Command cmd = Main.getCommandByName(cmdName);
						if (cmd.getCategory().getName().equals(category))
							msg += "**" + cmd.getName() + "**" + (cmd.getHelp() != null && !cmd.getHelp().isEmpty() && !cmd.getHelp().toLowerCase().equals("no help available") ? ": " + cmd.getHelp().split("\n")[0] : "") + "\n\t";
						if (msg.length() >= 1750) {
							msgs.add(msg);
							msg = "";
						}
					}
					msg = msg.trim() + "\n";
					if (!msg.trim().isEmpty()) msgs.add(msg);
					for (String msg1 : msgs)
						event.reply(msg1.trim());
					return;
				}
			event.reply("That command or category could not be found.");
		}
	}
}
