package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.ptsmods.impulse.Main.TimeType;
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
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.webhook.WebhookClient;

public class Miscellaneous {

	private static Map									settings;
	private static Map<String, Map<String, String>>		patronEmails;
	private static Map<String, String>					rifts;
	private static Map<String, Map<String, Webhook>>	riftWebhooks	= new HashMap();
	private static List<Map<String, Object>>			reminders;

	static {
		try {
			settings = DataIO.loadJsonOrDefault("data/customcommands/settings.json", Map.class, new HashMap());
			patronEmails = DataIO.loadJsonOrDefault("data/other/patronEmails.json", Map.class, new HashMap());
			rifts = DataIO.loadJsonOrDefault("data/miscellaneous/rifts.json", Map.class, new HashMap()); // for when the bot dies in the middle of it.
			reminders = DataIO.loadJsonOrDefault("data/miscellaneous/reminders.json", List.class, new ArrayList());
		} catch (IOException e) {
			throw new RuntimeException("There was an error while loading the data files.", e);
		}
	}

	@Command(category = "Miscellaneous", help = "Manage custom commands.", name = "customcom", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customCom(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Create a custom command. \nExample: [p]customcom create does this work?; Yes, it does.", name = "create", parent = "com.ptsmods.impulse.commands.Miscellaneous.customCom", arguments = "<command>; <message>", userPermissions = {Permission.KICK_MEMBERS}, guildOnly = true)
	public static void customComCreate(CommandEvent event) {
		if (!event.getArgs().isEmpty() && (event.getArgs().split(";").length >= 2 || event.getArgs().split("; ").length >= 2)) {
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
			if (!settings.containsKey(event.getGuild().getId()))
				event.reply("This server has no custom commands.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(event.getArgs()))
				event.reply("That custom command does not seem to exist.");
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
			if (!settings.containsKey(event.getGuild().getId()))
				event.reply("This server has no custom commands.");
			else if (!((Map) settings.get(event.getGuild().getId())).containsKey(args[0]))
				event.reply("That custom command does not seem to exist.");
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
		if (MailServer.isEnabled())
			Main.sendCommandHelp(event);
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
					if (success)
						event.reply("Successfully made an email account, login info:\n\tEmail address: %s@%s\n\tPassword: %s\n\nTo see how to log in on any email client type %semail options", event.getAuthor().getId(), Config.get("mailBaseUrl"), event.getArgs(), Main.getPrefix(event.getGuild()));
					else event.reply("An email address could not be created.");
				} else event.reply("The password must be a minimum of 8 characters long.");
			} else event.reply("This feature has not been enabled by the owner of this bot.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Tells you the options to log in on any email client.", name = "options", parent = "com.ptsmods.impulse.commands.Miscellaneous.email")
	public static void emailOptions(CommandEvent event) {
		if (MailServer.isEnabled())
			event.reply(new EmbedBuilder().setColor(new Color(Random.randInt(256 * 256 * 256))).addField("Type", Boolean.parseBoolean(Config.get("miabIMAP")) ? "IMAP" : "POP", true).addField("Server", Config.get("miabServer"), true).addField("Incoming port", Config.get("miabIncomingPort"), true).addField("Outgoing port", Config.get("miabOutgoingPort"), true).addField("Incoming security", Config.get("miabIncomingSecurity"), true).addField("Outgoing security", Config.get("miabOutgoingSecurity"), true).addField("SMTP always verify", Config.get("miabSmtpAlwaysVerify"), true).build());
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
						MailServer.createMailAddress(name, password, patronEmails.containsKey(event.getAuthor().getId()) ? patronEmails.get(event.getAuthor().getId()).containsKey(name + "@" + Config.get("mailBaseUrl")) : false);
					} catch (IOException e) {
						event.reply("An unknown error occurred while making the email address, this is most likely due to it already being registered by someone other than you and thus you cannot change its password, if you don't think this is the case, please contact my owner using %scontact.", Main.getPrefix(event.getGuild()));
						e.printStackTrace();
						return;
					}
					if (!patronEmails.containsKey(event.getAuthor().getId())) patronEmails.put(event.getAuthor().getId(), new HashMap());
					patronEmails.get(event.getAuthor().getId()).put(name + "@" + Config.get("mailBaseUrl"), password);
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
				if (key.length() > maxLength) maxLength = key.length() + 1;
			String msg = "```\nEmail" + Main.multiplyString("*", maxLength - 5) + "Password";
			for (Entry<String, String> email : patronEmails.get(event.getAuthor().getId()).entrySet()) {
				msg += "\n" + email.getKey() + Main.multiplyString(" ", maxLength - email.getKey().length()) + (event.getArgs().contains("-showpass") ? email.getValue() : Main.multiplyString("*", email.getValue().length()));
				if (msg.length() > 1900) {
					event.reply(msg.trim() + "```");
					msg = "```\n";
				}
			}
			event.reply(msg + "```\n");
		} else event.reply("You have no custom emails.");
	}

	@Subcommand(help = "Shows you all the options necessary for logging in on any email client.", name = "options", parent = "com.ptsmods.impulse.commands.Miscellaneous.pemail")
	public static void pemailOptions(CommandEvent event) {
		emailOptions(event);
	}

	@Command(category = "Miscellaneous", help = "Captures a screenshot of a website.", name = "capture", arguments = "<url>", cooldown = 120)
	public static void capture(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			if (event.getArgs().startsWith("<") && event.getArgs().endsWith(">")) event.setArgs(event.getArgs().substring(1, event.getArgs().length() - 1));
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
				event.getChannel().sendFile(capture, new MessageBuilder().appendFormat("Done capturing <%s>, took **%s milliseconds**.", event.getArgs(), (System.nanoTime() - current) / 1000000D).build()).complete();
				msg.delete().queue();
				capture.delete();
			} catch (IllegalArgumentException e) {
				msg.editMessage("The captured screenshot was too big to be sent.").queue();
			} catch (IOException e) {
				if (e.getMessage() != null && e.getMessage().contains("403"))
					msg.editMessage("That URL is not allowed.").queue();
				else if (e.getMessage() != null && e.getMessage().contains("Server returned HTTP response code"))
					msg.editMessageFormat("The server returned a **%s error**.", e.getMessage().substring(36, 39)).queue();
				else throw new CommandException("An unknown error occurred while capturing the website.", e);
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Miscellaneous", help = "Open or close a rift with channels.\n\nA rift is a connection between 2 channels from different or the same guild which means that everything that gets sent to channel A will be sent to channel B and vice versa.", name = "rift", guildOnly = true)
	public static void rift(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Open a rift.", name = "open", parent = "com.ptsmods.impulse.commands.Miscellaneous.rift", guildOnly = true, arguments = "<query> [guild]", userPermissions = {Permission.MESSAGE_MANAGE}, botPermissions = {Permission.MANAGE_WEBHOOKS})
	public static void riftOpen(CommandEvent event) {
		if (!event.argsEmpty()) {
			if (rifts.containsKey(event.getChannel().getId())) {
				event.reply("This channel already has an open rift, you can close it with %srift close.", Main.getPrefix(event.getGuild()));
				return;
			}
			String name = event.getArgs().split(" ")[0].toLowerCase();
			String guild = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
			int current = 1;
			String channels = "";
			List<TextChannel> possibilities = new ArrayList();
			for (TextChannel channel : Main.getTextChannels())
				if (channel.getName().contains(name) && channel.getGuild().getName().toLowerCase().contains(guild.toLowerCase())) {
					possibilities.add(channel);
					channels += "\n\t" + current++ + ". " + channel.getName() + " (" + channel.getGuild().getName() + ")";
				}
			if (!possibilities.isEmpty()) {
				Message response = null;
				int count = 0;
				while ((response == null || !Main.isInteger(response.getContent()) || Integer.parseInt(response.getContent()) > possibilities.size()) && count++ < 4) {
					event.reply("The following results were found, please respond with only the channel's corresponding integer, please reply with 'cancel' to cancel:" + channels);
					response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
					if (response == null || response.getContent().equalsIgnoreCase("cancel")) {
						event.reply("Alright, then not.");
						return;
					}
				}
				if (count == 5) {
					event.reply("No response gotten after 4 attempts.");
					return;
				}
				TextChannel chosenOne = possibilities.get(Integer.parseInt(response.getContent()) - 1);
				if (rifts.containsKey(chosenOne.getId())) {
					event.reply("The selected channel already has an open rift.");
					return;
				}
				if (chosenOne.canTalk() && chosenOne.getGuild().getSelfMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
					if (chosenOne.getId().equals(event.getChannel().getId())) {
						event.reply("Cannot open a rift from the same channel as it was opened from.");
						return;
					}
					event.reply("Channel '%s' selected, waiting for approval of a user with Manage Messages permissions there...", chosenOne.getName());
					count = 0;
					response = null;
					while ((response == null || !response.getMember().hasPermission(Permission.MESSAGE_MANAGE) && !Main.isOwner(response.getAuthor()) && !Main.isCoOwner(response.getAuthor())) && count < 5) {
						chosenOne.sendMessageFormat("**%s** has requested to open a rift to this channel from **%s** in the guild **%s**, would you like to accept? (yes/no, user with Manage Messages perm, only)", Main.str(event.getAuthor()), event.getChannel().getName(), event.getGuild().getName()).queue();
						response = Main.waitForInput(chosenOne, 30000);
						count += 1;
					}
					if (response != null && response.getContent().toLowerCase().startsWith("ye")) {
						chosenOne.sendMessageFormat("Rift opened, any messages sent to from this channel will be sent to **%s** in **%s** and vice versa. You can always do %srift close to close the rift.", event.getChannel().getName(), event.getGuild().getName(), Main.getPrefix(chosenOne.getGuild())).queue();
						event.reply("Rift opened, any messages sent to from this channel will be sent to **%s** and vice versa. You can always do %srift close to close the rift.", event.getChannel().getName(), event.getGuild().getName(), Main.getPrefix(event.getGuild()));
						try {
							DataIO.saveJson(rifts, "data/miscellaneous/rifts.json");
						} catch (IOException e) {
							event.reply("The rifts data file could not be saved, this means that this session will be lost if the bot restarts during it.");
							chosenOne.sendMessage("The rifts data file could not be saved, this means that this session will be lost if the bot restarts during it.").queue();
						}
						rifts.put(chosenOne.getId(), event.getChannel().getId());
						rifts.put(event.getChannel().getId(), chosenOne.getId());
						riftWebhooks.put(chosenOne.getId(), new HashMap());
						riftWebhooks.put(event.getChannel().getId(), new HashMap());
					} else {
						event.reply("The user **%s** declined the offer.", Main.str(response.getAuthor()));
						chosenOne.sendMessage("The offer has been declined, no further action will be taken.").queue();
					}
				} else event.reply("I cannot talk there or I don't have the 'Manage Webhooks' permission there.");
			} else event.reply("No channels were found.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Close a rift.", name = "close", parent = "com.ptsmods.impulse.commands.Miscellaneous.rift", guildOnly = true, userPermissions = {Permission.MESSAGE_MANAGE})
	public static void riftClose(CommandEvent event) throws CommandException {
		if (rifts.containsKey(event.getChannel().getId())) {
			String chosenOne = rifts.remove(event.getChannel().getId());
			rifts.remove(chosenOne);
			for (Webhook webhook : riftWebhooks.get(event.getChannel().getId()).values())
				webhook.delete().queue();
			for (Webhook webhook : riftWebhooks.get(chosenOne).values())
				webhook.delete().queue();
			try {
				DataIO.saveJson(rifts, "data/miscellaneous/rifts.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the rifts data file.", e);
			}
			event.reply("The rift was successfully closed.");
			Main.getTextChannelById(chosenOne).sendMessageFormat("The rift was closed by **%s**.", Main.str(event.getAuthor())).queue();
		} else event.reply("This channel does not have a rift session running.");
	}

	@Command(category = "Miscellaneous", help = "Remind you something.\n\nAmount has to be a double (e.g. 1.65), unit has to be something along the lines of m, h or d and message can be whatever you want it to be.", name = "remindme", arguments = "<amount> <unit> <message>")
	public static void remindMe(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length >= 3 && Main.isDouble(event.getArgs().split(" ")[0])) {
			double amount = Double.parseDouble(event.getArgs().split(" ")[0]);
			TimeType type = event.getArgs().split(" ")[1].contains("m") ? TimeType.MINUTES : event.getArgs().split(" ")[1].contains("h") ? TimeType.HOURS : TimeType.DAYS;
			amount *= type.toMilliseconds();
			amount += System.currentTimeMillis();
			reminders.add(Main.newHashMap(new String[] {"initiated", "at", "owner", "message"}, new Object[] {System.currentTimeMillis(), amount, event.getAuthor().getId(), Main.join(Main.removeArgs(event.getArgs().split(" "), 0, 0))}));
			try {
				DataIO.saveJson(reminders, "data/miscellaneous/reminders.json");
			} catch (IOException e) {
				throw new CommandException("An unkown error occurred while saving the reminder, you may or may not actually be reminded.", e);
			}
			event.reply("I will remind you that in %s!", Main.formatMillis((long) (amount - System.currentTimeMillis())));
		} else Main.sendCommandHelp(event);
	}

	@SubscribeEvent
	public static void onFullyBooted() {
		new Thread(() -> {
			while (true) {
				for (Map<String, Object> reminder : new ArrayList<>(reminders)) {
					User user = null;
					if (System.currentTimeMillis() >= Main.getLongFromPossibleDouble(reminder.get("at")) && (user = Main.getUserById(reminder.get("owner").toString())) != null) {
						Main.sendPrivateMessage(user, "At %s you've asked me to remind you this: %s.", new Date(Main.getLongFromPossibleDouble(reminder.get("initiated"))), reminder.get("message"));
						reminders.remove(reminder);
					}
				}
				try {
					DataIO.saveJson(reminders, "data/miscellaneous/reminders.json");
				} catch (IOException e) {
					e.printStackTrace();
				}
				Main.sleep(30, TimeUnit.SECONDS);
			}
		}, "RemindMe thread").start();
	}

	@SubscribeEvent
	public static void onMessageReceived(MessageReceivedEvent event) {
		if (event.getGuild() == null) return;
		String serverPrefix = Main.getPrefix(event.getGuild());
		if (event.getMessage().getContent().startsWith(serverPrefix)) if (settings.containsKey(event.getGuild().getId()) && ((Map) settings.get(event.getGuild().getId())).containsKey(event.getMessage().getContent().substring(serverPrefix.length()))) {
			try {
				Map blacklist = DataIO.loadJson("data/mod/blacklist.json", Map.class);
				blacklist = blacklist == null ? new HashMap() : blacklist;
				if (event.getGuild() != null && blacklist.containsKey(event.getGuild().getId()) && ((List) blacklist.get(event.getGuild().getId())).contains(event.getAuthor().getId()))
					return;
				else if (blacklist.containsKey("global") && ((List) blacklist.get("global")).contains(event.getAuthor().getId())) return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			event.getChannel().sendMessage((String) ((Map) settings.get(event.getGuild().getId())).get(event.getMessage().getContent().substring(serverPrefix.length()))).queue();
		}
		if (rifts.containsKey(event.getChannel().getId()) && !event.getMessage().isWebhookMessage()) {
			if (!riftWebhooks.get(rifts.get(event.getChannel().getId())).containsKey(event.getAuthor().getId())) try {
				riftWebhooks.get(rifts.get(event.getChannel().getId())).put(event.getAuthor().getId(), Main.getTextChannelById(rifts.get(event.getChannel().getId())).createWebhook(event.getAuthor().getName()).setAvatar(Icon.from(Main.openStream(new URL(event.getAuthor().getEffectiveAvatarUrl())))).complete());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (riftWebhooks.get(rifts.get(event.getChannel().getId())).get(event.getAuthor().getId()) != null) {
				WebhookClient client = riftWebhooks.get(rifts.get(event.getChannel().getId())).get(event.getAuthor().getId()).newClient().build();
				if (!event.getMessage().getContent().isEmpty()) client.send(event.getMessage().getContent());
				for (Attachment attachment : event.getMessage().getAttachments()) {
					File attachmentFile = new File("data/tmp/attachment_" + Random.randInt() + "_" + attachment.getFileName());
					attachment.download(attachmentFile);
					client.send(attachmentFile, attachment.getFileName());
					attachmentFile.delete();
				}
				if (!event.getMessage().getEmbeds().isEmpty()) client.send(event.getMessage().getEmbeds());
				client.close();
			}
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
