package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.CommandPermissionException;
import com.ptsmods.impulse.miscellaneous.SilentCommandEvent;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.Dashboard;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.GenericTextChannelUpdateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.update.GenericVoiceChannelUpdateEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class Moderation {

	private static Map blacklist;
	private static Map settings;
	private static Map filters;
	private static Map givemeSettings;
	private static Map warnerSettings;
	private static Map<String, Map<String, Object>> modlogSettings;
	private static Map pastNicks;
	private static Map pastNames;
	private static Map<String, List<Message>> messages = new HashMap();
	private static Map<String, Map<String, Object>> loggedMessages = new HashMap();
	private static Map<String, Channel> channels = new HashMap();
	private static Map<String, Role> roles = new HashMap();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread("Moderation file saving shutdown hook") {
			@Override
			public void run() {
				Main.print(LogType.DEBUG, "Shutting down, saving moderation files...");
				saveFiles();
			}
		});
		try {
			settings = DataIO.loadJsonOrDefault("data/mod/settings.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the settings.json file.", e);
		}
		try {
			filters = DataIO.loadJsonOrDefault("data/mod/filters.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the filters.json file.", e);
		}
		try {
			blacklist = DataIO.loadJsonOrDefault("data/mod/blacklist.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the blacklist.json file.", e);
		}
		Main.addCommandHook(event -> {
			Map blacklist = Moderation.getBlacklist();
			if (event.getGuild() != null && blacklist.containsKey(event.getGuild().getId()) && ((List) blacklist.get(event.getGuild().getId())).contains(event.getAuthor().getId())) throw new CommandPermissionException("You're blacklisted in this server.");
			else if (blacklist.containsKey("global") && ((List) blacklist.get("global")).contains(event.getAuthor().getId())) throw new CommandPermissionException("You're globally blacklisted.");
		});
		Main.addCommandHook(event -> {
			if (event.getGuild() != null)
				if (event.getCommand().isAnnotationPresent(Command.class)) {
					Command command = event.getCommand().getAnnotation(Command.class);
					if (!Dashboard.getEnabledModules(event.getGuild()).contains(command.category().toLowerCase()) && command.obeyDashboard())
						throw new CommandPermissionException("That command can not be used as its category, %s, isn't enabled.", command.category());
				} else if (event.getCommand().isAnnotationPresent(Subcommand.class)) {
					Subcommand command = event.getCommand().getAnnotation(Subcommand.class);
					if (!Dashboard.getEnabledModules(event.getGuild()).contains(Main.getAbsoluteParentCommand(command).getAnnotation(Command.class).category().toLowerCase()) && command.obeyDashboard())
						throw new CommandPermissionException("That command can not be used as its category, %s, isn't enabled.", Main.getAbsoluteParentCommand(command).getAnnotation(Command.class).category());
				}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> saveFiles()));
		try {
			pastNicks = DataIO.loadJsonOrDefault("data/mod/pastNicks.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the pastNicks.json file.", e);
		}
		try {
			pastNames = DataIO.loadJsonOrDefault("data/mod/pastNames.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the pastNames.json file.", e);
		}
		try {
			warnerSettings = DataIO.loadJsonOrDefault("data/warner/warnerSettings.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("There was an error while loading the warnerSettings.json file.", e);
		}
		try {
			modlogSettings = DataIO.loadJsonOrDefault("data/mod/modlog.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the modlog.json file.", e);
		}
		try {
			givemeSettings = DataIO.loadJsonOrDefault("data/mod/givemeSettings.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the modlog.json file.", e);
		}
		try {
			loggedMessages = DataIO.loadJsonOrDefault("data/mod/loggedMessages.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the loggedMessages.json file.", e);
		}
		for (String message : new ArrayList<>(loggedMessages.keySet()))
			if (System.currentTimeMillis()-(double) loggedMessages.get(message).get("sent") > 1000*60*60*24*7*2) loggedMessages.remove(message); // removing messages older than 2 weeks.
		try {
			DataIO.saveJson(loggedMessages, "data/mod/loggedMessages.json");
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while saving the loggedMessages.json file.", e);
		}
	}

	@SubscribeEvent
	public static void onFullyBooted() {
		for (TextChannel channel : Main.getTextChannels())
			channels.put(channel.getId(), Main.cloneChannel(channel));
		for (VoiceChannel channel : Main.getVoiceChannels())
			channels.put(channel.getId(), Main.cloneChannel(channel));
		for (Role role : Main.getRoles())
			roles.put(role.getId(), Main.cloneRole(role));
	}

	@Command(category = "Moderation", help = "Ban someone.", name = "ban", arguments = "<user>", botPermissions = {Permission.BAN_MEMBERS}, userPermissions = {Permission.BAN_MEMBERS})
	public static void ban(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot ban that user as they're higher in the hierarchy than I am.");
			else {
				event.getGuild().getController().ban(member.getUser(), 1).queue();
				event.reply("Successfully banned " + member.getEffectiveName() + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Kick a user.", name = "kick", userPermissions = {Permission.KICK_MEMBERS}, botPermissions = {Permission.KICK_MEMBERS})
	public static void kick(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot kick that user as they're higher in the hierarchy than I am.");
			else {
				event.getGuild().getController().kick(member).complete();
				event.reply("Successfully kicked " + member.getEffectiveName() + ".");
				logModAction(member.getUser(), event.getAuthor(), event.getGuild(), "Kick", "boot");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Mute someone.", name = "mute", botPermissions = {Permission.MANAGE_ROLES}, userPermissions = {Permission.MANAGE_ROLES}, guildOnly = true, arguments = "<user>")
	public static void mute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot mute that user as they're higher in the hierarchy than I am.");
			else {
				Main.mute(member);
				event.reply("Successfully muted " + member.getEffectiveName() + ".");
				logModAction(member.getUser(), event.getAuthor(), event.getGuild(), "Mute", "mute");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Unmute someone.", name = "unmute", botPermissions = {Permission.MANAGE_ROLES}, userPermissions = {Permission.MANAGE_ROLES}, guildOnly = true, arguments = "<user>")
	public static void unmute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else if (!settings.containsKey(event.getGuild().getId()) || !((List) ((Map) settings.get(event.getGuild().getId())).get("mutes")).contains(member.getUser().getId())) event.reply("That user has not been muted using this bot.");
			else if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot unmute that user as they're higher in the hierarchy than I am.");
			else {
				Main.unmute(member);
				event.reply("Successfully unmuted " + member.getEffectiveName() + ".");
				logModAction(member.getUser(), event.getAuthor(), event.getGuild(), "Unmute", "speaker");
			}
		} else Main.sendCommandHelp(event);
	}

	private static void logModAction(User user, User moderator, Guild guild, String actionType, String emoji) {
		if (settings.containsKey(guild.getId())) {
			if (actionType.toLowerCase().equals("mute"))
				((List) ((Map) settings.get(guild.getId())).get("mutes")).add(user.getId());
			TextChannel channel = null;
			try {
				channel = guild.getTextChannelById((String) ((Map) settings.get(guild.getId())).get("channel"));
			} catch (Exception ignored) {}
			int caseNum = Main.getIntFromPossibleDouble(((Map) ((Map) settings.get(guild.getId())).get("cases")).size()) + 1;
			if (channel != null) {
				String messageId = channel.sendMessageFormat("**Case #%s** | %s :%s:\n"
						+ "**User:** %s (%s)\n"
						+ "**Moderator:** %s\n"
						+ "**Reason:** Unknown, type %sreason %s <reason> to add it.",
						caseNum, actionType, emoji,
						Main.str(user), user.getId(),
						moderator == null ? "Unknown" : moderator.getName() + "#" + moderator.getDiscriminator() + " (" + moderator.getId() + ")",
								Main.getPrefix(guild), caseNum).complete().getId();
				((Map) ((Map) settings.get(guild.getId())).get("cases")).put("" + caseNum, Main.newHashMap(new String[] {"messageId", "user", "moderator", "reason", "caseNum"}, new Object[] {messageId, user.getId(), moderator == null ? "" : moderator.getId(), "Unknown", caseNum}));
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occurred while saving the data file.", e);
				}
			}
		}
	}

	@Command(category = "Moderation", help = "Give a reason to a case which was created when doing either [p]ban, [p]kick, or [p]mute.", name = "reason", userPermissions = {Permission.KICK_MEMBERS}, arguments = "<casenum> <reason>", guildOnly = true)
	public static void reason(CommandEvent event) {
		if (event.getArgs().split(" ").length >= 2 && Main.isInteger(event.getArgs().split(" ")[0])) {
			if (settings.containsKey(event.getGuild().getId())) {
				Map cases = (Map) ((Map) settings.get(event.getGuild().getId())).get("cases");
				if (!cases.isEmpty() && cases.size() >= Integer.parseInt(event.getArgs().split(" ")[0])) {
					Map caseM = (Map) cases.get(event.getArgs().split(" ")[0]);
					TextChannel channel = event.getGuild().getTextChannelById(((Map) settings.get(event.getGuild().getId())).get("channel").toString());
					if (channel != null) {
						Message message = channel.getMessageById(caseM.get("messageId").toString()).complete();
						if (message != null) {
							if (!message.getAuthor().getId().equals(event.getSelfMember().getUser().getId())) event.reply("I cannot edit the message attached to that case.");
							else {
								message.editMessage(message.getRawContent().substring(0, message.getRawContent().lastIndexOf("**") + 3) + Main.join(Main.removeArg(event.getArgs().split(" "), 0))).queue();
								event.reply("The case has been updated.");
							}
						} else event.reply("The message attached to that case could not be found.");
					} else event.reply("The logging channel could not be found.");
				} else event.reply("That case could not be found.");
			} else event.reply("The moderation settings for the server have not yet been set up.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Manage the blackist.", name = "blacklist")
	public static void blacklist(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add a user to this server's blacklist.", name = "add", arguments = "<user>", parent = "com.ptsmods.impulse.commands.Moderation.blacklist", guildOnly = true)
	public static void blacklistAdd(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else {
				if (!blacklist.containsKey(event.getGuild().getId())) blacklist.put(event.getGuild().getId(), new ArrayList<>());
				((List) blacklist.get(event.getGuild().getId())).add(member.getUser().getId());
				try {
					DataIO.saveJson(blacklist, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("Successfully added " + member.getAsMention() + " to this server's blacklist.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Removes a user from the blacklist.", name = "remove", arguments = "<user>", parent = "com.ptsmods.impulse.commands.Moderation.blacklist", guildOnly = true)
	public static void blacklistRemove(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else if (!blacklist.containsKey(event.getGuild().getId())) event.reply("No one in this server is currently blacklisted.");
			else if (!((List) blacklist.get(event.getGuild().getId())).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
			else {
				((List) blacklist.get(event.getGuild().getId())).remove(member.getUser().getId());
				try {
					DataIO.saveJson(blacklist, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("Successfully removed " + member.getAsMention() + " from this server's blacklist.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Manage the global blacklist.", name = "global", parent = "com.ptsmods.impulse.commands.Moderation.blacklist", ownerCommand = true)
	public static void blacklistGlobal(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add a user to the global blacklist.", name = "add", arguments = "<user>", parent = "com.ptsmods.impulse.commands.Moderation.blacklistGlobal", ownerCommand = true)
	public static void blacklistGlobalAdd(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else {
				if (!blacklist.containsKey("global")) blacklist.put("global", new ArrayList<>());
				((List) blacklist.get("global")).add(member.getUser().getId());
				try {
					DataIO.saveJson(blacklist, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("Successfully added " + member.getAsMention() + " to the global blacklist.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Removes a user from the global blacklist.", name = "remove", arguments = "<user>", parent = "com.ptsmods.impulse.commands.Moderation.blacklistGlobal", ownerCommand = true)
	public static void blacklistGlobalRemove(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That user could not be found.");
			else if (!blacklist.containsKey("global")) event.reply("No one is currently globally blacklisted.");
			else if (!((List) blacklist.get("global")).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
			else {
				((List) blacklist.get("global")).remove(member.getUser().getId());
				try {
					DataIO.saveJson(blacklist, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("Successfully removed " + member.getAsMention() + " from the global blacklist.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Manage the filters.", name = "filter", guildOnly = true)
	public static void filter(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add a filter to the list of filters.", name = "add", parent = "com.ptsmods.impulse.commands.Moderation.filter", arguments = "<filter>", userPermissions = {Permission.MESSAGE_MANAGE}, guildOnly = true)
	public static void filterAdd(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!filters.containsKey(event.getGuild().getId())) filters.put(event.getGuild().getId(), Main.newHashMap(new String[] {"filtered", "message", "disabled"}, new Object[] {new ArrayList(), "{USER} you're not allowed to say that!", false}));
			((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).add(event.getArgs());
			try {
				DataIO.saveJson(filters, "data/mod/filters.json");
			} catch (IOException e) {
				throw new CommandException("There was an error while saving the data file.", e);
			}
			event.reply("Successfully added it to the list of filtered items.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Remove a filter from the list of filters.", name = "remove", parent = "com.ptsmods.impulse.commands.Moderation.filter", arguments = "<filter>", userPermissions = {Permission.MESSAGE_MANAGE}, guildOnly = true)
	public static void filterRemove(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!filters.containsKey(event.getGuild().getId())) event.reply("Nothing is being filtered in this server.");
			else if (!((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).contains(event.getArgs())) event.reply("The given message is currently not being filtered.");
			else {
				((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).remove(event.getArgs());
				try {
					DataIO.saveJson(filters, "data/mod/filters.json");
				} catch (IOException e) {
					throw new CommandException("There was an error while saving the data file.", e);
				}
				event.reply("Successfully removed it from the list of filtered items.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Lists all filters in this server.", name = "list", parent = "com.ptsmods.impulse.commands.Moderation.filter", guildOnly = true)
	public static void filterList(CommandEvent event) {
		if (filters.containsKey(event.getGuild().getId()) && !((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).isEmpty())
			event.reply("Currently the following messages are being filtered in this server: `" + Main.joinCustomChar("`, `", (List) ((Map) filters.get(event.getGuild().getId())).get("filtered")) + "`.");
		else event.reply("Nothing is being filtered in this server.");
	}

	@Command(category = "Moderation", help = "Give yourself roles.", name = "giveme", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<giveme>")
	public static void giveme(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (!givemeSettings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes yet.");
			else {
				String givemeName = "";
				for (String giveme : ((Map<String, Object>) givemeSettings.get(event.getGuild().getId())).keySet())
					if (giveme.equalsIgnoreCase(event.getArgs())) {
						givemeName = giveme;
						break;
					}
				if (!((Map) givemeSettings.get(event.getGuild().getId())).containsKey(givemeName)) event.reply("That giveme could not be found.");
				else {
					Role giveme = event.getGuild().getRoleById((String) ((Map) givemeSettings.get(event.getGuild().getId())).get(givemeName));
					if (giveme == null) event.reply("The givemes was found, but the connected role was deleted.");
					else if (!PermissionUtil.canInteract(event.getSelfMember(), giveme)) event.reply("I cannot give you that role, as it is higher in the hierarchy than my highest role.");
					else {
						event.getGuild().getController().addSingleRoleToMember(event.getMember(), giveme).queue();
						event.reply("The role has been added, you're welcome.");
					}
				}
			}
		} else
			Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add givemes, divide the roles with a semi-colon (;).", name = "add", parent = "com.ptsmods.impulse.commands.Moderation.giveme", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES}, arguments = "<roles>")
	public static void givemeAdd(CommandEvent event) {
		if (!event.argsEmpty()) {
			List<Role> roles = new ArrayList<>();
			for (String roleName : event.getArgs().split(event.getArgs().contains("; ") ? "; " : ";"))
				if (event.getGuild().getRolesByName(roleName, true).size() == 0) {event.reply("The role '%s' could not be found.", roleName); return;}
				else roles.add(event.getGuild().getRolesByName(roleName, true).get(0));
			if (!givemeSettings.containsKey(event.getGuild().getId())) givemeSettings.put(event.getGuild().getId(), new HashMap());
			for (Role role : roles)
				((Map) givemeSettings.get(event.getGuild().getId())).put(role.getName(), role.getId());
			try {
				DataIO.saveJson(givemeSettings, "data/mod/givemeSettings.json");
			} catch (IOException e) {
				event.reply("An unknown error occurred while saving the data file.");
				return;
			}
			event.reply("Successfully added %s giveme%s.",
					roles.size(),
					roles.size() == 1 ? "" : "s");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Lists all givemes in this server.", name = "list", parent = "com.ptsmods.impulse.commands.Moderation.giveme", guildOnly = true)
	public static void givemeList(CommandEvent event) {
		if (!givemeSettings.containsKey(event.getGuild().getId())) givemeSettings.put(event.getGuild().getId(), new HashMap());
		if (((Map) givemeSettings.get(event.getGuild().getId())).keySet().isEmpty()) event.reply("This server has no givemes.");
		else event.reply("This server has the following givemes:\n" + Main.joinCustomChar(", ", ((Map) givemeSettings.get(event.getGuild().getId())).keySet().toArray(new String[0])));
	}

	@Subcommand(help = "Remove a giveme.", name = "remove", parent = "com.ptsmods.impulse.commands.Moderation.giveme", guildOnly = true, arguments = "<giveme>")
	public static void givemeRemove(CommandEvent event) {
		if (!event.argsEmpty()) {
			if (!givemeSettings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes.");
			else {
				int counter = 0;
				for (String giveme : event.getArgs().split(";"))
					if (!((Map) givemeSettings.get(event.getGuild().getId())).containsKey(giveme)) {event.reply("The giveme '%s' could not be found.", giveme); return;}
					else {
						((Map) givemeSettings.get(event.getGuild().getId())).remove(giveme);
						counter += 1;
					}
				try {
					DataIO.saveJson(givemeSettings, "data/mod/givemeSettings.json");
				} catch (IOException e) {
					event.reply("An unknown error occurred while saving the data file.");
					return;
				}
				event.reply("Successfully removed %s giveme%s.",
						counter,
						counter == 1 ? "" : "s");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Remove a giveme from your roles.", name = "getoff", parent = "com.ptsmods.impulse.commands.Moderation.giveme", arguments = "<giveme>", botPermissions = {Permission.MANAGE_ROLES})
	public static void givemeGetoff(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (!givemeSettings.containsKey(event.getGuild().getId())) event.reply("This server has no givemes yet.");
			else {
				String givemeName = "";
				for (String giveme : ((Map<String, Object>) givemeSettings.get(event.getGuild().getId())).keySet())
					if (giveme.equalsIgnoreCase(event.getArgs())) {
						givemeName = giveme;
						break;
					}
				if (!((Map) givemeSettings.get(event.getGuild().getId())).containsKey(givemeName)) event.reply("That giveme could not be found.");
				else {
					Role giveme = event.getGuild().getRoleById((String) ((Map) givemeSettings.get(event.getGuild().getId())).get(event.getArgs()));
					if (giveme == null) event.reply("The giveme was found, but the connected role was deleted.");
					else if (!PermissionUtil.canInteract(event.getSelfMember(), giveme)) event.reply("I cannot remove that role from you, as it is higher in the hierarchy than my highest role.");
					else if (!event.getMember().getRoles().contains(giveme)) event.reply("You do not have that giveme on you.");
					else {
						event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), giveme).queue();
						event.reply("The role has been removed, you're welcome.");
					}
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Makes the bot leave.", name = "leave", guildOnly = true)
	public static void leave(CommandEvent event) {
		event.reply("Are you sure you want me to go? (yes/no)");
		Message response = Main.waitForInput(event.getGuild().getMember(event.getAuthor()), event.getChannel(), 15000);
		if (response == null)
			event.reply("No response gotten, guess I'll stay.");
		else if (response.getContent().toLowerCase().startsWith("ye")) {
			event.reply("Kk, bye! :wave:");
			event.getGuild().leave().queue();
		} else event.reply("Guess I'll stay.");
	}

	@Command(category = "Moderation", help = "Manage moderation settings.", name = "modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modset(CommandEvent event) {
		String serverPrefix = "not set";
		String channel = "not set";
		String banMentionSpam = "disabled";
		String autorole = "not set";
		String autoroleEnabled = "false";
		String greeting = "";
		String farewell = "";
		String welcomeChannel = "not set";
		String toDms = "false";
		if (settings.containsKey(event.getGuild().getId())) {
			serverPrefix = (String) ((Map) settings.get(event.getGuild().getId())).get("serverPrefix");
			serverPrefix = serverPrefix == null || serverPrefix.isEmpty() ? "not set" : serverPrefix;
			String channelId = (String) ((Map) settings.get(event.getGuild().getId())).get("channel");
			channel = channelId.isEmpty() ? channel : event.getGuild().getTextChannelById(channelId) == null ? channel : event.getGuild().getTextChannelById(channelId).getAsMention();
			banMentionSpam = (boolean) ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam") ? "enabled" : banMentionSpam;
			autorole = ((Map) settings.get(event.getGuild().getId())).get("autorole").toString().isEmpty() || event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get("autorole")) == null ? "not set" : event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get("autorole")).getName();
			autoroleEnabled = ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled").toString();
			greeting = ((Map) settings.get(event.getGuild().getId())).get("greeting").toString();
			farewell = ((Map) settings.get(event.getGuild().getId())).get("farewell").toString();
			String welcomeChannelId = (String) ((Map) settings.get(event.getGuild().getId())).get("channel");
			welcomeChannel = welcomeChannelId.isEmpty() ? welcomeChannel : event.getGuild().getTextChannelById(welcomeChannelId) == null ? welcomeChannel : event.getGuild().getTextChannelById(welcomeChannelId).getAsMention();
			toDms = ((Map) settings.get(event.getGuild().getId())).get("dm").toString();
		}
		Main.sendCommandHelp(event, String.format("**Server prefix**: %s\n**Channel**: %s\n**Ban mention spam**: %s\n**Autorole**: %s\n**Autorole enabled**: %s\n**Greeting**: %s\n**Farewell**: %s\n**Welcome channel**: %s\n**Welcome in DMs**: %s", serverPrefix, channel, banMentionSpam, autorole, autoroleEnabled, greeting, farewell, welcomeChannel, toDms));
	}

	@Subcommand(help = "Sets this server's prefix.", name = "serverprefix", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetServerPrefix(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", "", true, false, event.getArgs(), "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", new HashMap(), new ArrayList()}));
			else ((Map) settings.get(event.getGuild().getId())).put("serverPrefix", event.getArgs());
			try {
				DataIO.saveJson(settings, "data/mod/settings.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the data file.", e);
			}
			event.reply("This server's prefix has been set to '" + event.getArgs() + "'.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the channel to which kicks, bans and mutes are logged to.", name = "channel", parent = "com.ptsmods.impulse.commands.Moderation.modset", arguments = "<channel>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetChannel(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			TextChannel channel;
			if (!event.getMessage().getMentionedChannels().isEmpty()) channel = event.getMessage().getMentionedChannels().get(0);
			else channel = event.getGuild().getTextChannelsByName(event.getArgs(), true).get(0);
			if (channel == null) event.reply("The given channel could not be found.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {channel.getId(), "", true, false, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}));
				else ((Map) settings.get(event.getGuild().getId())).put("channel", channel.getId());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully set the channel to " + channel.getAsMention() + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Sets if the bot should ban users who sent a message which mentions more than 8 users.", name = "banmentionspam", parent = "com.ptsmods.impulse.commands.Moderation.modset", arguments = "<flag>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetBanMentionSpam(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "serverPrefix", "cases", "mutes"}, new Object[] {"", "", true, true, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}));
			else ((Map) settings.get(event.getGuild().getId())).put("banMentionSpam", !(boolean) ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam"));
			try {
				DataIO.saveJson(settings, "data/mod/settings.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while loading the data file.", e);
			}
			event.reply("Successfully toggled banMentionSpam to " + ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam") + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the role members should automatically get assigned when they join.", name = "autorole", parent = "com.ptsmods.impulse.commands.Moderation.modset", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES}, arguments = "<role>")
	public static void modsetAutorole(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Role role = null;
			if (!event.getMessage().getMentionedRoles().isEmpty()) role = event.getMessage().getMentionedRoles().get(0);
			else role = event.getGuild().getRolesByName(event.getArgs(), true).isEmpty() ? null : event.getGuild().getRolesByName(event.getArgs(), true).get(0);
			if (role == null) event.reply("The given role could not be found.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", role.getId(), true, false, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}));
				else ((Map) settings.get(event.getGuild().getId())).put("autorole", role.getId());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully set the autorole to " + role.getName() + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Toggle whether new members should automatically get a role assigned when they join.", name = "toggleautorole", parent = "com.ptsmods.impulse.commands.Moderation.modset", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES})
	public static void modsetToggleAutorole(CommandEvent event) throws CommandException {
		if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "serverPrefix", "cases", "mutes"}, new Object[] {"", "", false, false, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}));
		else ((Map) settings.get(event.getGuild().getId())).put("autoroleEnabled", !(boolean) ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled"));
		try {
			DataIO.saveJson(settings, "data/mod/settings.json");
		} catch (IOException e) {
			throw new CommandException("An unknown error occurred while loading the data file.", e);
		}
		event.reply("Successfully toggled autoroleEnabled to " + ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled") + ".");
	}

	@Subcommand(help = "Set the message that should be sent when a new user joins.\nYou can use variables in this such as:\nUSER this'll be the user's name and discriminator.\nUSER_MENTION this'll mention the user.\nSERVER this'll be the server's name.", name = "greeting", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetGreeting(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			User user = event.getAuthor();
			Guild guild = event.getGuild();
			event.reply("Testing new greeting:\n\n%s\n\nWould you like to keep it? (yes/no)",
					event.getArgs()
					.replaceAll("USER_MENTION", user.getAsMention())
					.replaceAll("USER", Main.str(user))
					.replaceAll("SERVER", Main.str(guild)));
			Message response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
			if (response == null) event.reply("No response gotten, guess not.");
			else if (!response.getContent().toLowerCase().startsWith("ye")) event.reply("Answer was not yes, guess not.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", "", true, false, "", event.getArgs(), "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}));
				else ((Map) settings.get(event.getGuild().getId())).put("greeting", event.getArgs());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully set the greeting to " + ((Map) settings.get(event.getGuild().getId())).get("greeting") + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the message that should be sent when an old user leaves.\nYou can use variables in this such as:\nUSER this'll be the user's name and discriminator.\nUSER_MENTION this'll mention the user.\nSERVER this'll be the server's name.", name = "farewell", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetFarewell(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			User user = event.getAuthor();
			Guild guild = event.getGuild();
			event.reply("Testing new farewell:\n\n%s\n\nWould you like to keep it? (yes/no)",
					event.getArgs()
					.replaceAll("USER_MENTION", user.getAsMention())
					.replaceAll("USER", Main.str(user))
					.replaceAll("SERVER", Main.str(guild)));
			Message response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
			if (response == null) event.reply("No response gotten, guess not.");
			else if (!response.getContent().toLowerCase().startsWith("ye")) event.reply("Answer was not yes, guess not.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", "", true, false, "", "Welcome to **SERVER**, **USER_MENTION**!", event.getArgs(), "", false, new HashMap(), new ArrayList()}));
				else ((Map) settings.get(event.getGuild().getId())).put("farewell", event.getArgs());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully set the farewell to " + ((Map) settings.get(event.getGuild().getId())).get("farewell") + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Sets whether greetings and farewells should be sent in DMs.", name = "dm", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetDm(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", "", true, false, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", true, new HashMap(), new ArrayList()}));
			else ((Map) settings.get(event.getGuild().getId())).put("dm", !(boolean) ((Map) settings.get(event.getGuild().getId())).get("dm"));
			try {
				DataIO.saveJson(settings, "data/mod/settings.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while loading the data file.", e);
			}
			event.reply("Successfully toggled sending in DMs to " + ((Map) settings.get(event.getGuild().getId())).get("dm") + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Sets the channel in which the bot should greet and farewell users.", name = "welcomechannel", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetWelcomeChannel(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			TextChannel channel = null;
			if (!event.getMessage().getMentionedChannels().isEmpty()) channel = event.getMessage().getMentionedChannels().get(0);
			else channel = Main.getOrDefault(event.getGuild().getTextChannelsByName(event.getArgs(), true), 0, null);
			if (channel == null) event.reply("The given channel could not be found.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"}, new Object[] {"", "", true, false, "", "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", channel.getId(), false, new HashMap(), new ArrayList()}));
				else ((Map) settings.get(event.getGuild().getId())).put("welcomeChannel", channel.getId());
				try {
					DataIO.saveJson(settings, "data/mod/settings.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully set the channel to " + channel.getAsMention() + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Disable the welcome system.", name = "disable", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetDisableGreeting(CommandEvent event) throws CommandException {
		if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has not yet set up the welcome system.");
		else ((Map) settings.get(event.getGuild().getId())).put("welcomeChannel", "");
		try {
			DataIO.saveJson(settings, "data/mod/settings.json");
		} catch (IOException e) {
			throw new CommandException("An unknown error occurred while loading the data file.", e);
		}
		event.reply("Successfully disabled the welcome system, to enable it again type %smodset welcomechannel <channel>.", Main.getPrefix(event.getGuild()));
	}

	@Command(category = "Moderation", help = "Shows you the past names of a user.", name = "pastnames", arguments = "<user>", guildOnly = true)
	public static void pastNames(CommandEvent event) {
		if (!event.argsEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("That user could not be found.");
			else if (!pastNames.containsKey(user.getId())) event.reply("No data could be found for that user.");
			else event.reply("Past names:\n" + Main.joinCustomChar(", ", ((List<String>) pastNames.get(user.getId())).toArray(new String[0])));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Shows you the past names of a user.", name = "pastnicks", arguments = "<user>", guildOnly = true)
	public static void pastNicks(CommandEvent event) {
		if (!event.argsEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("That user could not be found.");
			else if (!pastNicks.containsKey(user.getId())) event.reply("No data could be found for that user.");
			else event.reply("Past nicks:\n" + Main.joinCustomChar(", ", ((List<String>) pastNicks.get(user.getId())).toArray(new String[0])));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Renames a user.", name = "rename", arguments = "<user> <newName>", guildOnly = true)
	public static void rename(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("That member could not be found.");
			else if (!PermissionUtil.canInteract(event.getSelfMember(), member)) event.reply("I cannot rename that user as they're higher in the hierarchy than I am.");
			else {
				String newName = "";
				if (event.getMessage().getMentionedUsers().isEmpty())
					newName = Main.join(Main.removeArgs(event.getArgs().split(" "), Main.range(member.getUser().getName().split(" ").length)));
				else
					newName = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
				event.getGuild().getController().setNickname(member, newName).queue();
				event.reply("%s's nickname %s.", member.getAsMention(), newName.isEmpty() ? "has been reset" : "has been changed to " + newName);
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Reset the warnings of a user.", name = "resetwarns", arguments = "<user>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void resetWarns(CommandEvent event) {
		if (!event.argsEmpty()) {
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!warnerSettings.containsKey(guildId)) event.reply("No one in this server currently has any warnings.");
			else if (!((Map) warnerSettings.get(guildId)).containsKey(userId)) event.reply("That user has no warnings.");
			else {
				((Map) warnerSettings.get(guildId)).remove(userId);
				if (((Map) warnerSettings.get(guildId)).isEmpty()) warnerSettings.remove(guildId);
				try {
					DataIO.saveJson(warnerSettings, "data/warner/settings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the data file.");
					return;
				}
				event.reply(event.getGuild().getMemberById(userId).getEffectiveName() + "'s warnings have been reset.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Warn a user for their behavior.", name = "warn", userPermissions = {Permission.KICK_MEMBERS}, botPermissions = {Permission.MANAGE_PERMISSIONS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS}, arguments = "<user>", guildOnly = true)
	public static void warn(CommandEvent event) {
		if (!event.argsEmpty()) {
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!warnerSettings.containsKey(guildId)) warnerSettings.put(guildId, new HashMap<>());
			if (!event.getGuild().getSelfMember().canInteract(member))
				event.reply("I cannot mute, kick, or ban that user as they're higher in the hierarchy than I am.");
			else if (!((Map) warnerSettings.get(guildId)).containsKey(userId)) {
				((Map) warnerSettings.get(guildId)).put(userId, 1);
				event.reply(member.getAsMention() + " has **1 warning**, but nothing happens yet. Next up: **5 minute mute**.");
				try {
					DataIO.saveJson(warnerSettings, "data/warner/warnerSettings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the file.");
					e.printStackTrace();
					return;
				}
			} else {
				Integer warnings = 1;
				try {
					warnings = (Integer) ((Map) warnerSettings.get(guildId)).get(userId) + 1;
				} catch (ClassCastException e) {
					warnings = ((Double) ((Map) warnerSettings.get(guildId)).get(userId)).intValue() + 1;
				}
				((Map) warnerSettings.get(guildId)).put(userId, warnings);
				String output = "";
				switch (warnings) {
				case 0: {output = "%s has **0 warnings**, dafuq? :thinking:"; break;}
				case 1: {output = "%s has **1 warning**, but nothing happens yet. Next up: **5 minute mute**."; break;}
				case 2: {output = "%s has **2 warnings** and has been muted for 5 minutes. Next up: **30 minute mute**."; mute(member, 5); break;}
				case 3: {output = "%s has **3 warnings** and has been muted for 30 minutes. Next up: **kick**."; mute(member, 30); break;}
				case 4: {output = "%s has **4 warnings** and has been kicked. Next up: **ban**."; event.getGuild().getController().kick(member).complete(); break;}
				case 5: {output = "%s has **5 warnings** and has been banned."; resetWarns(new SilentCommandEvent(event)); event.getGuild().getController().ban(member, 1).complete(); break;}
				default: {output = "%s has **an unknown** amount of warnings. :thinking:"; break;}
				}
				try {
					DataIO.saveJson(warnerSettings, "data/warner/warnerSettings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the data file.");
					e.printStackTrace();
					return;
				}
				event.reply(String.format(output, member.getAsMention()));
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Tells you how many warnings a user has.", name = "warns", arguments = "<user>", guildOnly = true)
	public static void warns(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!warnerSettings.containsKey(guildId)) event.reply("No one in this server currently has any warnings.");
			else if (!((Map) warnerSettings.get(guildId)).containsKey(userId)) event.reply("That user has no warnings.");
			else {
				int warns = Main.getIntFromPossibleDouble(((Map) warnerSettings.get(guildId)).get(userId));
				event.reply(event.getGuild().getMemberById(userId).getEffectiveName() + " has " + warns + " warning" + (warns != 1 ? "s" : "") + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Manage settings for the modlog.", name = "modlogset", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void modlogset(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the channel modlog should log to.", name = "channel", parent = "com.ptsmods.impulse.commands.Moderation.modlogset", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true, arguments = "<channel>")
	public static void modlogsetChannel(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			TextChannel channel;
			if (!event.getMessage().getMentionedChannels().isEmpty()) channel = event.getMessage().getMentionedChannels().get(0);
			else channel = Main.getOrDefault(event.getGuild().getTextChannelsByName(event.getArgs(), true), 0, null);
			if (channel != null) {
				if (!modlogSettings.containsKey(event.getGuild().getId())) modlogSettings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "enabled"}, new Object[] {channel.getId(), true}));
				else modlogSettings.get(event.getGuild().getId()).put("channel", channel.getId());
				try {
					DataIO.saveJson(modlogSettings, "data/mod/modlog.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the modlog file.", e);
				}
				event.reply("Successfully set the modlog channel to %s.", channel.getAsMention());
			} else event.reply("The given channel could not be found.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Toggle whether the modlog is enabled.\nThe enabled arg should be either true or false.", name = "toggle", parent = "com.ptsmods.impulse.commands.Moderation.modlogset", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true, arguments = "<enabled>")
	public static void modlogsetToggle(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			boolean enabled = Boolean.valueOf(event.getArgs().split(" ")[0]);
			if (!modlogSettings.containsKey(event.getGuild().getId())) modlogSettings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "enabled"}, new Object[] {"", true}));
			else modlogSettings.get(event.getGuild().getId()).put("enabled", enabled);
			try {
				DataIO.saveJson(modlogSettings, "data/mod/modlog.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the modlog file.", e);
			}
			event.reply("Successfully toggled the modlog to %s.", enabled);
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Manage your dashboard settings.", name = "dashboard", guildOnly = true, userPermissions = {Permission.ADMINISTRATOR}, obeyDashboard = false)
	public static void dashboard(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Create a key to use on <https://dashboard.impulsebot.com>.", name = "getkey", parent = "com.ptsmods.impulse.commands.Moderation.dashboard", guildOnly = true, userPermissions = {Permission.ADMINISTRATOR}, obeyDashboard = false)
	public static void dashboardGetKey(CommandEvent event) throws CommandException {
		if (!Dashboard.hasKey(event.getMember())) {
			try {
				event.replyInDM("Your dashboard key is %s, hop on https://dashboard.impulsebot.com to log in.", Dashboard.createKey(event.getMember()));
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while creating your dashboard key.", e);
			}
			event.reply("Your dashboard key has been sent to you in your DMs.");
		} else {
			event.replyInDM("Your dashboard key is %s, hop on https://dashboard.impulsebot.com to log in.", Dashboard.getKey(event.getMember()));
			event.reply("You already have a dashboard key, in case you lost it, it has been sent to you in your DMs.");
		}
	}

	@Command(category = "Moderation", help = "Mass-delete messages.\nArg 'amount' can only be a maximum of 99.", name = "purge", botPermissions = {Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY}, userPermissions = {Permission.MESSAGE_MANAGE}, guildOnly = true, arguments = "<amount>")
	public static void purge(CommandEvent event) {
		if (!event.argsEmpty() && Main.isInteger(event.getArgs()) && Integer.parseInt(event.getArgs()) < 100 && Integer.parseInt(event.getArgs()) > 0) {
			long minEpoch = System.currentTimeMillis() / 1000 - 86400 * 14; // current time - 2 weeks
			List<String> ids = new ArrayList();
			event.getTextChannel().getHistory().retrievePast(Integer.parseInt(event.getArgs()) + 1).complete().forEach(m -> {if (m.getCreationTime().toEpochSecond() > minEpoch) ids.add(m.getId());});
			event.getTextChannel().deleteMessagesByIds(ids).queue();
			event.reply("Successfully deleted %s messages.", ids.size()-1);
		} else Main.sendCommandHelp(event);
	}

	private static void log(Guild guild, String emote, String name, String content, Object... formatArgs) {
		if (modlogSettings.containsKey(guild.getId()) && (boolean) modlogSettings.get(guild.getId()).get("enabled") && !modlogSettings.get(guild.getId()).get("channel").toString().isEmpty()) {
			TextChannel channel = guild.getTextChannelById(modlogSettings.get(guild.getId()).get("channel").toString());
			if (channel != null)
				channel.sendMessageFormat("`[%s]` :%s: **%s Log**\n```java\n%s```",
						Main.getFormattedTime(), emote, name, String.format(content, formatArgs)).queue();
		}
	}

	@SubscribeEvent
	public static void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (settings.containsKey(event.getGuild().getId()) && ((Map) settings.get(event.getGuild().getId())).containsKey("mutes") && ((List) ((Map) settings.get(event.getGuild().getId())).get("mutes")).contains(event.getUser().getId()))
			Main.mute(event.getGuild().getMember(event.getUser()));
		if (settings.containsKey(event.getGuild().getId()) && !((Map) settings.get(event.getGuild().getId())).get("autorole").toString().isEmpty() && event.getGuild().getRoleById(((Map) settings.get(event.getGuild().getId())).get("autorole").toString()) != null)
			try {
				event.getGuild().getController().addSingleRoleToMember(event.getMember(), event.getGuild().getRoleById(((Map) settings.get(event.getGuild().getId())).get("autorole").toString())).queue();
			} catch (Exception e) {}
		if (settings.containsKey(event.getGuild().getId()) && !((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString().isEmpty() && event.getGuild().getTextChannelById(((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString()) != null)
			event.getGuild().getTextChannelById(((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString()).sendMessage(((Map) settings.get(event.getGuild().getId())).get("greeting").toString()
					.replaceAll("USER_MENTION", event.getUser().getAsMention())
					.replaceAll("USER", Main.str(event.getUser()))
					.replaceAll("SERVER", Main.str(event.getGuild()))).queue();
		log(event.getGuild(), "inbox_tray", "Member Join", "Member joined: %s.", Main.str(event.getUser()));
	}

	@SubscribeEvent
	public static void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		if (settings.containsKey(event.getGuild().getId()) && !((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString().isEmpty() && event.getGuild().getTextChannelById(((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString()) != null)
			event.getGuild().getTextChannelById(((Map) settings.get(event.getGuild().getId())).get("welcomeChannel").toString()).sendMessage(((Map) settings.get(event.getGuild().getId())).get("farewell").toString()
					.replaceAll("USER_MENTION", event.getUser().getAsMention())
					.replaceAll("USER", Main.str(event.getUser()))
					.replaceAll("SERVER", Main.str(event.getGuild()))).queue();
		log(event.getGuild(), "outbox_tray", "Member Leave", "Member left: %s.", Main.str(event.getUser()));
	}

	@SubscribeEvent
	public static void onGuildBan(GuildBanEvent event) {
		logModAction(event.getUser(), null, event.getGuild(), "Ban", "hammer");
		log(event.getGuild(), "hammer", "Member Ban", "Member banned: %s.", Main.str(event.getUser()));
	}

	@SubscribeEvent
	public static void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		log(event.getGuild(), "bangbang", "Voice State Update", "Member: %s\nBefore: null\nAfter: %s", Main.str(event.getMember()), event.getChannelJoined().getName());
	}

	@SubscribeEvent
	public static void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		log(event.getGuild(), "bangbang", "Voice State Update", "Member: %s\nBefore: %s\nAfter: %s", Main.str(event.getMember()), event.getChannelLeft().getName(), event.getChannelJoined().getName());
	}

	@SubscribeEvent
	public static void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		log(event.getGuild(), "bangbang", "Voice State Update", "Member: %s\nBefore: %s\nAfter: null", Main.str(event.getMember()), event.getChannelLeft().getName());
	}

	@SubscribeEvent
	public static void onMessageUpdate(MessageUpdateEvent event) {
		log(event.getGuild(), "pencil2", "Message Edit", "Member: %s\nChannel: %s\nBefore: %s\nAfter: %s", Main.str(event.getAuthor()), event.getChannel().getName(), loggedMessages.containsKey(event.getMessageId()) ? loggedMessages.get(event.getMessageId()).get("content") : "Unknown", event.getMessage().getContent());
		loggedMessages.put(event.getMessage().getId(), Main.newHashMap(new String[] {"content", "sent", "author"}, new Object[] {event.getMessage().getRawContent(), System.currentTimeMillis(), event.getAuthor().getId()})); // ik it's updating the sent variable, it is supposed to.
	}

	@SubscribeEvent
	public static void onMessageDelete(MessageDeleteEvent event) {
		if (loggedMessages.containsKey(event.getMessageId()) && !Main.getSelfUser().getId().equals(loggedMessages.get(event.getMessageId()).get("author")))
			log(event.getGuild(), "wastebasket", "Message Delete", "Member: %s\nChannel: %s\nMessage: %s", !loggedMessages.containsKey(event.getMessageId()) ? "Unknown" : Main.getUserById(loggedMessages.get(event.getMessageId()).get("author").toString()), event.getTextChannel().getName(), !loggedMessages.containsKey(event.getMessageId()) ? "Unknown" : loggedMessages.get(event.getMessageId()).get("content").toString());
	}

	@SubscribeEvent
	public static void onTextChannelCreate(TextChannelCreateEvent event) {
		channels.put(event.getChannel().getId(), Main.cloneChannel(event.getChannel()));
		log(event.getGuild(), "pick", "TextChannel Create", "Channel: " + event.getChannel().getName());
	}

	@SubscribeEvent
	public static void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
		channels.put(event.getChannel().getId(), Main.cloneChannel(event.getChannel()));
		log(event.getGuild(), "pick", "VoiceChannel Create", "Channel: " + event.getChannel().getName());
	}

	@SubscribeEvent
	public static void onTextChannelDelete(TextChannelDeleteEvent event) {
		channels.remove(event.getChannel().getId());
		log(event.getGuild(), "pick", "TextChannel Delete", "Channel: " + event.getChannel().getName());
	}

	@SubscribeEvent
	public static void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
		channels.remove(event.getChannel().getId());
		log(event.getGuild(), "pick", "VoiceChannel Delete", "Channel: " + event.getChannel().getName());
	}

	@SubscribeEvent
	public static void onGenericTextChannelUpdate(GenericTextChannelUpdateEvent event) {
		if (!event.getChannel().getName().equals(channels.get(event.getChannel().getId()).getName())) log(event.getGuild(), "pick", "TextChannel Update", "Before: %s\nAfter: %s", channels.get(event.getChannel().getId()).getName(), event.getChannel().getName());
		channels.put(event.getChannel().getId(), Main.cloneChannel(event.getChannel()));
	}

	@SubscribeEvent
	public static void onGenericVoiceChannelUpdate(GenericVoiceChannelUpdateEvent event) {
		if (!event.getChannel().getName().equals(channels.get(event.getChannel().getId()).getName())) log(event.getGuild(), "pick", "VoiceChannel Update", "Before: %s\nAfter: %s", channels.get(event.getChannel().getId()).getName(), event.getChannel().getName());
		channels.put(event.getChannel().getId(), Main.cloneChannel(event.getChannel()));
	}

	@SubscribeEvent
	public static void onRoleCreate(RoleCreateEvent event) {
		roles.put(event.getRole().getId(), Main.cloneRole(event.getRole()));
		log(event.getGuild(), "game_die", "Role Create", "None yet.");
	}

	@SubscribeEvent
	public static void onRoleDelete(RoleDeleteEvent event) {
		roles.remove(event.getRole().getId());
		log(event.getGuild(), "game_die", "Role Delete", "Role: " + event.getRole().getName());
	}

	@SubscribeEvent
	public static void onGenericRoleUpdate(GenericRoleUpdateEvent event) {
		Role before = roles.get(event.getRole().getId());
		Role after = event.getRole();
		List<String> permsBefore = new ArrayList();
		List<String> permsAfter = new ArrayList();
		for (Permission perm : before.getPermissions())
			permsBefore.add(perm.getName());
		for (Permission perm : after.getPermissions())
			permsAfter.add(perm.getName());
		log(event.getGuild(), "game_die", "Role Update", "Before:"
				+ "\n\tName:            %s"
				+ "\n\tColour:          %s"
				+ "\n\tMentionable:     %s"
				+ "\n\tHoisted:         %s"
				+ "\n\tPermissions:\n\t\t%s"
				+ "\n\nAfter:"
				+ "\n\tName:            %s"
				+ "\n\tColour:          %s"
				+ "\n\tMentionable:     %s"
				+ "\n\tHoisted:         %s"
				+ "\n\tPermissions:\n\t\t%s",
				before.getName(),
				Main.colorToHex(before.getColor()),
				before.isMentionable(),
				before.isHoisted(),
				Main.joinCustomChar("\n\t\t", permsBefore),
				after.getName(),
				Main.colorToHex(after.getColor()),
				after.isMentionable(),
				after.isHoisted(),
				Main.joinCustomChar("\n\t\t", permsAfter));
		roles.put(event.getRole().getId(), Main.cloneRole(event.getRole()));
	}

	private static void mute(Member member, int minutes) {
		Map<Channel, Long> perms = new HashMap();
		for (Channel channel : Main.getAllChannels(member.getGuild())) {
			if (channel.getPermissionOverride(member) != null) perms.put(channel, channel.getPermissionOverride(member).getAllowedRaw());
			else perms.put(channel, null);
			Main.getPermissionOverride(member, channel).getManagerUpdatable().deny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_SPEAK).update().queue();
		}
		Main.runAsynchronously(() -> {
			try {
				Thread.sleep(60000*minutes);
			} catch (InterruptedException e) {}
			for (Channel channel : perms.keySet())
				if (channel.getGuild().getTextChannelById(channel.getId()) != null || channel.getGuild().getVoiceChannelById(channel.getId()) != null)
					if (perms.get(channel) != null) channel.getPermissionOverride(member).getManagerUpdatable().grant(perms.get(channel)).update().queue();
					else channel.getPermissionOverride(member).delete().queue();
		});
	}

	@SubscribeEvent
	public static void onMessageReceived(MessageReceivedEvent event) {
		loggedMessages.put(event.getMessage().getId(), Main.newHashMap(new String[] {"content", "sent", "author"}, new Object[] {event.getMessage().getRawContent(), System.currentTimeMillis(), event.getAuthor().getId()}));
		if (event.getGuild() == null) return;
		if (filters.containsKey(event.getGuild().getId()) && !event.getAuthor().getId().equals(Main.getSelfUser().getId()) && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			List<String> filters = new ArrayList<>((List<String>) ((Map) Moderation.filters.get(event.getGuild().getId())).get("filtered"));
			Main.toLowerCase(filters);
			if (Main.contains(event.getMessage().getRawContent().toLowerCase(), filters)) {
				try {
					event.getMessage().delete().complete();
				} catch (Throwable e) {
					return;
				}
				Message msg = event.getChannel().sendMessageFormat("%s you're not allowed to say that!", event.getAuthor().getAsMention()).complete();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
				msg.delete().queue();
				return;
			}
		}
		if (event.getMessage().getContent().toLowerCase().contains("discord.gg/") && event.getMember() != null && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			try {
				event.getMessage().delete().complete();
			} catch (Throwable e) {
				return;
			}
			event.getChannel().sendMessage(event.getAuthor().getAsMention() + " don't advertise other Discord servers!").queue();
		}
		if (settings.containsKey(event.getGuild().getId()))
			if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam") && event.getMessage().getMentionedUsers().size() > 10) event.getGuild().getController().ban(event.getAuthor(), 1).queue();
		messages.put(event.getAuthor().getId(), Main.add(messages.getOrDefault(event.getAuthor().getId(), new ArrayList<Message>()), event.getMessage()));
	}

	@SubscribeEvent
	public static void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		if (!pastNicks.containsKey(event.getUser().getId())) pastNicks.put(event.getUser().getId(), Lists.newArrayList(event.getPrevNick()));
		else ((List) pastNicks.get(event.getUser().getId())).add(event.getPrevNick());
		log(event.getGuild(), "warning", "Member Nickname Change", "Member: %s\nBefore: %s\nAfter: %s", Main.str(event.getMember()), event.getPrevNick(), event.getNewNick());
	}

	@SubscribeEvent
	public static void onUserNameUpdate(UserNameUpdateEvent event) {
		if (!pastNames.containsKey(event.getUser().getId())) pastNicks.put(event.getUser().getId(), Lists.newArrayList(event.getOldName()));
		else ((List) pastNames.get(event.getUser().getId())).add(event.getOldName());
	}

	public static Map getBlacklist() {
		return Collections.unmodifiableMap(blacklist);
	}

	public static Map getSettings(Guild guild) {
		Map settings = (Map) Moderation.settings.get(guild.getId()) == null ? Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "greeting", "farewell", "welcomeChannel", "dm", "cases", "mutes"},
				new Object[] {"", "", true, false, Config.get("prefix"), "Welcome to **SERVER**, **USER_MENTION**!", "**USER_MENTION** has left **SERVER**, bye bye **USER_MENTION**.", "", false, new HashMap(), new ArrayList()}) : (Map) Moderation.settings.get(guild.getId());
		try {
			settings.put("welcomeChannel", settings.get("welcomeChannel").toString().isEmpty() ? "" : guild.getTextChannelById(settings.get("welcomeChannel").toString()) == null ? "" : guild.getTextChannelById(settings.get("welcomeChannel").toString()).getName());
			settings.put("channel", settings.get("channel").toString().isEmpty() ? "" : guild.getTextChannelById(settings.get("channel").toString()) == null ? "" : guild.getTextChannelById(settings.get("channel").toString()).getName());
		} catch (Exception ignored) {} // there used to be a bug in the dashboard that didn't convert channel names to their IDs.
		return settings;
	}

	public static Map getGivemeSettings(Guild guild) {
		return (Map) givemeSettings.get(guild.getId()) == null ? new HashMap() : (Map) givemeSettings.get(guild.getId());
	}

	public static Map<String, Object> getModlogSettings(Guild guild) {
		return modlogSettings.get(guild.getId()) == null ? Main.newHashMap(new String[] {"enabled", "channel"}, new Object[] {true, ""}) : modlogSettings.get(guild.getId());
	}

	public static void putSettings(Guild guild, Map settings) throws IOException {
		if (Main.getCallerClass() == Dashboard.DefaultHttpHandler.class) {
			Map modlogSettings = new HashMap();
			modlogSettings.put("enabled", settings.get("enableLogging"));
			modlogSettings.put("channel", guild.getTextChannelsByName(settings.get("logChannel").toString(), true).isEmpty() ? "" : guild.getTextChannelsByName(settings.get("logChannel").toString(), true).get(0).getId());
			Moderation.modlogSettings.put(guild.getId(), modlogSettings);
			DataIO.saveJson(Moderation.modlogSettings, "data/mod/modlog.json");
			settings = Main.removeKeys(settings, new String[] {"enableLogging", "logChannel"});
			String[] givemes = settings.get("givemes").toString().split(";");
			Map<String, String> givemeIds = new HashMap();
			for (String giveme : givemes)
				if (!guild.getRolesByName(giveme.trim(), true).isEmpty()) givemeIds.put(giveme.trim(), guild.getRolesByName(giveme.trim(), true).get(0).getId());
			givemeSettings.put(guild.getId(), givemeIds);
			DataIO.saveJson(givemeSettings, "data/mod/givemeSettings.json");
			settings.remove("givemes");
			settings.put("welcomeChannel", settings.get("welcomeChannel").toString().isEmpty() ? "" : guild.getTextChannelsByName(settings.get("welcomeChannel").toString(), true).isEmpty() ? "" : guild.getTextChannelsByName(settings.get("welcomeChannel").toString(), true).get(0).getName());
			settings.put("channel", settings.get("channel").toString().isEmpty() ? "" : guild.getTextChannelsByName(settings.get("channel").toString(), true).isEmpty() ? "" : guild.getTextChannelsByName(settings.get("channel").toString(), true).get(0).getName());
			Moderation.settings.put(guild.getId(), settings);
			DataIO.saveJson(modlogSettings, "data/mod/settings.json");
		}
	}

	private static void saveFiles() {
		Main.print(LogType.DEBUG, "Shutting down, saving files.");
		try {
			DataIO.saveJson(loggedMessages, "data/mod/loggedMessages.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataIO.saveJson(pastNames, "data/mod/pastNames.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			DataIO.saveJson(pastNicks, "data/mod/pastNicks.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
