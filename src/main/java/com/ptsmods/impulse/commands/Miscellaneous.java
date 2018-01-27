package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Miscellaneous {

	private static Map settings;

	static {
		try {
			settings = DataIO.loadJson("data/customcommands/settings.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
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
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Creates an email account.", name = "create", parent = "com.ptsmods.impulse.commands.Miscellaneous.email", dmOnly = true)
	public static void emailCreate(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			if (MailServer.isEnabled()) {
				boolean success;
				try {
					success = MailServer.createMailAddress(event.getAuthor().getId(), event.getArgs());
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while creating the email address.", e);
				}
				if (success) event.reply("Successfully made an email account, login info:\n\tEmail address: %s@%s\n\tPassword: %s\n\nTo see how to log in on any email client type %semail options", event.getAuthor().getId(), Config.get("mailBaseUrl"), event.getArgs(), Main.getPrefix(event.getGuild()));
				else event.reply("An email address could not be created.");
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

}
