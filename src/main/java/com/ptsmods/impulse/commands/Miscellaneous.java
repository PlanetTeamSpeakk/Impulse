package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.Main;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.DataIO;
import com.ptsmods.impulse.utils.MailServer;
import com.ptsmods.impulse.utils.Random;
import com.ptsmods.impulse.utils.Url2Png;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Miscellaneous {

	private static Map settings;
	private static Map<String, Map<String, String>> patronEmails;

	static {
		try {
			settings = DataIO.loadJsonOrDefault("data/customcommands/settings.json", Map.class, new HashMap());
			patronEmails = DataIO.loadJsonOrDefault("data/other/patronEmails.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("There was an error while loading the data file.", e);
		}
	}

	@Command(category = "Miscellaneous", help = "Manage custom commands.", name = "customcom", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customCom(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Create a custom command. \nExample: [p]customcom create does this work?; Yes, it does.", name = "create", parent = "com.ptsmods.impulse.commands.Miscellaneous.customCom", arguments = "<command>; <message>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customComCreate(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getArgs().split(";").length >= 2 && event.getArgs().split("; ").length >= 2) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), new HashMap());
			String[] args = event.getArgs().contains("; ") ? event.getArgs().split("; ") : event.getArgs().split(";");
			((Map) settings.get(event.getGuild().getId())).put(args[0], args[1]);
			try {
				DataIO.saveJson(settings, "data/customcommands/settings.json");
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occurred while loading the data file.", e);
			}
			event.reply("Successfully added the custom command.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Remove a custom command.", name = "remove", parent = "com.ptsmods.impulse.commands.Miscellaneous.customCom", arguments = "<command>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customComRemove(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no custom commands.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(event.getArgs())) event.reply("That custom command does not seem to exist.");
			else {
				((Map) settings.get(event.getGuild().getId())).remove(event.getArgs());
				try {
					DataIO.saveJson(settings, "data/customcommands/settings.json");
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully removed the custom command.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Edit a custom command.", name = "edit", parent = "com.ptsmods.impulse.commands.Miscellaneous.customCom", arguments = "<command>; <message>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customComEdit(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getArgs().split(";").length >= 2 || event.getArgs().split("; ").length >= 2) {
			String[] args = event.getArgs().contains("; ") ? event.getArgs().split("; ") : event.getArgs().split(";");
			if (!settings.containsKey(event.getGuild().getId())) event.reply("This server has no custom commands.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(args[0])) event.reply("That custom command does not seem to exist.");
			else {
				((Map) settings.get(event.getGuild().getId())).put(args[0], args[1]);
				try {
					DataIO.saveJson(settings, "data/customcommands/settings.json");
				} catch (IOException e) {
					throw new RuntimeException("An unknown error occurred while loading the data file.", e);
				}
				event.reply("Successfully added the custom command.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Miscellaneous", help = "Manage your email account", name = "email", dmOnly = true)
	public static void email(CommandEvent event) {
		if (MailServer.isEnabled()) Main.sendCommandHelp(event);
		else event.reply("This feature has not been enabled by the owner of this bot.");
	}

	@Subcommand(help = "Creates an email account.", name = "create", parent = "com.ptsmods.impulse.commands.Miscellaneous.email", dmOnly = true, arguments = "<password>")
	public static void emailCreate(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			if (MailServer.isEnabled()) {
				if (event.getArgs().length() >= 8) {
					boolean success;
					try {
						success = MailServer.createMailAddress(event.getAuthor().getId(), event.getArgs(), true);
					} catch (IOException e) {
						throw new CommandException("An unknown error occurred while creating the email address.", e);
					}
					if (success) event.reply("Successfully made an email account, login info:\n\tEmail address: %s@%s\n\tPassword: %s\n\nTo see how to log in on any email client type %semail options", event.getAuthor().getId(), Config.get("mailBaseUrl"), event.getArgs(), Main.getPrefix(event.getGuild()));
					else event.reply("An email address could not be created.");
				} else event.reply("The password must be a minimum of 8 characters long.");
			} else event.reply("This feature has not been enabled by the owner of this bot.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Tells you the options to log in on any email client.", name = "options", parent = "com.ptsmods.impulse.commands.Miscellaneous.email")
	public static void emailOptions(CommandEvent event) {
		if (MailServer.isEnabled())
			event.reply(new EmbedBuilder()
					.setColor(new Color(Random.randInt(256*256*256)))
					.addField("Type", Boolean.parseBoolean(Config.get("miabIMAP")) ? "IMAP" : "POP", true)
					.addField("Server", Config.get("miabServer"), true)
					.addField("Incoming port", Config.get("miabIncomingPort"), true)
					.addField("Outgoing port", Config.get("miabOutgoingPort"), true)
					.addField("Incoming security", Config.get("miabIncomingSecurity"), true)
					.addField("Outgoing security", Config.get("miabOutgoingSecurity"), true)
					.addField("SMTP always verify", Config.get("miabSmtpAlwaysVerify"), true)
					.build());
		else event.reply("This feature has not been enabled by the owner of this bot.");
	}

	@Command(category = "Miscellaneous", help = "A command for Patrons to create custom email addresses.", name = "pemail", dmOnly = true)
	public static void pemail(CommandEvent event) {
		if (isPatron(event.getAuthor()) && MailServer.isEnabled())
			Main.sendCommandHelp(event);
		else event.reply("You're not a patron or this feature has not been enabled by the owner of this bot so you cannot use this command.");
	}

	@Subcommand(help = "Create your custom patron email address.", name = "create", parent = "com.ptsmods.impulse.commands.Miscellaneous.pemail", arguments = "<name> <password>", dmOnly = true)
	public static void pemailCreate(CommandEvent event) throws CommandException {
		if (isPatron(event.getAuthor()) && MailServer.isEnabled()) {
			if (event.getArgs().split(" ").length >= 2) {
				String name = event.getArgs().split(" ")[0];
				String password = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
				if (password.length() >= 8) {
					try {
						MailServer.createMailAddress(name, password, patronEmails.containsKey(event.getAuthor().getId()) ? patronEmails.get(event.getAuthor().getId()).containsKey(name+"@"+Config.get("mailBaseUrl")) : false);
					} catch (IOException e) {
						event.reply("An unknown error occurred while making the email address, this is most likely due to it already being registered by someone other than you and thus you cannot change its password, if you don't think this is the case, please contact my owner using %scontact.", Main.getPrefix(event.getGuild()));
						e.printStackTrace();
						return;
					}
					if (!patronEmails.containsKey(event.getAuthor().getId())) patronEmails.put(event.getAuthor().getId(), new HashMap());
					patronEmails.get(event.getAuthor().getId()).put(name+"@"+Config.get("mailBaseUrl"), password);
					try {
						DataIO.saveJson(patronEmails, "data/other/patronEmails.json");
					} catch (IOException e) {
						throw new CommandException(String.format("An unknown error occurred while saving your email address to the data file, it should be made, but it won't appear in %spemail list.", Main.getPrefix(event.getGuild())), e);
					}
					event.reply("Successfully made your custom email address, your email address is **%s@%s** and your password is **%s**, type %spemail options for the options used to log in.", name, Config.get("mailBaseUrl"), password, Main.getPrefix(null));
				} else event.reply("The password must be a minimum of 8 characters long.");
			} else Main.sendCommandHelp(event);
		} else event.reply("You're not a patron or this feature has not been enabled by the owner of this bot so you cannot use this command.");
	}

	@Subcommand(help = "Lists all your custom patron emails, to make it show passwords run it with [p]pemail list -showpass.", name = "list", parent = "com.ptsmods.impulse.commands.Miscellaneous.pemail")
	public static void pemailList(CommandEvent event) {
		if (patronEmails.containsKey(event.getAuthor().getId())) {
			int maxLength = 6;
			for (String key : patronEmails.get(event.getAuthor().getId()).keySet())
				if (key.length() > maxLength) maxLength = key.length()+1;
			String msg = "```\nEmail"+Main.multiplyString("*", maxLength-5)+"Password";
			for (Entry<String, String> email : patronEmails.get(event.getAuthor().getId()).entrySet()) {
				msg += "\n" + email.getKey() + Main.multiplyString(" ", maxLength-email.getKey().length())+(event.getArgs().contains("-showpass") ? email.getValue() : Main.multiplyString("*", email.getValue().length()));
				if (msg.length() > 1900) {
					event.reply(msg.trim()+"```");
					msg = "```\n";
				}
			}
			event.reply(msg+"```\n");
		} else event.reply("You have no custom emails.");
	}

	@Subcommand(help = "Shows you all the options necessary for logging in on any email client.", name = "options", parent = "com.ptsmods.impulse.commands.Miscellaneous.pemail")
	public static void pemailOptions(CommandEvent event) {
		emailOptions(event);
	}

	@Command(category = "Miscellaneous", help = "Captures a screenshot of a website.", name = "capture", arguments = "<url>", cooldown = 120)
	public static void capture(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			if (event.getArgs().startsWith("<") && event.getArgs().endsWith(">")) event.setArgs(event.getArgs().substring(1, event.getArgs().length()-1));
			try {
				new URL(event.getArgs());
			} catch (MalformedURLException e) {
				event.reply("The given URL is not valid, please make sure it starts with either http:// or https://.");
				return;
			}
			long current = System.nanoTime();
			Message msg = event.getChannel().sendMessageFormat("Capturing a screenshot of <%s>, please wait...", event.getArgs()).complete();
			try {
				File capture = Url2Png.capture(event.getArgs());
				event.getChannel().sendFile(capture, new MessageBuilder().appendFormat("Done capturing <%s>, took **%s milliseconds**.", event.getArgs(), (System.nanoTime()-current) / 1000000D).build()).complete();
				msg.delete().queue();
				capture.delete();
			} catch (IllegalArgumentException e) {
				msg.editMessage("The captured screenshot was too big to be sent.").queue();
			} catch (IOException e) {
				if (e.getMessage() != null && e.getMessage().contains("403")) msg.editMessage("That URL is not allowed.").queue();
				else if (e.getMessage() != null && e.getMessage().contains("Server returned HTTP response code")) msg.editMessageFormat("The server returned a **%s error**.", e.getMessage().substring(36, 39)).queue();
				else throw new CommandException("An unknown error occurred while capturing the website.", e);
			}
		} else Main.sendCommandHelp(event);
	}

	@SubscribeEvent
	public static void onMessageReceived(MessageReceivedEvent event) {
		if (event.getGuild() == null) return;
		String serverPrefix = Main.getPrefix(event.getGuild());
		if (event.getMessage().getContent().startsWith(serverPrefix))
			if (settings.containsKey(event.getGuild().getId()) && ((Map) settings.get(event.getGuild().getId())).containsKey(event.getMessage().getContent().substring(serverPrefix.length()))) {
				try {
					Map blacklist = DataIO.loadJson("data/mod/blacklist.json", Map.class);
					blacklist = blacklist == null ? new HashMap() : blacklist;
					if (event.getGuild() != null && blacklist.containsKey(event.getGuild().getId()) && ((List) blacklist.get(event.getGuild().getId())).contains(event.getAuthor().getId())) return;
					else if (blacklist.containsKey("global") && ((List) blacklist.get("global")).contains(event.getAuthor().getId())) return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				event.getChannel().sendMessage((String) ((Map) settings.get(event.getGuild().getId())).get(event.getMessage().getContent().substring(serverPrefix.length()))).queue();
			}
	}

	public static boolean isPatron(User user) {
		Guild impulseGuild = Main.getGuildById("234356084398096394");
		if (impulseGuild != null) {
			Member member = impulseGuild.getMember(user);
			Role patronRole = impulseGuild.getRoleById("421057366960635904");
			return member != null && patronRole != null && member.getRoles().contains(patronRole);
		}
		return false;
	}

}
