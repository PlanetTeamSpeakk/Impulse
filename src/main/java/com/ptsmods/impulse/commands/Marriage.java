package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;

public class Marriage {

	private static Map settings;
	public static final String heart = "❤";

	static {
		try {
			settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the data file.", e);
		}
	}

	@Command(category = "Marriage", help = "Counts all the couples in this server.", name = "couplecount", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES})
	public static void coupleCount(CommandEvent event) {
		List<Role> marriageRoles = new ArrayList<>();
		for (Role role : event.getGuild().getRoles())
			if (role.getName().contains(heart) && role.getColor() != null && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRoles.add(role);
		event.reply("There are currently " + marriageRoles.size() + " married couples in this server.");
	}

	@Command(category = "Marriage", help = "Divorce your ex.", name = "divorce", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<ex>")
	public static void divorce(CommandEvent event) {
		if (!event.getArgs().isEmpty() && Main.isLong(event.getArgs().split(" ")[0])) {
			MessageChannel marriageChannel = null;
			try {
				if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) try {marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());} catch (Exception e) {}
				if (marriageChannel == null) marriageChannel = Main.getOrDefault(event.getGuild().getTextChannelsByName("marriage", true), 0, null);
			} catch (Exception e) {}
			Role marriageRole = event.getGuild().getRoleById(event.getArgs().split(" ")[0]);
			if (marriageRole == null) event.reply("That divorce id could not be found.");
			else {
				try {
					marriageRole.delete().queue();
				} catch (HierarchyException e) {
					event.reply("I cannot remove the marriage role as it is higher than my highest role.");
					return;
				}
				event.reply("You're now divorced.");
				if (marriageChannel != null) if (marriageChannel != null) marriageChannel.sendMessageFormat("%s divorced id `%s`.", event.getAuthor().getName(), marriageRole.getId()).queue();
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Forcibly divorce a couple.\\nExample: [p]forcedivorce My homie #1; My homie #36", name = "forcedivorce", userPermissions = {Permission.MANAGE_ROLES}, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<user1>; <user2>", guildOnly = true)
	public static void forceDivorce(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getArgs().split(";").length > 1 || event.getArgs().split("; ").length > 1) {
			String[] usernames = event.getArgs().contains("; ") ? event.getArgs().split("; ") : event.getArgs().split(";");
			List<Member> members = new ArrayList<>();
			MessageChannel marriageChannel = null;
			if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) try {marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());} catch (Exception e) {}
			if (marriageChannel == null) marriageChannel = Main.getOrDefault(event.getGuild().getTextChannelsByName("marriage", true), 0, null);
			for (String username : usernames)
				if (!event.getGuild().getMembersByName(username, true).isEmpty()) members.add(event.getGuild().getMembersByName(username, true).get(0));
				else {
					event.reply("The user '" + username + "' could not be found.");
					return;
				}
			for (Role role : event.getGuild().getRoles())
				if (role.getName().contains(members.get(0).getUser().getName()) && role.getName().contains(heart) && role.getName().contains(members.get(1).getUser().getName())) {
					try {
						role.delete().queue();
					} catch (HierarchyException e) {
						event.reply("I cannot remove the marriage role as it is higher than my highest role.");
						return;
					}
					event.reply("Successfully deleted the marriage role between %s and %s.", members.get(0).getAsMention(), members.get(1).getAsMention());
					if (marriageChannel != null) marriageChannel.sendMessageFormat("%s was forced to divorce %s.", members.get(0).getAsMention(), members.get(1).getAsMention()).queue();
					return;
				}
			event.reply("A marriage role between those 2 members could not be found.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Forcible marry 2 members, you pervert. \nExample: [p]forcemarry @Homie #1 @Homie #36", name = "forcemarry", userPermissions = {
			Permission.MANAGE_ROLES,
			Permission.MANAGE_CHANNEL,
			Permission.CREATE_INSTANT_INVITE,
			Permission.NICKNAME_CHANGE,
			Permission.VOICE_CONNECT,
			Permission.MESSAGE_READ,
			Permission.MESSAGE_WRITE,
			Permission.MESSAGE_ATTACH_FILES,
			Permission.MESSAGE_HISTORY,
			Permission.MESSAGE_EXT_EMOJI,
			Permission.VOICE_SPEAK,
			Permission.VOICE_USE_VAD
	}, botPermissions = {Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE}, arguments = "<user1> <user2>", guildOnly = true)
	public static void forceMarry(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getMessage().getMentionedUsers().size() > 1) {
			Member mem1 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
			Member mem2 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(1));
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
			MessageChannel marriageChannel = null;
			boolean isMarriedToMember = false;
			if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) try {marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());} catch (Exception e) {}
			if (marriageChannel == null) marriageChannel = Main.getOrDefault(event.getGuild().getTextChannelsByName("marriage", true), 0, null);
			if (mem1.equals(mem2)) event.reply("People can't marry themselves, that would be weird wouldn't it?");
			else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled")) event.reply("Marriages are currently disabled on this server.");
			else if (isMarriedToMember) event.reply("They're already married.");
			else if (mem2.equals(event.getSelfMember()) && !mem1.getUser().equals(Main.getOwner()) || mem1.equals(event.getSelfMember()) && !mem2.getUser().equals(Main.getOwner())) event.reply("I'd only marry my owner.");
			else {
				Role role = event.getGuild().getController().createRole().setName(String.format("%s " + heart + " %s", mem1.getUser().getName(), mem2.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions().complete();
				event.getGuild().getController().addSingleRoleToMember(mem1, role).queue();
				event.getGuild().getController().addSingleRoleToMember(mem2, role).queue();
				if (marriageChannel != null) marriageChannel.sendMessageFormat("%s was forced to marry %s.", mem2.getAsMention(), mem1.getAsMention()).queue();
				try {
					Main.sendPrivateMessage(mem1.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
							event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem2.getUser().getName(), mem2.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
				} catch (Throwable e) {
					event.reply("I wasn't able to send a direct message to " + mem1.getUser().getName() + ".");
				}
				try {
					Main.sendPrivateMessage(mem2.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
							event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem1.getUser().getName(), mem1.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
				} catch (Throwable e) {
					event.reply("I wasn't able to send a direct message to " + mem2.getUser().getName() + ".");
				}
				event.reply("Successfully forced %s to marry %s.", mem2.getUser().getName(), mem1.getUser().getName());
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Marry your crush.", name = "marry", arguments = "<crush>", botPermissions = {
			Permission.MANAGE_ROLES,
			Permission.MANAGE_CHANNEL,
			Permission.CREATE_INSTANT_INVITE,
			Permission.NICKNAME_CHANGE,
			Permission.VOICE_CONNECT,
			Permission.MESSAGE_READ,
			Permission.MESSAGE_WRITE,
			Permission.MESSAGE_ATTACH_FILES,
			Permission.MESSAGE_HISTORY,
			Permission.MESSAGE_EXT_EMOJI,
			Permission.VOICE_SPEAK,
			Permission.VOICE_USE_VAD
	}, cooldown = 60, guildOnly = true)
	public static void marry(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			try {
				settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
				settings = settings == null ? new HashMap() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occurred while loading the data file.", e);
			}
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
				int marryLimit = -1;
				marryLimit = Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) + 1;
				List<Role> marriageRolesOfAuthor = new ArrayList<>();
				List<Role> marriageRolesOfCrush = new ArrayList<>();
				for (Role role : event.getMember().getRoles())
					if (role.getName().contains(heart) && role.getColor() != null && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRolesOfAuthor.add(role);
				for (Role role : member.getRoles())
					if (role.getName().contains(heart) && role.getColor() != null && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRolesOfCrush.add(role);
				MessageChannel marriageChannel = null;
				boolean isMarriedToMember = false;
				for (Role role : marriageRolesOfAuthor)
					if (role.getName().contains(member.getUser().getName())) isMarriedToMember = true;
				for (Role role : marriageRolesOfCrush)
					if (role.getName().contains(event.getAuthor().getName())) isMarriedToMember = true;
				if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) try {marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());} catch (Exception e) {}
				if (marriageChannel == null) marriageChannel = Main.getOrDefault(event.getGuild().getTextChannelsByName("marriage", true), 0, null);
				if (event.getMember().equals(member)) event.reply("You can't marry yourself, that would be weird wouldn't it?");
				else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled")) event.reply("Marriages are currently disabled in this server.");
				else if (marryLimit > 0 && marriageRolesOfAuthor.size() >= marryLimit) event.reply("You have reached this server's marry limit. (" + marryLimit + ")");
				else if (marryLimit > 0 && marriageRolesOfCrush.size() >= marryLimit) event.reply("The user you're trying to marry has reached their marry limit. (" + marryLimit + ")");
				else if (isMarriedToMember) event.reply("You're already married to that person.");
				else if (member.equals(event.getSelfMember()) && !event.getMember().getUser().equals(Main.getOwner())) event.reply("I'd only marry my owner.");
				else {
					event.reply("%s, do you take %s as your husband/wife? (yes/no)", member.getAsMention(), event.getAuthor().getAsMention());
					Message response = Main.waitForInput(member, event.getChannel(), 60000);
					if (response == null) event.reply("%s, the user you tried to marry did not respond, I'm sorry.", event.getAuthor().getAsMention());
					else if (!response.getContent().startsWith("ye")) event.reply("%s, the user you tried to marry did not say yes, I'm sorry.", event.getAuthor().getAsMention());
					else {
						Role role = event.getGuild().getController().createRole().setName(String.format("%s " + heart + " %s", event.getAuthor().getName(), member.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions(Main.defaultPermissionsArray).complete();
						event.getGuild().getController().addSingleRoleToMember(event.getMember(), role).queue();
						event.getGuild().getController().addSingleRoleToMember(member, role).queue();
						if (marriageChannel != null) marriageChannel.sendMessageFormat("%s married %s, congratulations!", event.getAuthor().getAsMention(), member.getAsMention()).queue();
						try {
							Main.sendPrivateMessage(event.getAuthor(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
									member.getUser().getName(), member.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
						} catch (Throwable e) {
							event.reply("I wasn't able to send a direct message to " + event.getAuthor().getName() + ".");
						}
						try {
							Main.sendPrivateMessage(member.getUser(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
									event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
						} catch (Throwable e) {
							event.reply("I wasn't able to send a direct message to " + member.getUser().getName() + ".");
						}
					}
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Tells you this server's marry limit.", name = "marrylimit", guildOnly = true)
	public static void marryLimit(CommandEvent event) {
		if (!settings.containsKey(event.getGuild().getId()) || Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) < 1) event.reply("This server has no marrylimit.");
		else event.reply("This server's marry limit is currently set to " + Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) + ".");
	}

	@Command(category = "Marriage", help = "Divorces everyone in this server.", name = "massdivorce", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES}, botPermissions = {Permission.MANAGE_ROLES})
	public static void massDivorce(CommandEvent event) {
		List<Role> marriageRoles = new ArrayList<>();
		for (Role role : event.getGuild().getRoles())
			if (role.getName().contains(heart) && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRoles.add(role);
		int success = 0;
		int failed = 0;
		for (Role role : marriageRoles)
			try {
				role.delete().queue();
				success += 1;
			} catch (Exception e) {
				failed += 1;
			}
		event.reply("Successfully deleted %s marriage roles and failed to delete %s marriage roles in this server.", success, failed);
	}

	@Command(category = "Marriage", help = "Sets this server's marry limit.", name = "setmarrylimit", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void setMarryLimit(CommandEvent event) {
		if (Main.isInteger(event.getArgs().split(" ")[0])) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
			((Map) settings.get(event.getGuild().getId())).put("marryLimit", Integer.parseInt(event.getArgs().split(" ")[0]));
			try {
				DataIO.saveJson(settings, "data/marriage/settings.json");
			} catch (IOException e) {
				event.reply("An unknown error occurred while saving the data file.");
				e.printStackTrace();
				return;
			}
			event.reply("Successfully set the marrylimit of this server to " + event.getArgs().split(" ")[0] + ".");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Toggles whether members can marry each other in this server.", name = "togglemarriages", userPermissions = {Permission.ADMINISTRATOR})
	public static void toggleMarriages(CommandEvent event) {
		if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
		boolean disabled = (boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled");
		((Map) settings.get(event.getGuild().getId())).put("disabled", !disabled);
		try {
			DataIO.saveJson(settings, "data/marriage/settings.json");
		} catch (IOException e) {
			event.reply("An unknown error occurred while saving the data file.");
			e.printStackTrace();
			return;
		}
		event.reply("Marriages in this server are now " + (disabled ? "enabled" : "disabled") + ".");
	}

	public static int getMarryLimit(Guild guild) {
		if (settings.containsKey(guild.getId())) return Main.getIntFromPossibleDouble(((Map) settings.get(guild.getId())).get("marryLimit"));
		else return 0;
	}

}
