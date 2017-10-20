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
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CommandGiveme extends Command {

	private static Map settings;

	public CommandGiveme() {
		name = "giveme";
		help = "Give yourself roles.";
		arguments = "<role>";
		guildOnly = true;
		ownerCommand = false;
		botPermissions = new Permission[] {Permission.MANAGE_ROLES};
		children = new Command[] {new CommandGivemeMassadd(), new CommandGivemeList(), new CommandGivemeRemove(), new CommandGivemeGetoff()};
		category = Main.getCategory("Moderation");
		try {
			settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
			settings = settings == null ? new HashMap<>() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().isEmpty()) Main.sendCommandHelp(event, this);
		else {
			try {
				settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
				settings = settings == null ? new HashMap<>() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occured while loading the file.", e);
			}
			if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes yet.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(event.getArgs())) event.reply("That giveme could not be found.");
			else {
				Role giveme = event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get(event.getArgs()));
				if (giveme == null) event.reply("The giveme was found, but the connected role was deleted.");
				else if (!PermissionUtil.canInteract(event.getSelfMember(), giveme)) event.reply("I cannot give you that role, as it is higher in the hierarchy than my highest role.");
				else {
					event.getGuild().getController().addSingleRoleToMember(event.getMember(), giveme).queue();
					event.reply("The role has been added, you're welcome.");
				}
			}
		}
	}

	public static class CommandGivemeMassadd extends Command {

		public CommandGivemeMassadd() {
			name = "add";
			help = "Add givemes, divide the roles with a semi-colon (;).";
			arguments = "<roles>";
			guildOnly = true;
			ownerCommand = false;
			userPermissions = new Permission[] {Permission.ADMINISTRATOR};
			botPermissions = new Permission[] {Permission.MANAGE_ROLES};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			if (event.getArgs().length() != 0) {
				List<Role> roles = new ArrayList<>();
				for (String roleName : event.getArgs().split(event.getArgs().contains("; ") ? "; " : ";"))
					if (event.getGuild().getRolesByName(roleName, true).size() == 0) {event.replyFormatted("The role '%s' could not be found.", roleName); return;}
					else roles.add(event.getGuild().getRolesByName(roleName, true).get(0));
				try {
					settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the file.", e);
				}
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), new HashMap());
				for (Role role : roles)
					((Map) settings.get(event.getGuild().getId())).put(role.getName(), role.getId());
				try {
					DataIO.saveJson(settings, "data/giveme/settings.json");
				} catch (IOException e) {
					event.reply("An unknown error occured while saving the data file.");
					return;
				}
				event.replyFormatted("Successfully added %s giveme%s.",
						roles.size(),
						roles.size() == 1 ? "" : "s");
			} else Main.sendCommandHelp(event, this);
		}
	}

	public static class CommandGivemeList extends Command {

		public CommandGivemeList() {
			name = "list";
			help = "List all givemes in this server.";
			arguments = "";
			guildOnly = true;
			ownerCommand = false;
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			try {
				settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
				settings = settings == null ? new HashMap<>() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occured while loading the file.", e);
			}
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), new HashMap());
			if (((Map) settings.get(event.getGuild().getId())).keySet().isEmpty()) event.reply("This server has no givemes.");
			else event.reply("This server has the following givemes:\n" + Main.joinCustomChar(", ", ((Map) settings.get(event.getGuild().getId())).keySet().toArray(new String[0])));
		}
	}

	public static class CommandGivemeRemove extends Command {

		public CommandGivemeRemove() {
			name = "remove";
			help = "Removes givemes, divide the roles with a semi-colon (;).";
			arguments = "<givemes>";
			guildOnly = true;
			ownerCommand = false;
			userPermissions = new Permission[] {Permission.ADMINISTRATOR};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			if (event.getArgs().length() != 0) {
				try {
					settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the file.", e);
				}
				if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes.");
				else {
					int counter = 0;
					for (String giveme : event.getArgs().split(";"))
						if (!((Map) settings.get(event.getGuild().getId())).containsKey(giveme)) {event.replyFormatted("The giveme '%s' could not be found.", giveme); return;}
						else {
							((Map) settings.get(event.getGuild().getId())).remove(giveme);
							counter += 1;
						}
					try {
						DataIO.saveJson(settings, "data/giveme/settings.json");
					} catch (IOException e) {
						event.reply("An unknown error occured while saving the data file.");
						return;
					}
					event.replyFormatted("Successfully removed %s giveme%s.",
							counter,
							counter == 1 ? "" : "s");
				}
			} else Main.sendCommandHelp(event, this);
		}
	}

	public static class CommandGivemeGetoff extends Command {

		public CommandGivemeGetoff() {
			name = "getoff";
			help = "Removes a giveme from your roles.";
			arguments = "<giveme>";
			guildOnly = true;
			ownerCommand = false;
			botPermissions = new Permission[] {Permission.MANAGE_ROLES};
			category = Main.getCategory("Moderation");
			isSubcommand = true;
		}

		@Override
		protected void execute(CommandEvent event) {
			if (event.getArgs().isEmpty()) Main.sendCommandHelp(event, this);
			else {
				try {
					settings = DataIO.loadJson("data/giveme/settings.json", Map.class);
					settings = settings == null ? new HashMap<>() : settings;
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occured while loading the file.", e);
				}
				if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes yet.");
				else if (!((Map) settings.get(event.getGuild().getId())).containsKey(event.getArgs())) event.reply("That giveme could not be found.");
				else {
					Role giveme = event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get(event.getArgs()));
					if (giveme == null) event.reply("The giveme was found, but the connected role was deleted.");
					else if (!PermissionUtil.canInteract(event.getSelfMember(), giveme)) event.reply("I cannot remove that role from you, as it is higher in the hierarchy than my highest role.");
					else if (!event.getMember().getRoles().contains(giveme)) event.reply("You do not have that giveme on you.");
					else {
						event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), giveme).queue();
						event.reply("The role has been removed, you're welcome.");
					}
				}
			}
		}

	}

}
