package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.SilentCommandEvent;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class Moderation {

	private static Map settings;
	private static Map filters;
	private static Map givemeSettings;
	private static Map warnerSettings;
	private static Map pastNicks;
	private static Map pastNames;
	private static Map<String, List<Message>> messages = new HashMap();

	static {
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
		// this has to be final, smh.
		final Map blacklist = DataIO.loadJsonOrDefaultQuietly("data/mod/blacklist.json", Map.class, new HashMap());
		Main.addCommandHook((event) -> {
			if (event.getGuild() != null && blacklist.containsKey(event.getGuild().getId()) && ((List) blacklist.get(event.getGuild().getId())).contains(event.getAuthor().getId())) throw new SecurityException("You're blacklisted in this server.");
			else if (blacklist.containsKey("global") && ((List) blacklist.get("global")).contains(event.getAuthor().getId())) throw new SecurityException("You're globally blacklisted.");
		});
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
	}

	@Command(category = "Moderation", help = "Ban someone.", name = "ban", arguments = "<user>", botPermissions = {Permission.BAN_MEMBERS}, userPermissions = {Permission.BAN_MEMBERS})
	public static void ban(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (!PermissionUtil.canInteract(event.getGuild().getSelfMember(), member)) event.reply("I cannot ban that user as they're higher in the hierarchy than I am.");
			else {
				event.getGuild().getController().ban(member.getUser(), 1).queue();
				event.reply("Successfully banned " + member.getEffectiveName() + ".");
				logModAction(member.getUser(), event.getAuthor(), member.getGuild(), "Ban", "hammer");
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

	private static void logModAction(User user, User moderator, Guild guild, String actionType, String emoji) {
		if (settings.containsKey(guild.getId())) {
			if (actionType.toLowerCase().equals("mute"))
				((List) ((Map) settings.get(guild.getId())).get("mutes")).add(user.getId());
			TextChannel channel = guild.getTextChannelById((String) ((Map) settings.get(guild.getId())).get("channel"));
			int caseNum = Main.getIntFromPossibleDouble(((Map) ((Map) settings.get(guild.getId())).get("cases")).size()) + 1;
			if (channel != null) {
				String messageId = channel.sendMessageFormat("**Case #%s** | %s :%s:\n"
						+ "**User:** %s#%s (%s)\n"
						+ "**Moderator:** %s\n"
						+ "**Reason:** Unknown, type %sreason %s <reason> to add it.",
						caseNum, actionType, emoji,
						user.getName(), user.getDiscriminator(), user.getId(),
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
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), new ArrayList<>());
				((List) settings.get(event.getGuild().getId())).add(member.getUser().getId());
				try {
					DataIO.saveJson(settings, "data/mod/blacklist.json");
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
			else if (!settings.containsKey(event.getGuild().getId())) event.reply("No one in this server is currently blacklisted.");
			else if (!((List) settings.get(event.getGuild().getId())).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
			else {
				((List) settings.get(event.getGuild().getId())).remove(member.getUser().getId());
				try {
					DataIO.saveJson(settings, "data/mod/blacklist.json");
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
				if (!settings.containsKey("global")) settings.put("global", new ArrayList<>());
				((List) settings.get("global")).add(member.getUser().getId());
				try {
					DataIO.saveJson(settings, "data/mod/blacklist.json");
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
			else if (!settings.containsKey("global")) event.reply("No one is currently globally blacklisted.");
			else if (!((List) settings.get("global")).contains(member.getUser().getId())) event.reply("That user isn't blacklisted.");
			else {
				((List) settings.get("global")).remove(member.getUser().getId());
				try {
					DataIO.saveJson(settings, "data/mod/blacklist.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("Successfully removed " + member.getAsMention() + " from the global blacklist.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Manage the filters.", name = "filter")
	public static void filter(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add a filter to the list of filters.", name = "add", parent = "com.ptsmods.impulse.commands.Moderation.filter", arguments = "<filter>", userPermissions = {Permission.MESSAGE_MANAGE})
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

	@Subcommand(help = "Remove a filter from the list of filters.", name = "remove", parent = "com.ptsmods.impulse.commands.Moderation.filter", arguments = "<filter>", userPermissions = {Permission.MESSAGE_MANAGE})
	public static void filterRemove(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!filters.containsKey(event.getGuild().getId())) event.reply("Nothing is being filtered in this server.");
			else if (!((List) filters.get(event.getGuild().getId())).contains(event.getArgs())) event.reply("The given message is currently not being filtered.");
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

	@Subcommand(help = "Lists all filters in this server.", name = "list", parent = "com.ptsmods.impulse.commands.Moderation.filter")
	public static void filterList(CommandEvent event) {
		if (filters.containsKey(event.getGuild().getId()) && !((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).isEmpty())
			event.reply("Currently the following messages are being filtered in this server: `" + Main.joinCustomChar("`, `", (List) ((Map) filters.get(event.getGuild().getId())).get("filtered")) + "`.");
		else event.reply("Nothing is being filtered in this server.");
	}

	@Command(category = "Moderation", help = "Give yourself roles.", name = "giveme", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<giveme>")
	public static void giveme(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no givemeSettingss yet.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(event.getArgs())) event.reply("That givemeSettings could not be found.");
			else {
				Role giveme = event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get(event.getArgs()));
				if (giveme == null) event.reply("The givemeSettings was found, but the connected role was deleted.");
				else if (!PermissionUtil.canInteract(event.getSelfMember(), giveme)) event.reply("I cannot give you that role, as it is higher in the hierarchy than my highest role.");
				else {
					event.getGuild().getController().addSingleRoleToMember(event.getMember(), giveme).queue();
					event.reply("The role has been added, you're welcome.");
				}
			}
		} else
			Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Add givemes, divide the roles with a semi-colon (;).", name = "add", parent = "com.ptsmods.impulse.commands.Moderation.giveme", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES}, arguments = "<roles>")
	public static void givemeAdd(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			List<Role> roles = new ArrayList<>();
			for (String roleName : event.getArgs().split(event.getArgs().contains("; ") ? "; " : ";"))
				if (event.getGuild().getRolesByName(roleName, true).size() == 0) {event.reply("The role '%s' could not be found.", roleName); return;}
				else roles.add(event.getGuild().getRolesByName(roleName, true).get(0));
			if (!givemeSettings.containsKey(event.getGuild().getId())) givemeSettings.put(event.getGuild().getId(), new HashMap());
			for (Role role : roles)
				((Map) givemeSettings.get(event.getGuild().getId())).put(role.getName(), role.getId());
			try {
				DataIO.saveJson(givemeSettings, "data/giveme/givemeSettings.json");
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
		if (event.getArgs().length() != 0) {
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
					DataIO.saveJson(givemeSettings, "data/giveme/givemeSettings.json");
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
			else if (!((Map) givemeSettings.get(event.getGuild().getId())).containsKey(event.getArgs())) event.reply("That giveme could not be found.");
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
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Makes the bot leave.", name = "leave", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void leave(CommandEvent event) {
		event.reply("Are you sure you want me to go? (yes/no)");
		Message response = Main.waitForInput(event.getGuild().getMember(event.getAuthor()), event.getChannel(), 15000, event.getMessage().getCreationTime().toEpochSecond());
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
		if (settings.containsKey(event.getGuild().getId())) {
			serverPrefix = (String) ((Map) settings.get(event.getGuild().getId())).get("serverPrefix");
			serverPrefix = serverPrefix == null || serverPrefix.isEmpty() ? "not set" : serverPrefix;
			String channelId = (String) ((Map) settings.get(event.getGuild().getId())).get("channel");
			channel = channelId.isEmpty() ? channel : event.getGuild().getTextChannelById(channelId) == null ? channel : event.getGuild().getTextChannelById(channelId).getAsMention();
			banMentionSpam = (boolean) ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam") ? "enabled" : banMentionSpam;
			autorole = event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get("autorole")) == null ? "not set" : event.getGuild().getRoleById((String) ((Map) settings.get(event.getGuild().getId())).get("autorole")).getName();
			autoroleEnabled = (String) ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled");
		}
		Main.sendCommandHelp(event, String.format("**Server prefix**: %s\n**Channel**: %s\n**Ban mention spam**: %s\n**Autorole**: %s\n**Autorole enabled**: %s", serverPrefix, channel, banMentionSpam, autorole, autoroleEnabled));
	}

	@Subcommand(help = "Sets this server's prefix.", name = "serverprefix", parent = "com.ptsmods.impulse.commands.Moderation.modset", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void modsetServerPrefix(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "cases", "mutes"}, new Object[] {"", "", false, false, event.getArgs(), new HashMap(), new ArrayList()}));
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
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "cases", "mutes"}, new Object[] {channel.getId(), "", false, false, "", new HashMap(), new ArrayList()}));
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
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "cases", "mutes"}, new Object[] {"", "", false, true, "", new HashMap(), new ArrayList()}));
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
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "cases", "mutes"}, new Object[] {"", role.getId(), true, false, "", new HashMap(), new ArrayList()}));
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
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"channel", "autorole", "autoroleEnabled", "banMentionSpam", "serverPrefix", "cases", "mutes"}, new Object[] {"", "", true, false, "", new HashMap(), new ArrayList()}));
			else ((Map) settings.get(event.getGuild().getId())).put("autoroleEnabled", !(boolean) ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled"));
			try {
				DataIO.saveJson(settings, "data/mod/settings.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while loading the data file.", e);
			}
			event.reply("Successfully toggled autoroleEnabled to " + ((Map) settings.get(event.getGuild().getId())).get("autoroleEnabled") + ".");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Shows you the past names of a user.", name = "pastnames", arguments = "<user>")
	public static void pastNames(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("That user could not be found.");
			else if (!pastNames.containsKey(user.getId())) event.reply("No data could be found for that user.");
			else event.reply("Past names:\n" + Main.joinCustomChar(", ", ((List<String>) pastNames.get(user.getId())).toArray(new String[0])));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Shows you the past names of a user.", name = "pastnicks", arguments = "<user>")
	public static void pastNicks(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("That user could not be found.");
			else if (!pastNicks.containsKey(user.getId())) event.reply("No data could be found for that user.");
			else event.reply("Past nicks:\n" + Main.joinCustomChar(", ", ((List<String>) pastNicks.get(user.getId())).toArray(new String[0])));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Moderation", help = "Renames a user.", name = "rename", arguments = "<user> <newName>")
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

	@Command(category = "Moderation", help = "Reset the warnings of a user.", name = "resetwarns", arguments = "<user>", userPermissions = {Permission.KICK_MEMBERS})
	public static void resetWarns(CommandEvent event) {
		if (event.getArgs().length() != 0) {
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

	@Command(category = "Moderation", help = "Warn a user for their behavior.", name = "warn", userPermissions = {Permission.KICK_MEMBERS}, arguments = "<user>")
	public static void warn(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!warnerSettings.containsKey(guildId)) warnerSettings.put(guildId, new HashMap<>());
			if (!event.getGuild().getSelfMember().canInteract(member))
				event.reply("I cannot mute, kick or ban that user as they're higher in the hierarchy than I am.");
			else if (!((Map) warnerSettings.get(guildId)).containsKey(userId)) {
				((Map) warnerSettings.get(guildId)).put(userId, 1);
				event.reply(member.getAsMention() + " has 1 warning, but nothing happens yet. Next up: 5 minute mute.");
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
				case 2: {output = "%s has **2 warnings**, and has been muted for 5 minutes. Next up: **30 minute mute**."; mute(member, 5); break;}
				case 3: {output = "%s has **3 warnings**, and has been muted for 30 minutes. Next up: **kick**."; mute(member, 30); break;}
				case 4: {output = "%s has **4 warnings**, and has been kicked. Next up: **ban**."; event.getGuild().getController().kick(member).complete(); break;}
				case 5: {output = "%s has **5 warnings**, and has been banned."; resetWarns(new SilentCommandEvent(event)); event.getGuild().getController().ban(member, 1).complete(); break;}
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

	@Command(category = "Moderation", help = "Tells you how many warnings a user has.", name = "warns", arguments = "<user>")
	public static void warns(CommandEvent event) throws CommandException {
		if (event.getArgs().length() != 0) {
			try {
				warnerSettings = DataIO.loadJson("data/warner/warnerSettings.json", Map.class);
			} catch (IOException e) {
				throw new CommandException("There was an error while loading the file.", e);
			}
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
				int warns = ((Double) ((Map) warnerSettings.get(guildId)).get(userId)).intValue();
				event.reply(event.getGuild().getMemberById(userId).getEffectiveName() + " has " + warns + " warning" + (warns != 1 ? "s" : "") + ".");
			}
		} else Main.sendCommandHelp(event);
	}

	@SubscribeEvent
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		if (settings.containsKey(event.getGuild().getId()) && ((List) ((Map) settings.get(event.getGuild().getId())).get("mutes")).contains(event.getUser().getId()))
			Main.mute(event.getGuild().getMember(event.getUser()));
	}

	@SubscribeEvent
	public static void onGuildBan(GuildBanEvent event) {
		if (settings.containsKey(event.getGuild().getId())) {
			Map<String, Map<String, String>> cases = (Map) ((Map) settings.get(event.getGuild().getId())).get("cases");
			if (cases.size() == 0 || !cases.get("" + (cases.size()-1)).get("user").equals(event.getUser().getId()))
				logModAction(event.getUser(), null, event.getGuild(), "Ban", "hammer");
		}
	}

	private static void mute(Member member, int minutes) {
		Role role = Main.getRoleByName(member.getGuild(), "Impulse Muted", true);
		if (role == null)
			role = member.getGuild().getController().createRole().setName("Impulse Muted").setPermissions().complete();
		List<Role> roles = member.getRoles();
		List<Role> rolesToAdd = new ArrayList<>();
		rolesToAdd.add(role);
		List<Role> rolesToRemove = new ArrayList<>();
		for (Role r : roles) {
			if (r.isManaged() || !PermissionUtil.canInteract(member.getGuild().getSelfMember(), r) || r.equals(role))
				continue;
			rolesToRemove.add(r);
		}
		member.getGuild().getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue();
		Main.runAsynchronously(() -> {
			try {
				Thread.sleep(60000*minutes);
			} catch (InterruptedException e) {}
			member.getGuild().getController().modifyMemberRoles(member, rolesToRemove, rolesToAdd).queue();
		});
	}

	@SubscribeEvent
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getGuild() == null) return;
		if (filters.containsKey(event.getGuild().getId()) && !event.getAuthor().getId().equals(Main.getSelfUser().getId()) && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE))
			for (String part : event.getMessage().getContent().split(" "))
				if (((List) ((Map) filters.get(event.getGuild().getId())).get("filtered")).contains(part)) {
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
		if (settings.containsKey(event.getGuild().getId()))
			if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("banMentionSpam") && event.getMessage().getMentionedUsers().size() > 10) event.getGuild().getController().ban(event.getAuthor(), 1).queue();
		messages.put(event.getAuthor().getId(), Main.add(messages.getOrDefault(event.getAuthor().getId(), new ArrayList<Message>()), event.getMessage()));
	}

	@SubscribeEvent
	public static void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		if (!pastNicks.containsKey(event.getUser().getId())) pastNicks.put(event.getUser().getId(), Lists.newArrayList(event.getPrevNick()));
		else ((List) pastNicks.get(event.getUser().getId())).add(event.getPrevNick());
		try {
			DataIO.saveJson(pastNicks, "data/mod/pastNicks.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void onUserNameUpdate(UserNameUpdateEvent event) {
		if (!pastNames.containsKey(event.getUser().getId())) pastNicks.put(event.getUser().getId(), Lists.newArrayList(event.getOldName()));
		else ((List) pastNames.get(event.getUser().getId())).add(event.getOldName());
		try {
			DataIO.saveJson(pastNames, "data/mod/pastNames.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
