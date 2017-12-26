package com.ptsmods.impulse.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.Config;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;

public class Owner {

	private static final ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("nashorn");

	static {
		try {
			engine.eval("var imports = new JavaImporter(" +
					"java.io," +
					"java.lang," +
					"java.util," +
					"java.awt," +
					"Packages.net.dv8tion.jda.core," +
					"Packages.net.dv8tion.jda.core.entities," +
					"Packages.net.dv8tion.jda.core.entities.impl," +
					"Packages.net.dv8tion.jda.core.managers," +
					"Packages.net.dv8tion.jda.core.managers.impl," +
					"Packages.net.dv8tion.jda.core.utils," +
					"Packages.com.ptsmods.impulse," +
					"Packages.com.ptsmods.impulse.commands," +
					"Packages.com.ptsmods.impulse.miscellaneous," +
					"Packages.com.ptsmods.impulse.utils," +
					"Packages.com.jagrosh.jdautilities);" +
					"var Main = com.ptsmods.impulse.Main;");
		} catch (ScriptException e) {
			throw new RuntimeException("An error occurred while setting up the imports for the evaluator.", e);
		}
	}

	@Command(category = "Owner", help = "Sends a message to every server.", name = "announce", arguments = "<message>", ownerCommand = true)
	public static void announce(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Message status = event.getChannel().sendMessageFormat("Sending announcement to every guild...", Main.getGuilds().size()).complete();
			for (Guild guild : Main.getGuilds())
				guild.getDefaultChannel().sendMessageFormat("%s ~ %s#%s", event.getArgs(), event.getAuthor().getName(), event.getAuthor().getDiscriminator()).queue();
			status.editMessage("Announcement sent.").queue();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Clears the console.", name = "clearconsole", ownerCommand = true)
	public static void clearConsole(CommandEvent event) {
		for (int i : Main.range(20000)) System.out.println();
		event.reply("The console was cleared.");
	}

	@Command(category = "Owner", help = "Contact the owner.", name = "contact", cooldown = 60)
	public static void contact(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Main.sendPrivateMessage(Main.getOwner(), String.format("**%s#%s** (%s) has sent you a message from **%s** (%s):\n\n",
					event.getAuthor().getName(),
					event.getAuthor().getDiscriminator(),
					event.getAuthor().getId(),
					event.getGuild().getName(),
					event.getGuild().getId()) + event.getArgs());
			event.reply("Successfully sent your message to my owner!");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Evaluate code.", name = "debug", ownerCommand = true, hidden = true)
	public static void debug(CommandEvent event) {
		try {
			engine.put("event", event);
			engine.put("message", event.getMessage());
			engine.put("channel", event.getTextChannel());
			engine.put("args", event.getArgs());
			engine.put("jda", event.getJDA());
			engine.put("author", event.getAuthor());
			if (event.isFromType(ChannelType.TEXT)) {
				engine.put("guild", event.getGuild());
				engine.put("member", event.getMember());
			}
			Object out;
			try {
				out = engine.eval(String.format("(function() {with (imports) {return %s;}})();", event.getArgs()));
			} catch (Throwable e) {
				out = engine.eval(String.format("(function() {with (imports) {%s;}})();", event.getArgs()));
			}
			out = out == null ? "null" : out.toString();
			List<String> messages = new ArrayList<>();
			while (out.toString().length() > 1990) {
				messages.add(out.toString().substring(0, 1990).trim());
				out = out.toString().substring(1990, out.toString().length());
			}
			messages.add(out.toString());
			event.reply("Input:```javascript\n" + event.getArgs() + "```\nOutput:```javascript\n" + messages.get(0) + "```");
			messages.remove(0);
			for (String message : messages) event.reply("```javascript\n" + message + "```");
		} catch (Throwable e1) {
			event.reply("```javascript\n" + e1.getMessage() + "```");
		}
	}

	@Command(category = "Owner", help = "Shows you all the servers the bot is in.", name = "servers", ownerCommand = true, hidden = true)
	public static void servers(CommandEvent event) {
		Map<Integer, String> pages = new HashMap();
		int counter = 0;
		int counter1 = 0;
		String page = "```css\n[Page 1/%s]\n\t";
		int id = 0;
		for (Guild guild : Main.getGuilds()) {
			page += guild.getName() + "\n\t";
			counter += 1;
			counter1 += 1;
			if (counter == 10 || counter1 == Main.getGuilds().size()) {
				counter = 0;
				id += 1;
				pages.put(id, page.trim() + "```");
				page = "```css\n[Page " + (id+1) + "/%s]\n\t";
			}
		}
		int pageN = 1;
		if (event.getArgs().length() != 0 && Main.isInteger(event.getArgs().split(" ")[0])) pageN = Integer.parseInt(event.getArgs().split(" ")[0]);
		if (pageN >= pages.size()+1) event.reply("The maximum page is " + (pages.size()-1) + ".");
		else event.reply(pages.get(pageN), pages.size());
	}

	@Command(category = "Owner", help = "Manage settings.", name = "set", ownerCommand = true)
	public static void set(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the bot's status.", name = "status", parent = "com.ptsmods.impulse.commands.Owner.set", arguments = "<status>", ownerCommand = true)
	public static void setStatus(CommandEvent event) {
		if (!event.getArgs().isEmpty() &&
				event.getArgs().split(" ")[0].toUpperCase().equals("OFFLINE") ||
				event.getArgs().split(" ")[0].toUpperCase().equals("INVISIBLE") ||
				event.getArgs().split(" ")[0].toUpperCase().equals("DND") ||
				event.getArgs().split(" ")[0].toUpperCase().equals("DO_NOT_DISTURB") ||
				event.getArgs().split(" ")[0].toUpperCase().equals("IDLE") ||
				event.getArgs().split(" ")[0].toUpperCase().equals("ONLINE")) {
			Main.setOnlineStatus(Main.getStatusFromString(event.getArgs().split(" ")[0]));
			event.reply("Successfully set the online status to " + Main.getStatusFromString(event.getArgs().split(" ")[0]).name() + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the bot's game.", name = "game", parent = "com.ptsmods.impulse.commands.Owner.set", arguments = "<game>", ownerCommand = true)
	public static void setGame(CommandEvent event) {
		Main.setGame(event.getArgs());
		event.reply(event.getArgs().isEmpty() ? "The game has been reset." : "Successfully set the game to " + event.getArgs() + ".");
	}

	@Subcommand(help = "Set the bot's avatar.", name = "avatar", parent = "com.ptsmods.impulse.commands.Owner.set", arguments = "<avatar>", ownerCommand = true)
	public static void setAvatar(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			URL url = null;
			try {
				url = new URL(event.getArgs());
			} catch (MalformedURLException e) {
				event.reply("The given URL was malformed.");
				return;
			}
			try {
				Main.setAvatar(Icon.from(url.openStream()));
			} catch (IOException e) {
				event.reply(e.getMessage());
				return;
			}
			event.reply("Successfully set the avatar.");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the bot's nickname.", name = "nickname", parent = "com.ptsmods.impulse.commands.Owner.set", arguments = "<nickname>", userPermissions = {Permission.NICKNAME_MANAGE}, botPermissions = {Permission.NICKNAME_CHANGE})
	public static void setNickname(CommandEvent event) {
		event.getGuild().getController().setNickname(event.getGuild().getSelfMember(), event.getArgs()).queue();
		event.reply(event.getArgs().isEmpty() ? "Successfully reset my nickname." : "Successfully set my nickname to " + event.getArgs() + ".");
	}

	@Subcommand(help = "Set the bot's name, the given name should be bigger than 2 characters and smaller than 32 characters.", name = "name", parent = "com.ptsmods.impulse.commands.Owner.set", ownerCommand = true)
	public static void setName(CommandEvent event) {
		if (event.getArgs().length() >= 2 && event.getArgs().length() <= 32) {
			event.getJDA().getSelfUser().getManager().setName(event.getArgs()).queue();
			event.reply("Successfully set my name to " + event.getArgs() + ".");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Shuts the bot down.", name = "shutdown", ownerCommand = true)
	public static void shutdown(CommandEvent event) {
		event.getChannel().sendMessage("Kk, shutting down.").complete();
		Main.print(LogType.WARN, event.getAuthor().getName() + " requested the bot to be shut down.");
		Main.shutdown(0);
	}

	@Command(category = "Owner", help = "Toggles developer mode.", name = "toggledevmode", ownerCommand = true, hidden = true)
	public static void toggleDevMode(CommandEvent event) {
		Main.devMode(!Main.devMode());
		Main.setGame(Game.of(Main.devMode() ? "DEVELOPER MODE" : "try " + Config.get("prefix") + "help!"));
		event.reply("Developer mode has been " + (Main.devMode() ? "enabled" : "disabled") + ", " + (Main.devMode() ? "errors will no longer be sent privately and only you will now be able to use commands." :
				"errors will once again be sent privately and everyone will be able to use commands again."));
	}

	@Command(category = "Owner", help = "Shows you all the permissions this bot has in this channel.", name = "permissionshere", ownerCommand = true, guildOnly = true, hidden = true)
	public static void permissionsHere(CommandEvent event) {
		List<String> permissions = new ArrayList();
		for (Permission perm : event.getSelfMember().getPermissions(event.getTextChannel()))
			permissions.add(perm.getName());
		event.reply("I have the following permissions in this channel: **" + Main.joinCustomChar("**, **", permissions) + "**.");
	}

	@Command(category = "Owner", help = "Execute a command on the command line.", name = "terminal", ownerCommand = true, hidden = true)
	public static void terminal(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			Message msg = event.getTextChannel().sendMessage("Executing the given command on the command line...").complete();
			if (Main.isWindows()) event.setArgs("cmd /c " + event.getArgs());
			Process process;
			try {
				process = Runtime.getRuntime().exec(event.getArgs());
			} catch (IOException e) {
				throw new CommandException("An error occurred while parsing the given command.", e);
			}
			StringBuilder output = new StringBuilder();
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			try {
				while ((line = input.readLine()) != null)
					output.append(line);
			} catch (IOException e) {
				throw new CommandException("An error occurred while parsing the given command.", e);
			}
			msg.editMessage("Input:\n```\n" + event.getOriginalArgs() + "```\nOutput:\n```\n" + output + "```").queue();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Calculates the average amount of messages the bot receives per second.", name = "averagemessages", ownerCommand = true, hidden = true, arguments = "<seconds>")
	public static void averageMessages(CommandEvent event) {
		if (!event.argsEmpty() && Main.isFloat(event.getArgs())) {
			Message status = event.getTextChannel().sendMessage("Retrieving messages, please wait...").complete();
			long msgs = Main.getReceivedMessages().size();
			long millis = System.currentTimeMillis();
			try {
				Thread.sleep((long) Float.parseFloat(event.getArgs()) * 1000L);
			} catch (InterruptedException e) {}
			status.editMessageFormat("Done! Received **%s** messages in **%s** seconds with an average of **%s** messages per second.",
					Main.getReceivedMessages().size()-msgs,
					(System.currentTimeMillis()-millis)/1000,
					(float) (Main.getReceivedMessages().size()-msgs)/(float) ((System.currentTimeMillis()-millis)/1000)).queue();
		} else Main.sendCommandHelp(event);
	}

}
