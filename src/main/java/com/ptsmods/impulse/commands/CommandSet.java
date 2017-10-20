package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;

public class CommandSet extends Command {

	private static Map settings;

	public CommandSet() {
		name = "set";
		help = "Manage settings.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
		category = Main.getCategory("Moderation");
		children = new Command[] {new CommandSetServerprefix()};
	}

	@Override
	protected void execute(CommandEvent event) {
		Main.sendCommandHelp(event, this);
	}

	public static class CommandSetServerprefix extends Command {

		public CommandSetServerprefix() {
			name = "serverprefix";
			help = "Sets this server's prefix.";
			arguments = "<prefix>";
			guildOnly = true;
			ownerCommand = false;
			userPermissions = new Permission[] {Permission.ADMINISTRATOR};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			try {
				settings = DataIO.loadJson("data/mod/settings.json", Map.class);
				settings = settings == null ? new HashMap<>() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occured while loading the data file.", e);
			}
			if (!event.getArgs().isEmpty()) {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"serverPrefix"}, new String[] {Main.getClient().getPrefix()}));
				((Map) settings.get(event.getGuild().getId())).put("serverPrefix", event.getArgs());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while saving the data file.", e);
				}
				event.reply("This server's prefix has been set to '" + event.getArgs() + "'.");
			} else Main.sendCommandHelp(event, this);
		}

	}

}
