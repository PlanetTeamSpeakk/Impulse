package com.impulsebot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.NameGenerator;

import com.google.common.collect.Lists;
import com.impulsebot.utils.ArraySet;
import com.impulsebot.utils.DataIO;
import com.impulsebot.utils.Random;
import com.impulsebot.utils.commands.Command;
import com.impulsebot.utils.commands.CommandEvent;
import com.impulsebot.utils.commands.CommandException;
import com.impulsebot.utils.commands.Subcommand;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;

public class Marriage {

	private static Map												settings;
	private static Map<String, Map<String, Map<String, Object>>>	marriages;
	public static final String										heart	= "❤";

	static {
		try {
			settings = DataIO.loadJsonOrDefault("data/marriage/settings.json", Map.class, new HashMap());
			marriages = DataIO.loadJsonOrDefault("data/marriage/marriages.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the data file.", e);
		}
		Main.runAsynchronously(() -> {
			Main.sleep(10000); // GSON is probably still loading the marriages map in the background.
			while (true) {
				for (Entry<String, Map<String, Map<String, Object>>> guild : new HashMap<>(marriages).entrySet())
					for (Entry<String, Map<String, Object>> role : guild.getValue().entrySet())
						for (Map<String, Object> child : ((Map<String, Map<String, Object>>) role.getValue().get("children")).values()) {
							int satisfaction = Main.getIntFromPossibleDouble(child.get("satisfaction"));
							int health = Main.getIntFromPossibleDouble(child.get("health"));
							int fatigue = Main.getIntFromPossibleDouble(child.get("fatigue"));
							int defilement = Main.getIntFromPossibleDouble(child.get("defilement"));
							int euphoria = Main.getIntFromPossibleDouble(child.get("euphoria"));
							boolean sleeping = (boolean) child.get("sleeping");
							Random Random = com.impulsebot.utils.Random.INSTANCE; // just so it looks like the methods are static.
							int max = 15;
							if (!sleeping) {
								if (Random.randInt(max) == 0 && satisfaction > 0) satisfaction -= 1;
								if (Random.randInt(max) == 0 && (satisfaction < 40 || fatigue > 60 || defilement > 60) && health > 0) health -= 1;
								if ((Random.randInt(max) == 0 || Random.randInt(max / 2) == 0 && euphoria < 40) && fatigue < 100) fatigue += 1;
								if (Random.randInt(max) == 0 && defilement < 100) defilement += 1;
								if (Random.randInt(max) == 0 && euphoria > 0) euphoria -= 1;
							} else if (fatigue == 0)
								sleeping = false;
							else if (Random.randInt(max) == 0 && fatigue > 0) fatigue -= 1;
							if (health == 0) {
								for (String id : (List<String>) role.getValue().get("owners"))
									if (Main.getUserById(id) != null) Main.sendPrivateMessage(Main.getUserById(id), "Your child **%s** has deceased as you did not care for %s. I'm very sorry.", child.get("name"), child.get("gender").equals("m") ? "him" : "her");
								((Map<String, Object>) role.getValue().get("children")).remove(child.get("name"));
							}
							child.put("satisfaction", satisfaction);
							child.put("health", health);
							child.put("fatigue", fatigue);
							child.put("defilement", defilement);
							child.put("euphoria", euphoria);
							child.put("sleeping", sleeping);
						}
				try {
					DataIO.saveJson(marriages, "data/marriage/marriages.json");
				} catch (IOException e) {
					e.printStackTrace();
				}
				Main.sleep(1, TimeUnit.MINUTES);
			}
		});
	}

	@Command(category = "Marriage", help = "Counts all the couples in this server.", name = "couplecount", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES})
	public static void coupleCount(CommandEvent event) {
		if (marriages.containsKey(event.getGuild().getId())) {
			int roles = 0;
			for (String role : marriages.get(event.getGuild().getId()).keySet())
				if (event.getGuild().getRoleById(role) != null) roles += 1;
			event.reply("There are currently " + roles + " married couples in this server.");
		} else event.reply("No one in this server is married yet.");
	}

	@Command(category = "Marriage", help = "Divorce your ex.", name = "divorce", guildOnly = true, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<divorce id>")
	public static void divorce(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && Main.isLong(event.getArgs().split(" ")[0])) {
			if (!marriages.containsKey(event.getGuild().getId()))
				event.reply("There are no marriages in this server.");
			else if (!marriages.get(event.getGuild().getId()).containsKey(event.getArgs().split(" ")[0]))
				event.reply("A marriage role with that ID could not be found.");
			else if (!((List<String>) marriages.get(event.getGuild().getId()).get(event.getArgs().split(" ")[0]).get("owners")).contains(event.getAuthor().getId()))
				event.reply("You cannot divorce someone else.");
			else {
				Role marriageRole = event.getGuild().getRoleById(event.getArgs().split(" ")[0]);
				TextChannel marriageChannel = getMarriageChannel(event.getGuild());
				if (marriageRole != null && !event.getGuild().getSelfMember().getRoles().get(0).canInteract(event.getGuild().getRoleById(event.getArgs().split(" ")[0]))) {
					event.reply("I cannot delete their role as it's higher than my highest role.");
					return;
				}
				String other = "";
				for (String user : (List<String>) marriages.get(event.getGuild().getId()).get(event.getArgs().split(" ")[0]).get("owners"))
					if (!user.equals(event.getAuthor().getId())) other = user;
				marriages.get(event.getGuild().getId()).remove(event.getArgs().split(" ")[0]);
				try {
					DataIO.saveJson(marriages, "data/marriage/marriages.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				if (marriageRole != null) try {
					marriageRole.delete().queue();
				} catch (HierarchyException e) {
					event.reply("I cannot remove the marriage role as it is higher than my highest role.");
					return;
				}
				event.reply("Successfully divorced you and <@%s>.", other); // I am too lazy to check whether the other user is still in the guild, so I'll
																			// just do this instead.
				if (marriageChannel != null && marriageChannel.canTalk()) marriageChannel.sendMessageFormat("%s divorced <@%s>.", event.getAuthor().getAsMention(), other).queue();
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Forcibly divorce a couple.\\nExample: [p]forcedivorce My homie #1; My homie #36", name = "forcedivorce", userPermissions = {Permission.MANAGE_ROLES}, botPermissions = {Permission.MANAGE_ROLES}, arguments = "<user1>; <user2>", guildOnly = true)
	public static void forceDivorce(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && (event.getArgs().split(";").length > 1 || event.getArgs().split("; ").length > 1 || event.getMessage().getMentionedUsers().size() >= 2)) {
			if (!marriages.containsKey(event.getGuild().getId()))
				event.reply("There are currently no marriages in this server.");
			else {
				List<Member> members = new ArrayList<>();
				if (event.getMessage().getMentionedUsers().size() >= 2) {
					for (User user : event.getMessage().getMentionedUsers())
						if (event.getGuild().getMember(user) != null)
							members.add(event.getGuild().getMember(user));
						else {
							event.reply("The mentioned user '%s' is not in this server.", user.getAsMention());
							return;
						}
				} else {
					String[] usernames = event.getArgs().contains("; ") ? event.getArgs().split("; ") : event.getArgs().split(";");
					getMarriageChannel(event.getGuild());
					for (String username : usernames)
						if (!event.getGuild().getMembersByName(username, true).isEmpty())
							members.add(event.getGuild().getMembersByName(username, true).get(0));
						else {
							event.reply("The user '" + username + "' could not be found.");
							return;
						}
				}
				for (Entry<String, Map<String, Object>> role : marriages.get(event.getGuild().getId()).entrySet())
					if (((List<String>) role.getValue().get("owners")).containsAll(Lists.newArrayList(members.get(0).getUser().getId(), members.get(1).getUser().getId()))) {
						if (event.getGuild().getRoleById(role.getKey()) != null && !event.getGuild().getSelfMember().getRoles().get(0).canInteract(event.getGuild().getRoleById(role.getKey()))) {
							event.reply("I cannot delete their role as it's higher than my highest role.");
							return;
						}
						marriages.get(event.getGuild().getId()).remove(role.getKey());
						try {
							DataIO.saveJson(marriages, "data/marriage/marriages.json");
						} catch (IOException e) {
							throw new CommandException("An unknown error occurred while saving the data file.", e);
						}
						event.getGuild().getRoleById(role.getKey()).delete().queue();
						TextChannel marriageChannel = getMarriageChannel(event.getGuild());
						if (marriageChannel != null && marriageChannel.canTalk()) marriageChannel.sendMessageFormat("%s forcibly divorced %s and %s.", event.getAuthor().getAsMention(), members.get(0).getAsMention(), members.get(1).getAsMention()).queue();
						event.reply("The marriage role between **%s** and **%s** has been removed.", Main.str(members.get(0)), Main.str(members.get(1)));
						return;
					}
				event.reply("A marriage role between those 2 members could not be found.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Forcible marry 2 members, you pervert. \nExample: [p]forcemarry @Homie #1 @Homie #36", name = "forcemarry", userPermissions = {Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL, Permission.CREATE_INSTANT_INVITE, Permission.NICKNAME_CHANGE, Permission.VOICE_CONNECT, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD}, botPermissions = {Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE}, arguments = "<user1> <user2>", guildOnly = true)
	public static void forceMarry(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && event.getMessage().getMentionedUsers().size() > 1) {
			Member mem1 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
			Member mem2 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(1));
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
			TextChannel marriageChannel = getMarriageChannel(event.getGuild());
			boolean isMarriedToMember = false;
			if (mem1.equals(mem2))
				event.reply("People can't marry themselves, that would be weird wouldn't it?");
			else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled"))
				event.reply("Marriages are currently disabled on this server.");
			else if (isMarriedToMember)
				event.reply("They're already married.");
			else if (mem2.equals(event.getSelfMember()) && !mem1.getUser().equals(Main.getOwner()) || mem1.equals(event.getSelfMember()) && !mem2.getUser().equals(Main.getOwner()))
				event.reply("I'd only marry my owner.");
			else {
				Role role = event.getGuild().getController().createRole().setName(String.format("%s " + heart + " %s", mem1.getUser().getName(), mem2.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions().complete();
				event.getGuild().getController().addSingleRoleToMember(mem1, role).queue();
				event.getGuild().getController().addSingleRoleToMember(mem2, role).queue();
				if (!marriages.containsKey(event.getGuild().getId())) marriages.put(event.getGuild().getId(), new HashMap());
				marriages.get(event.getGuild().getId()).put(role.getId(), Main.newHashMap(new String[] {"owners", "at", "children"}, new Object[] {Lists.newArrayList(mem1.getUser().getId(), mem2.getUser().getId()), System.currentTimeMillis(), new HashMap()}));
				try {
					DataIO.saveJson(marriages, "data/marriage/marriages.json");
				} catch (IOException e) {
					marriages.get(event.getGuild().getId()).remove(role.getId());
					role.delete().queue();
					throw new CommandException("The data file could not be saved, no one was actually married.");
				}
				if (marriageChannel != null && marriageChannel.canTalk() && marriageChannel.canTalk()) marriageChannel.sendMessageFormat("%s was forced to marry %s.", mem2.getAsMention(), mem1.getAsMention()).queue();
				try {
					Main.sendPrivateMessage(mem1.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.", event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem2.getUser().getName(), mem2.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
				} catch (Throwable e) {
					event.reply("I wasn't able to send a direct message to " + mem1.getUser().getName() + ".");
				}
				try {
					Main.sendPrivateMessage(mem2.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.", event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem1.getUser().getName(), mem1.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
				} catch (Throwable e) {
					event.reply("I wasn't able to send a direct message to " + Main.str(mem2.getUser()) + ".");
				}
				event.reply("Successfully forced %s to marry %s.", mem2.getUser().getName(), mem1.getUser().getName());
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Marriage", help = "Marry your crush.", name = "marry", arguments = "<crush>", botPermissions = {Permission.MANAGE_ROLES}, cooldown = 60, guildOnly = true)
	public static void marry(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null)
				event.reply("The given user could not be found.");
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
				TextChannel marriageChannel = getMarriageChannel(event.getGuild());
				boolean isMarriedToMember = false;
				for (Role role : marriageRolesOfAuthor)
					if (role.getName().contains(member.getUser().getName())) isMarriedToMember = true;
				for (Role role : marriageRolesOfCrush)
					if (role.getName().contains(event.getAuthor().getName())) isMarriedToMember = true;
				if (event.getMember().equals(member))
					event.reply("You can't marry yourself, that would be weird wouldn't it?");
				else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled"))
					event.reply("Marriages are currently disabled in this server.");
				else if (marryLimit > 0 && marriageRolesOfAuthor.size() >= marryLimit)
					event.reply("You have reached this server's marry limit. (" + marryLimit + ")");
				else if (marryLimit > 0 && marriageRolesOfCrush.size() >= marryLimit)
					event.reply("The user you're trying to marry has reached their marry limit. (" + marryLimit + ")");
				else if (isMarriedToMember)
					event.reply("You're already married to that person.");
				else if (member.equals(event.getSelfMember()) && !event.getMember().getUser().equals(Main.getOwner()))
					event.reply("I'd only marry my owner.");
				else {
					event.reply("%s, do you take %s as your husband/wife? (yes/no)", member.getAsMention(), event.getAuthor().getAsMention());
					Message response = Main.waitForInput(member, event.getChannel(), 60000);
					if (response == null)
						event.reply("%s, the user you tried to marry did not respond, I'm sorry.", event.getAuthor().getAsMention());
					else if (!response.getContentDisplay().toLowerCase().contains("ye"))
						event.reply("%s, the user you tried to marry did not say yes, I'm sorry.", event.getAuthor().getAsMention());
					else {
						List<Permission> permissions = new ArrayList();
						for (Permission perm : Main.defaultPermissions)
							if (event.getSelfMember().hasPermission(perm)) permissions.add(perm);
						Role role = event.getGuild().getController().createRole().setName(String.format("%s " + heart + " %s", event.getAuthor().getName(), member.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions(permissions).complete();
						event.getGuild().getController().addSingleRoleToMember(event.getMember(), role).queue();
						event.getGuild().getController().addSingleRoleToMember(member, role).queue();
						if (!marriages.containsKey(event.getGuild().getId())) marriages.put(event.getGuild().getId(), new HashMap());
						marriages.get(event.getGuild().getId()).put(role.getId(), Main.newHashMap(new String[] {"owners", "at", "children"}, new Object[] {Lists.newArrayList(event.getAuthor().getId(), member.getUser().getId()), System.currentTimeMillis(), new HashMap()}));
						try {
							DataIO.saveJson(marriages, "data/marriage/marriages.json");
						} catch (IOException e) {
							marriages.get(event.getGuild().getId()).remove(role.getId());
							role.delete().queue();
							throw new CommandException("The data file could not be saved, no one was actually married.");
						}
						if (marriageChannel != null && marriageChannel.canTalk()) marriageChannel.sendMessageFormat("%s married %s, congratulations!", event.getAuthor().getAsMention(), member.getAsMention()).queue();
						try {
							Main.sendPrivateMessage(event.getAuthor(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.", member.getUser().getName(), member.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
						} catch (Throwable e) {
							event.reply("I wasn't able to send a direct message to " + event.getAuthor().getName() + ".");
						}
						try {
							Main.sendPrivateMessage(member.getUser(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.", event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getPrefix(event.getGuild()), role.getId(), event.getGuild().getName()));
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
		if (!settings.containsKey(event.getGuild().getId()) || Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) < 1)
			event.reply("This server has no marrylimit.");
		else event.reply("This server's marry limit is currently set to " + Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("marryLimit")) + ".");
	}

	@Command(category = "Marriage", help = "Divorces everyone in this server.", name = "massdivorce", guildOnly = true, userPermissions = {Permission.MANAGE_ROLES}, botPermissions = {Permission.MANAGE_ROLES})
	public static void massDivorce(CommandEvent event) throws CommandException {
		if (marriages.containsKey(event.getGuild().getId())) {
			int success = 0;
			int failed = 0;
			for (String role : new ArraySet<>(marriages.get(event.getGuild().getId()).keySet())) {
				if (event.getGuild().getRoleById(role) != null) try {
					event.getGuild().getRoleById(role).delete().queue();
					success += 1;
				} catch (Exception e) {
					failed += 1;
				}
				marriages.remove(role);
			}
			try {
				DataIO.saveJson(marriages, "data/marriage/marriages.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the data file, try again.", e);
			}
			event.reply("Successfully deleted %s marriage roles and failed to delete %s marriage roles in this server.", success, failed);
		} else event.reply("No one in this server is currently married.");
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

	@Command(category = "Marriage", help = "Manage your family, you need to marry someone first, of course.", name = "family", guildOnly = true)
	public static void family(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Procreate with your maritus and create a baby!", name = "procreate", parent = "com.impulsebot.commands.Marriage.family", arguments = "<maritus>", guildOnly = true)
	public static void familyProcreate(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null)
				event.reply("The given user could not be found.");
			else if (user == event.getAuthor())
				event.reply("The argument maritus cannot be you.");
			else {
				Map<String, Map<String, Object>> role = null;
				for (Entry<String, Map<String, Map<String, Object>>> entry : new ArrayList<Entry<String, Map<String, Map<String, Object>>>>(marriages.getOrDefault(event.getGuild().getId(), new HashMap()).entrySet()))
					if (((List<String>) entry.getValue().get("owners")).contains(event.getAuthor().getId()) && ((List<String>) entry.getValue().get("owners")).contains(user.getId())) {
						role = entry.getValue();
						break;
					}
				if (role != null) {
					if (((Map) role.get("children")).size() >= 4) {
						event.reply("You can only have 4 children with your maritus, 5 is just too many.");
						return;
					}
					Map<String, Object> baby = Main.newHashMap(new String[] {"name", "gender", "created_at", "satisfaction", "health", "fatigue", "defilement", "euphoria", "sleeping"}, new Object[] {"", Random.INSTANCE.choice("m", "f"), System.currentTimeMillis(), 100, 100, 0, 0, 100, false});
					event.reply("It's a **%s**! What do you want %s name to be? (type 'random' for a random name)", baby.get("gender").equals("m") ? "boy" : "girl", baby.get("gender").equals("m") ? "his" : "her");
					Message response = null;
					while (response == null) {
						response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
						if (response == null) {
							event.reply("No response gotten, the baby has been disposed of. :disappointed:");
							return;
						} else {
							String name = response.getContentDisplay().equalsIgnoreCase("random") ? new NameGenerator().generateName(baby.get("gender").equals("m") ? Gender.MALE : Gender.FEMALE).getFirstName() : response.getContentDisplay();
							if (name.length() > 16) {
								event.reply("Your baby's name can only be a max of 16 characters, please pick a new one. (type 'random' for a random name)");
								response = null;
								continue;
							}
							event.reply("Are you sure you want to name your %s kid **%s**? This **cannot** be changed later. (yes/no)", baby.get("gender").equals("m") ? "male" : "female", name);
							response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
							if (response == null) {
								event.reply("No response gotten, the baby has been disposed of. :disappointed:");
								return;
							} else if (response.getContentDisplay().toLowerCase().startsWith("y")) {
								baby.put("name", name);
								((Map) role.get("children")).put(name, baby);
								try {
									DataIO.saveJson(marriages, "data/marriage/marriages.json");
								} catch (IOException e) {
									throw new CommandException("An unknown error occurred while saving the data file.", e);
								}
								if (getMarriageChannel(event.getGuild()) != null) getMarriageChannel(event.getGuild()).sendMessageFormat("%s and %s just became the parents of a %s baby called %s, congratulations!", event.getMember().getAsMention(), user.getAsMention(), baby.get("gender").equals("m") ? "male" : "female", baby.get("name")).queue();
								event.reply("Congratulations on your new baby! Care for your new baby with the %sfamily children command.", Main.getPrefix(event.getGuild()));
							} else {
								event.reply("Then what *do* you want to name your baby? (type 'random' for a random name)");
								response = null;
							}
						}
					}
				} else event.reply("You're not married with that user, you can't procreate with strangers, smh, thot.");
			}
		} else event.sendCommandHelp();
	}

	@Subcommand(help = "Care for your kids.", name = "children", parent = "com.impulsebot.commands.Marriage.family", guildOnly = true)
	public static void familyChildren(CommandEvent event) {
		event.sendCommandHelp();
	}

	@Subcommand(help = "List all your children and their status.", name = "list", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus>")
	public static void familyChildrenList(CommandEvent event) {
		if (!event.argsEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == event.getAuthor())
				event.reply("The argument maritus cannot be you.");
			else if (user != null) {
				if (marriages.containsKey(event.getGuild().getId())) {
					for (Entry<String, Map<String, Object>> entry : marriages.get(event.getGuild().getId()).entrySet())
						if (((List<String>) entry.getValue().get("owners")).containsAll(Lists.newArrayList(event.getAuthor().getId(), user.getId()))) {
							if (!((Map) entry.getValue().get("children")).isEmpty()) {
								String stats = "";
								for (Map<String, Object> baby : ((Map<String, Map<String, Object>>) entry.getValue().get("children")).values()) {
									// @formatter:off
									String age = (System.currentTimeMillis() - Main.getLongFromPossibleDouble(baby.get("created_at"))) / 1000 / 60 / 60 / 24 + " days";
									stats += baby.get("name") + Main.multiplyString(" ", 15 - baby.get("name").toString().length()) +
											(baby.get("gender").equals("m") ? "Male  " : "Female") + " " +
											age + Main.multiplyString(" ", 11 - age.length()) +
											baby.get("satisfaction") + "%" + Main.multiplyString(" ", 11 - baby.get("satisfaction").toString().length()) +
											baby.get("health") + "%" + Main.multiplyString(" ", 5 - baby.get("health").toString().length()) +
											baby.get("fatigue") + "%" + Main.multiplyString(" ", 6 - baby.get("fatigue").toString().length()) +
											baby.get("defilement") + "%" + Main.multiplyString(" ", 9 - baby.get("defilement").toString().length()) +
											baby.get("euphoria") + "%" + Main.multiplyString(" ", 7 - baby.get("euphoria").toString().length()) +
											baby.get("sleeping") + "\n";
									// @formatter:on
								}
								event.reply("```\nName\t\t\tGender Age\t\t Satisfaction Health Fatigue Defilement Euphoria Sleeping\n%s```", stats.trim());
							} else event.reply("You and **%s** do not yet have any children.", Main.str(user));
							return;
						}
					event.reply("You're not married with that person.");
				} else event.reply("No one in this server is married yet.");
			} else event.reply("The given user could not be found.");
		} else event.sendCommandHelp();
	}

	@Subcommand(help = "Feed your child so it won't die.", name = "feed", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus> <child>")
	public static void familyChildrenFeed(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 2) {
			Map<String, Object> child = null;
			try {
				child = getChild(event);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (e.getMessage().equals("not married"))
						event.reply("You're not married with that person.");
					else if (e.getMessage().equals("same user"))
						event.reply("The argument maritus cannot be you.");
					else if (e.getMessage().equals("user not found")) event.reply("The given user could not be found.");
				} else throw new CommandException("An unknown error occurred while getting the child connected to that name.", e);
				return;
			}
			if (child == null)
				event.reply("The given child could not be found.");
			else {
				int satisfaction = Main.getIntFromPossibleDouble(child.get("satisfaction"));
				int i = Random.INSTANCE.randInt(5, 10);
				if (satisfaction + i >= 100)
					event.reply("You cannot feed your child now as there's no need to.");
				else if (!Economy.hasAccount(event.getMember()))
					event.reply("You cannot feed your child as you don't have a bank account.");
				else if (!Economy.hasEnoughBalance(event.getMember(), 50))
					event.reply("You cannot feed your child as you don't have enough money.");
				else {
					Economy.subtractBalance(event.getMember(), 50);
					child.put("satisfaction", satisfaction + i);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Successfully fed your child!");
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Heal your child so it won't die.", name = "heal", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus> <child>")
	public static void familyChildrenHeal(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 2) {
			Map<String, Object> child = null;
			try {
				child = getChild(event);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (e.getMessage().equals("not married"))
						event.reply("You're not married with that person.");
					else if (e.getMessage().equals("same user"))
						event.reply("The argument maritus cannot be you.");
					else if (e.getMessage().equals("user not found")) event.reply("The given user could not be found.");
				} else throw new CommandException("An unknown error occurred while getting the child connected to that name.", e);
				return;
			}
			if (child == null)
				event.reply("The given child could not be found.");
			else {
				int health = Main.getIntFromPossibleDouble(child.get("health"));
				int i = Random.INSTANCE.randInt(10, 20);
				if (health + i >= 100)
					event.reply("You cannot heal your child now as there's no need to.");
				else if (!Economy.hasAccount(event.getMember()))
					event.reply("You cannot heal your child as you don't have a bank account.");
				else if (!Economy.hasEnoughBalance(event.getMember(), 100))
					event.reply("You cannot heal your child as you don't have enough money.");
				else {
					Economy.subtractBalance(event.getMember(), 50);
					child.put("health", health + i);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Successfully healed your child!");
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Take your child to bed so it won't get tired.", name = "sleep", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus> <child>")
	public static void familyChildrenSleep(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 2) {
			Map<String, Object> child = null;
			try {
				child = getChild(event);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (e.getMessage().equals("not married"))
						event.reply("You're not married with that person.");
					else if (e.getMessage().equals("same user"))
						event.reply("The argument maritus cannot be you.");
					else if (e.getMessage().equals("user not found")) event.reply("The given user could not be found.");
				} else throw new CommandException("An unknown error occurred while getting the child connected to that name.", e);
				return;
			}
			if (child == null)
				event.reply("The given child could not be found.");
			else {
				int fatigue = Main.getIntFromPossibleDouble(child.get("fatigue"));
				Random.INSTANCE.randInt(10, 20);
				if (fatigue == 100 && !(boolean) child.get("sleeping"))
					event.reply("You cannot take your child to bed atm as there's no need to.");
				else if ((boolean) child.get("sleeping")) {
					child.put("sleeping", false);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Successfully woke your child up!");
				} else {
					child.put("sleeping", true);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Successfully took your child to bed!");
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Clean your child so it won't get sick and die.", name = "clean", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus> <child>")
	public static void familyChildrenClean(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 2) {
			Map<String, Object> child = null;
			try {
				child = getChild(event);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (e.getMessage().equals("not married"))
						event.reply("You're not married with that person.");
					else if (e.getMessage().equals("same user"))
						event.reply("The argument maritus cannot be you.");
					else if (e.getMessage().equals("user not found")) event.reply("The given user could not be found.");
				} else throw new CommandException("An unknown error occurred while getting the child connected to that name.", e);
				return;
			}
			if (child == null)
				event.reply("The given child could not be found.");
			else {
				int defilement = Main.getIntFromPossibleDouble(child.get("defilement"));
				int i = Random.INSTANCE.randInt(5, 10);
				if (defilement + i >= 100)
					event.reply("You cannot clean your child now as there's no need to.");
				else if (!Economy.hasAccount(event.getMember()))
					event.reply("You cannot clean your child as you don't have a bank account.");
				else if (!Economy.hasEnoughBalance(event.getMember(), 50))
					event.reply("You cannot clean your child as you don't have enough money.");
				else {
					Economy.subtractBalance(event.getMember(), 50);
					child.put("defilement", defilement + i);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Successfully cleaned your child!");
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Play with your child so it doesn't become sad and bored.", name = "play", parent = "com.impulsebot.commands.Marriage.familyChildren", guildOnly = true, arguments = "<maritus> <child>")
	public static void familyChildrenPlay(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 2) {
			Map<String, Object> child = null;
			try {
				child = getChild(event);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					if (e.getMessage().equals("not married"))
						event.reply("You're not married with that person.");
					else if (e.getMessage().equals("same user"))
						event.reply("The argument maritus cannot be you.");
					else if (e.getMessage().equals("user not found")) event.reply("The given user could not be found.");
				} else throw new CommandException("An unknown error occurred while getting the child connected to that name.", e);
				return;
			}
			if (child == null)
				event.reply("The given child could not be found.");
			else {
				int euphoria = Main.getIntFromPossibleDouble(child.get("euphoria"));
				int i = Random.INSTANCE.randInt(5, 10);
				if (euphoria + i >= 100)
					event.reply("You cannot play with your child now as there's no need to.");
				else {
					child.put("euphoria", euphoria + i);
					try {
						DataIO.saveJson(marriages, "data/marriage/marriages.json");
					} catch (IOException e) {
						throw new CommandException(e);
					}
					event.reply("Aww %s looks so happy!", child.get("gender").equals("m") ? "he" : "she");
				}
			}
		} else Main.sendCommandHelp(event);
	}

	private static final Map<String, Object> getChild(CommandEvent event) throws Exception {
		User maritus = null;
		boolean mentioned = false;
		if (!event.getMessage().getMentionedUsers().isEmpty()) {
			maritus = event.getMessage().getMentionedUsers().get(0);
			mentioned = true;
		} else {
			String[] args = event.getArgs().split(" ");
			for (int i = 1; i < args.length; i++) {
				String name = "";
				for (int x : Main.range(i))
					name += args[x] + " ";
				if (!event.getGuild().getMembersByName(name.trim(), true).isEmpty()) {
					maritus = event.getGuild().getMembersByName(name.trim(), true).get(0).getUser();
					break;
				}
			}
		}
		if (maritus == null) throw new Exception("user not found");
		if (maritus == event.getAuthor()) throw new Exception("same user");
		if (!isMarried(event.getAuthor(), maritus, event.getGuild())) throw new Exception("not married");
		String name = "";
		if (!mentioned)
			name = Main.join(Main.removeArgs(event.getArgs().split(" "), Main.intArrayToIntegerArray(new int[maritus.getName().split(" ").length])));
		else name = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
		for (Entry<String, Map<String, Map<String, Object>>> guild : marriages.entrySet())
			for (Entry<String, Map<String, Object>> role : guild.getValue().entrySet())
				for (Entry<String, Map<String, Object>> child : ((Map<String, Map<String, Object>>) role.getValue().get("children")).entrySet())
					if (child.getKey().equalsIgnoreCase(name)) return child.getValue();
		return null;
	}

	private static final boolean isMarried(User user1, User user2, Guild guild) {
		if (user1 == null || user2 == null || guild == null) return false;
		for (Entry<String, Map<String, Map<String, Object>>> entry : new ArrayList<Entry<String, Map<String, Map<String, Object>>>>(marriages.getOrDefault(guild.getId(), new HashMap()).entrySet()))
			if (((List<String>) entry.getValue().get("owners")).contains(user1.getId()) && ((List<String>) entry.getValue().get("owners")).contains(user2.getId())) return true;
		return false;
	}

	@Subcommand(help = "Gives you some information about this entire family stuff.", name = "info", parent = "com.impulsebot.commands.Marriage.familyChildren")
	public static void familyChildrenInfo(CommandEvent event) {
		event.reply("Once you become a parent, there is no turning back, you gotta care for that *thing* you just created, maybe it was an accident, maybe it wasn't, who cares? You gotta care for it anyway. So, now you may be asking what do all these values mean? Well, here's your answer:\n\t**Gender**: same as sex, either male or female: lil sausage or entry.\n\t**Age**: how old your baby is, duh.\n\t**Satisfaction**: how well fed your baby is, goes down randomly.\n\t**Health**: how healthy your baby is, goes down if either: satisfaction is less than 40, fatigue is greater than 60 or if defilement is greater than 60.\n\t**Fatigue**: how tired your baby is, goes up randomly, but faster if euphoria is below 40.\n\t**Defilement**: how dirty your baby is, goes up randomly.\n\t**Euphoria**: how happy your baby is, goes down randomly.\n\nI hope this made everything a bit more clear, good luck with your baby and have fun, if it gives you any. \nP.S. maritus is Latin for husband.");
	}

	public static int getMarryLimit(Guild guild) {
		if (settings.containsKey(guild.getId()))
			return Main.getIntFromPossibleDouble(((Map) settings.get(guild.getId())).get("marryLimit"));
		else return 0;
	}

	public static TextChannel getMarriageChannel(Guild guild) {
		TextChannel marriageChannel = null;
		try {
			if (guild.getTextChannelsByName("marriage", true).isEmpty()) marriageChannel = guild.getTextChannelById(guild.getController().createTextChannel("marriage").complete().getId());
			if (marriageChannel == null) marriageChannel = Main.getOrDefault(guild.getTextChannelsByName("marriage", true), 0, null);
		} catch (Exception e) {
		}
		return marriageChannel;
	}

}
