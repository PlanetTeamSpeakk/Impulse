package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;

public class CommandBlacklist extends Command {

	private static Map settings;

	public CommandBlacklist() {
		name = "blacklist";
		help = "Blacklist commands.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
		category = Main.getCategory("Moderation");
		children = new Command[] {new CommandBlacklistAdd(), new CommandBlacklistRemove(), new CommandBlacklistGlobal()};
		try {
			settings = DataIO.loadJson("data/mod/blacklist.json", Map.class);
			settings = settings == null ? new HashMap<>() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the data file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		Main.sendCommandHelp(event, this);
	}

	public static class CommandBlacklistAdd extends Command {

		public CommandBlacklistAdd() {
			name = "add";
			help = "Add someone to the blacklist.";
			arguments = "<user>";
			guildOnly = true;
			ownerCommand = false;
			userPermissions = new Permission[] {Permission.ADMINISTRATOR};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			if (!event.getArgs().isEmpty()) {
				try {
					settings = DataIO.loadJson("data/mod/blacklist.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the data file.", e);
				}
				Member member = Main.getMemberFromInput(event.getMessage());
				if (member == null) event.reply("That user could not be found.");
				else {
					if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), new ArrayList<>());
					((List) settings.get(event.getGuild().getId())).add(member.getUser().getId());
					try {
						DataIO.saveJson(settings, "data/mod/blacklist.json");
					} catch (IOException e) {
						throw new RuntimeException("An unknown error occured while saving the data file.", e);
					}
					event.reply("Successfully added " + member.getAsMention() + " to this server's blacklist.");
				}
			} else Main.sendCommandHelp(event, this);
		}
	}

	public static class CommandBlacklistRemove extends Command {

		public CommandBlacklistRemove() {
			name = "remove";
			help = "Removes someone from the blacklist.";
			arguments = "<user>";
			guildOnly = true;
			ownerCommand = false;
			userPermissions = new Permission[] {Permission.ADMINISTRATOR};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			try {
				settings = DataIO.loadJson("data/mod/blacklist.json", Map.class);
				settings = settings == null ? new HashMap<>() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occured while loading the data file.", e);
			}
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else if (!settings.containsKey(event.getGuild().getId())) event.reply("No one in this server is currently blacklisted.");
			else if (!((List) settings.get(event.getGuild().getId())).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
			else {
				((List) settings.get(event.getGuild().getId())).remove(member.getUser().getId());
				try {
					DataIO.saveJson(settings, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while saving the data file.", e);
				}
				event.reply("Successfully removed " + member.getAsMention() + " from this server's blacklist.");
			}
		}
	}

	public static class CommandBlacklistGlobal extends Command {

		public CommandBlacklistGlobal() {
			name = "global";
			help = "Global blacklist commands.";
			arguments = "";
			guildOnly = false;
			ownerCommand = true;
			children = new Command[] {new CommandBlacklistGlobalAdd(), new CommandBlacklistGlobalRemove()};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			Main.sendCommandHelp(event, this);
		}

		public static class CommandBlacklistGlobalAdd extends Command {

			public CommandBlacklistGlobalAdd() {
				name = "add";
				help = "Add someone to the global blacklist.";
				arguments = "<user>";
				guildOnly = false;
				ownerCommand = true;
				category = Main.getCategory("Moderation");
				isSubcommand = true;
			}

			@Override
			protected void execute(CommandEvent event) {
				try {
					settings = DataIO.loadJson("data/mod/blacklist.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the data file.", e);
				}
				Member member = Main.getMemberFromInput(event.getMessage());
				if (member == null) event.reply("That user could not be found.");
				else {
					if (!settings.containsKey("global")) settings.put("global", new ArrayList<>());
					((List) settings.get("global")).add(member.getUser().getId());
					try {
						DataIO.saveJson(settings, "data/mod/blacklist.json");
					} catch (IOException e) {
						throw new RuntimeException("An unknown error occured while saving the data file.", e);
					}
					event.reply("Successfully added " + member.getAsMention() + " to the global blacklist.");
				}
			}
		}

		public static class CommandBlacklistGlobalRemove extends Command {

			public CommandBlacklistGlobalRemove() {
				name = "remove";
				help = "Removes someone from the global blacklist.";
				arguments = "<user>";
				guildOnly = false;
				ownerCommand = true;
				category = Main.getCategory("Moderation");
				isSubcommand = true;
			}

			@Override
			protected void execute(CommandEvent event) {
				try {
					settings = DataIO.loadJson("data/mod/blacklist.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the data file.", e);
				}
				Member member = Main.getMemberFromInput(event.getMessage());
				if (member == null) event.reply("That user could not be found.");
				else if (!settings.containsKey("global")) event.reply("No one is currently globally blacklisted.");
				else if (!((List) settings.get("global")).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
				else {
					((List) settings.get("global")).remove(member.getUser().getId());
					try {
						DataIO.saveJson(settings, "data/mod/blacklist.json");
					} catch (IOException e) {
						throw new RuntimeException("An unknown error occured while saving the data file.", e);
					}
					event.reply("Successfully removed " + member.getAsMention() + " from the global blacklist.");
				}
			}
		}
	}
}
