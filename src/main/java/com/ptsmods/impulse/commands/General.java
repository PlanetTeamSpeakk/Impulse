package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class General {

	private static String[] answers = new String[] {
			"It is certain", "It is decidedly so", "Without a doubt", "Yes definitely", "You may rely on it",						  // positive
			"As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes",					  						  // positive
			"Reply hazy try again", "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrate and ask again",  // neutral
			"Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"};					  // negative

	@Command(category = "General", help = "Calculates a math equation so you don't have to.", name = "calc", arguments = "<equation>")
	public static void calc(CommandEvent event) {
		if (event.getArgs().length() != 0)
			try {
				event.reply("`" + event.getArgs() + "` = `" + Main.eval(event.getArgs()) + "`");
			} catch (RuntimeException e) {
				event.reply(e.getMessage());
			}
		else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Gives you the avatar of a user.", name = "avatar", arguments = "<user>")
	public static void avatar(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			event.reply(user.getAvatarUrl() == null ? user.getDefaultAvatarUrl() : user.getAvatarUrl());
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Answers the hardest questions life can give you.", name = "8ball", arguments = "<question>")
	public static void eightBall(CommandEvent event) {
		if (!event.getArgs().endsWith("?") || event.getArgs().length() == 0)
			event.reply("That does not look like a question.");
		else event.reply(answers[Random.randInt(answers.length)] + ".");
	}

	@Command(category = "General", help = "Flips text.", name = "flip", arguments = "<text>")
	public static void flip(CommandEvent event) {
		if (event.getArgs().isEmpty()) Main.sendCommandHelp(event);
		else event.reply(Main.flipString(event.getArgs()));
	}

	@Command(category = "General", help = "How does this work?", name = "help", arguments = "[command or category]")
	public static void help(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			List<String> msgs = new ArrayList<>();
			String msg = "**" + event.getJDA().getSelfUser().getName() + "** commands:\n\n";
			for (String category : Main.sort(Main.getCategories())) {
				msg += "**" + category + "**\n\t";
				for (String cmdName : Main.sort(Main.getCommandNames())) {
					Command cmd = Main.getCommandByName(cmdName).getAnnotation(Command.class);
					if (cmd == null) continue;
					if (!event.isOwner() && !event.isCoOwner())
						if (cmd.category() == null ||
						cmd.hidden() ||
						cmd.ownerCommand() ||
						event.getMember() != null && !event.getMember().hasPermission(cmd.userPermissions()))
							continue;
					if (cmd.category() != null && cmd.category().equals(category))
						msg += "**" + cmd.name() + "**" + (cmd.help() != null && !cmd.help().isEmpty() && !cmd.help().toLowerCase().equals("no help available") ? ": " + cmd.help().split("\n")[0] : "") + "\n\t";
					if (msg.length() >= 1750) {
						msgs.add(msg);
						msg = "";
					}
				}
				msg = msg.trim() + "\n";
			}
			if (!msg.trim().isEmpty()) msgs.add(msg);
			for (String msg1 : msgs)
				Main.sendPrivateMessage(event.getAuthor(), msg1.trim());
			event.getMessage().addReaction("\uD83D\uDC4D").queue();
		} else {
			for (Method cmdM : Main.getCommands()) {
				Command cmd = cmdM.getAnnotation(Command.class);
				String[] args = event.getArgs().split(" ");
				if (cmd.name().equals(args[0])) {
					args = Main.removeArg(args, 0);
					while (args.length > 0) {
						for (Method scmd : Main.getSubcommands(cmdM))
							if (scmd.getAnnotation(Subcommand.class).name().equals(args[0])) {
								cmdM = scmd;
								break;
							}
						args = Main.removeArg(args, 0);
					}
					Main.sendCommandHelp(event, cmdM);
					return;
				}
			}
			for (String category : Main.getCategories())
				if (event.getArgs().split(" ")[0].equals(category)) {
					List<String> msgs = new ArrayList<>();
					String msg = "";
					msg += "**" + category + "**\n\t";
					for (String cmdName : Main.sort(Main.getCommandNames())) {
						Command cmd = Main.getCommandByName(cmdName).getAnnotation(Command.class);
						if (cmd == null || // very unlikely
								cmd.category() == null ||
								cmd.hidden() ||
								cmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) ||
								event.getMember() != null && !event.getMember().hasPermission(cmd.userPermissions()))
							continue;
						if (cmd.category().equals(category))
							msg += "**" + cmd.name() + "**" + (cmd.help() != null && !cmd.help().isEmpty() && !cmd.help().toLowerCase().equals("no help available") ? ": " + cmd.help().split("\n")[0] : "") + "\n\t";
						if (msg.length() >= 1750) {
							msgs.add(msg);
							msg = "";
						}
					}
					msg = msg.trim() + "\n";
					if (!msg.trim().isEmpty()) msgs.add(msg);
					for (String msg1 : msgs)
						event.reply(msg1.trim());
					return;
				}
			event.reply("That command or category could not be found.");
		}
	}

	@Command(category = "General", help = "Some information about the bot.", name = "info")
	public static void info(CommandEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(event.getJDA().getSelfUser().getName());
		embed.setColor(new Color(Random.randInt(256*256*256)));
		embed.setThumbnail("https://cdn.impulsebot.com/3mR7g3RC0O.png");
		embed.setDescription("This bot is an instance of Impulse, a Discord Bot written in Java by PlanetTeamSpeak using JDA and JDA Utilities. "
				+ "If you want your own bot with all these commands, make sure to check out [the GitHub page](https://github.com/PlanetTeamSpeakk/Impulse \"Yes, it's open source.\") or [the Discord Server](https://discord.gg/tzsmCyk \"Yes, I like advertising.\").");
		event.reply(embed.build());
	}

	@Command(category = "General", help = "Gives you an invite link for this bot.", name = "invite")
	public static void invite(CommandEvent event) {
		event.reply("If you want to invite me to your server, click this link: <" + event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ">.");
	}

	@Command(category = "General", help = "Tells you the latency of this bot with the Discord servers.", name = "ping")
	public static void ping(CommandEvent event) {
		long nanos = System.nanoTime();
		event.getChannel().sendTyping().complete();
		long replyTime = new Date().getTime()/1000-event.getMessage().getCreationTime().toEpochSecond();
		replyTime = replyTime < 0 ? 0 : replyTime;
		event.reply("Ping: **" + (System.nanoTime()-nanos)/1000000F + " milliseconds**, took **" + replyTime + " second" + (replyTime != 1 ? "s" : "") + "** to reply.");
	}

	@Command(category = "General", help = "Generates a QR code from the given text.", name = "qrcode", arguments = "<text>")
	public static void qrCode(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Map<String, String> data;
			try {
				int rng = Random.randInt(10000);
				data = Downloader.downloadFile("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + Main.percentEncode(event.getArgs()), "data/general/" + rng + ".png");
				event.getChannel().sendFile(new File(data.get("fileLocation")), new MessageBuilder().append("Here you go:").build()).complete();
				new File(data.get("fileLocation")).delete();
			} catch (IOException e) {
				event.reply("An unknown error occured while creating the QR code, please try again.");
				e.printStackTrace();
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Rolls a number for you.", name = "roll", arguments = "[number]")
	public static void roll(CommandEvent event) {
		int max = 100;
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs())) max = Integer.parseInt(event.getArgs());
		if (max < 1) max = 1;
		event.reply(String.format("You rolled **%s**.", Random.randInt(max)));
	}

	@Command(category = "General", help = "Spin the revolver's chamber and then *PANG*. \nThe given bet will be removed from your balance if you lose or doubled if you win, if the given bet is 0 or not given there's nothing to lose.", name = "russianroulette", aliases = {"rr"}, cooldown = 30)
	public static void russianRoulette(CommandEvent event) {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0]) && Integer.parseInt(event.getArgs().split(" ")[0]) != 0) {
			int bet = Integer.parseInt(event.getArgs().split(" ")[0]);
			if (!Economy.hasAccount(event.getMember()))
				event.replyFormatted("You cannot bet without having a bank account, you can make one with %sbank register.", Main.getPrefix(event.getGuild()));
			else if (!Economy.hasEnoughBalance(event.getMember(), bet)) event.reply("You do not have enough balance to bid that high.");
			else {
				int bullets = Random.randInt(1, 5);
				if (event.getArgs().split(" ").length > 1 && Main.isInteger(event.getArgs().split(" ")[1]) && Integer.parseInt(event.getArgs().split(" ")[1]) > 0 && Integer.parseInt(event.getArgs().split(" ")[1]) < 6) bullets = Integer.parseInt(event.getArgs().split(" ")[1]);
				boolean shot = play(event, bullets, event.getChannel().sendMessageFormat("You've made a bet for %s credits that you'll survive with %s bullets in the barrel. You load up %s bullets and start spinning the barrel...", bet, bullets, bullets).complete());
				int oldBalance = Economy.getBalance(event.getMember());
				if (shot)
					Economy.removeBalance(event.getMember(), bet);
				else
					Economy.addBalance(event.getMember(), bet);
				event.replyFormatted("%s credits have been %s %s your bank account, old balance: **%s** credits, new balance: **%s** credits.", bet, shot ? "removed" : "added", shot ? "from" : "to", oldBalance, Economy.getBalance(event.getMember()));
			}
		} else {
			int bullets = Random.randInt(1, 5);
			if (event.getArgs().split(" ").length > 1 && Main.isInteger(event.getArgs().split(" ")[1]) && Integer.parseInt(event.getArgs().split(" ")[1]) > 0 && Integer.parseInt(event.getArgs().split(" ")[1]) < 6) bullets = Integer.parseInt(event.getArgs().split(" ")[1]);
			play(event, bullets, null);
		}
	}

	private static boolean play(CommandEvent event, int bullets, Message message) {
		if (message == null) message = event.getChannel().sendMessage(new MessageBuilder().append("You load up ").append(bullets).append(" bullets and start spinning the barrel...").build()).complete();
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {}
		message.editMessage("You put the gun against your head and work up the courage to pull the trigger.").queue();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
		boolean shot = false;
		Boolean[] choices = new Boolean[6];
		for (int x : Main.range(bullets))
			choices[x] = true;
		for (int x = 0; x < choices.length; x++)
			if (choices[x] == null) choices[x] = false;
		shot = Random.choice(choices);
		if (shot) message.editMessage("*PANG* sadly, one of the bullets hit you and now you're dead.").queue();
		else message.editMessage("*click* nothing happened, you've survived!").queue();
		return shot;
	}

	@Command(category = "General", help = "Let's the bot say something, this does filter out @\u200Beveryone and @\u200Bhere.", name = "say", arguments = "<text>")
	public static void say(CommandEvent event) {
		if (event.getArgs().length() != 0) event.reply(event.getArgs().replaceAll("@everyone", "@\u200Beveryone").replaceAll("@here", "@\u200Bhere"));
		else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Tells you in how many servers the bot is in.", name = "servercount")
	public static void serverCount(CommandEvent event) {
		if (Main.getShards().size() != 1)
			event.replyFormatted("This shard is currently in **%s** servers and can see **%s** users.\n"
					+ "This bot is currently in **%s** servers and can see **%s** users.",
					event.getJDA().getGuilds().size(), event.getJDA().getUsers().size(),
					Main.getGuilds().size(), Main.getUsers().size());
		else
			event.replyFormatted("This bot is currently in **%s** servers and can see **%s** users.",
					event.getJDA().getGuilds().size(), event.getJDA().getUsers().size());
	}

	@Command(category = "General", help = "Tells you which shard this server is on.", name = "shard")
	public static void shard(CommandEvent event) {
		event.reply("Shard " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal() + ".");
	}

	@Command(category = "General", help = "Shortens a URL.", name = "shorten", cooldown = 30, arguments = "<url>")
	public static void shorten(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Message msg = event.getChannel().sendMessage("Shortening your URL, please wait...").complete();
			Response<ShortenResponse> resp = new BitlyClient("dd800abec74d5b12906b754c630cdf1451aea9e0").shorten().setLongUrl(event.getArgs()).call();
			Main.print(LogType.INFO, resp.status_txt, resp.status_code, resp == null, msg == null);
			if (resp.status_txt.equals("INVALID_URI")) msg.editMessage("The given URL was invalid, according to bit.ly.").complete();
			else msg.editMessage("Here you go: <" + resp.data.url + ">").complete();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Tells you how long I've been up for.", name = "uptime")
	public static void uptime(CommandEvent event) {
		event.reply("I have been up for " + Main.formatMillis(new Date().getTime()-Main.started.getTime()) + ".");
	}

	@Command(category = "General", help = "Look something up on Urban Dictionary.", name = "urban", arguments = "<query>", cooldown = 30)
	public static void urban(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			event.getChannel().sendTyping().complete();
			String searchTerm = event.getArgs();
			String data;
			try {
				data = Main.getHTML("http://api.urbandictionary.com/v0/define?term=" + searchTerm.replaceAll(" ", "+"));
			} catch (IOException e) {
				event.reply("An unknown error occured while trying to get the data, please try again.");
				return;
			}
			if (data.contains("\"result_type\":\"no_results\""))
				event.replyFormatted("No results found for the search term **%s**.", searchTerm);
			else {
				Gson gson = new Gson();
				Map dataMap = gson.fromJson(data, Map.class);
				List definitions = (List) dataMap.get("list");
				Map definitionsMap = (Map) definitions.get(0);
				String definition = Main.getCleanString((String) definitionsMap.get("definition")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
				String example = Main.getCleanString((String) definitionsMap.get("example")).replaceAll("\\_", "\\\\_").replaceAll("\\*", "\\\\*").replaceAll("\\~", "\\\\~");
				Long thumbsUp = ((Double) definitionsMap.get("thumbs_up")).longValue();
				Long thumbsDown = ((Double) definitionsMap.get("thumbs_down")).longValue();
				String result = "Definition for **" + searchTerm + "**:\n" + definition + (!example.equals("") ? "\n\nExample:\n" + example : "") + "\n\nThumbs up: **" + thumbsUp + "**\nThumbs down: **" + thumbsDown + "**";
				event.reply(result);
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Tells you how much RAM is allocated, how much RAM is used and the amount of processors available.", name = "usage")
	public static void usage(CommandEvent event) {
		event.replyFormatted("RAM used: **%s**, RAM allocated: **%s**, cores: **%s**.",
				Main.formatFileSize(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()),
				Main.formatFileSize(Runtime.getRuntime().totalMemory()),
				Runtime.getRuntime().availableProcessors());
	}

}
