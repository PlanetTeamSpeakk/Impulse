package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ptsmods.impulse.Main.TimeType;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.Cleverbot;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.Downloader.DownloadResult;
import com.ptsmods.impulse.utils.MathHelper;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class General {

	private static final Map<Integer, String>	games	= new HashMap();
	private static final String[]				answers	= new String[] {"It is certain", "It is decidedly so", "Without a doubt", "Yes definitely", "You may rely on it",				// positive
			"As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes",																								// positive
			"Reply hazy try again", "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrate and ask again",													// neutral
			"Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"};																		// negative
	static {
		Main.apiKeys.put("steam", "4097EECAE0C75569D595A25BEB4BCB3C");
		Main.apiKeys.put("wargaming", "a223cd2a48a13e5b2e484f4a9ec80d33");
		Main.apiKeys.put("geocoding", "AIzaSyCXkFcW0v8XJWGK2Im2_fApsbh3I8OGCDI");
		Main.apiKeys.put("timezone", "AIzaSyCXkFcW0v8XJWGK2Im2_fApsbh3I8OGCDI");
		List<Map> data;
		try {
			data = (List<Map>) ((Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamApps/GetAppList/v0002/"), Map.class).get("applist")).get("apps");
		} catch (JsonSyntaxException | IOException e) {
			throw new RuntimeException("An unknown error occurred while getting the app list.");
		}
		for (Map game : data)
			games.put(Integer.parseInt(game.get("appid").toString().split("\\.")[0]), game.get("name").toString());
	}

	@Command(category = "General", help = "Calculates a math equation so you don't have to.", name = "calc", arguments = "<equation>")
	public static void calc(CommandEvent event) {
		if (event.getArgs().length() != 0)
			try {
				event.reply("`%s` = `%s`", event.getArgs(), new DecimalFormat("#").format(MathHelper.eval(event.getArgs())));
			} catch (RuntimeException e) {
				event.reply(e.getMessage());
			}
		else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Gives you the avatar of a user.", name = "avatar", arguments = "<user>")
	public static void avatar(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null)
				event.reply("The given user could not be found.");
			else event.reply(user.getEffectiveAvatarUrl());
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Answers the hardest questions life can give you.", name = "8ball", arguments = "<question>")
	public static void eightBall(CommandEvent event) {
		if (!event.getArgs().endsWith("?") || event.argsEmpty() || !Main.startsWith(event.getArgs().toLowerCase(), new String[] {"are", "may", "should", "is", "will", "have", "shall", "could", "can", "might", "did", "would", "am"}))
			event.reply("That does not look like a closed question. (Closed questions can only be answered with yes or no and end with a question mark.)");
		else event.reply(Random.INSTANCE.choice(answers) + ".");
	}

	@Command(category = "General", help = "Flips text.", name = "flip", arguments = "<text>")
	public static void flip(CommandEvent event) {
		if (event.getArgs().isEmpty())
			Main.sendCommandHelp(event);
		else event.reply(Main.flipString(event.getArgs()));
	}

	@Command(category = "General", help = "How does this work?", name = "help", arguments = "[command or category]", sendTyping = false)
	public static void help(CommandEvent event) {
		if (event.getArgs().isEmpty()) {
			List<String> msgs = new ArrayList<>();
			String msg = "**" + event.getJDA().getSelfUser().getName() + "** commands:\n\n";
			for (String category : Main.sort(Main.getCategories())) {
				msg += "**" + category + "**\n\t";
				for (String cmdName : Main.sort(Main.getCommandNames())) {
					Command cmd = Main.getCommandByName(cmdName).getAnnotation(Command.class);
					if (cmd == null) continue;
					if (!event.isOwner() && !event.isCoOwner()) if (cmd.category() == null || cmd.hidden() || cmd.ownerCommand() || event.getMember() != null && !event.getMember().hasPermission(cmd.userPermissions())) continue;
					if (cmd.category() != null && cmd.category().equals(category)) msg += "**" + cmd.name() + "**" + (cmd.help() != null && !cmd.help().isEmpty() && !cmd.help().toLowerCase().equals("no help available") ? ": " + cmd.help().split("\n")[0] : "") + "\n\t";
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
			try {
				event.getMessage().addReaction("\uD83D\uDC4D").queue();
			} catch (Exception e) {
				event.reply("Help has been sent in your DMs.");
			}
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
								cmd.category() == null || cmd.hidden() || cmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) || event.getMember() != null && !event.getMember().hasPermission(cmd.userPermissions()))
							continue;
						if (cmd.category().equals(category)) msg += "**" + cmd.name() + "**" + (cmd.help() != null && !cmd.help().isEmpty() && !cmd.help().toLowerCase().equals("no help available") ? ": " + cmd.help().split("\n")[0] : "") + "\n\t";
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
		embed.setColor(new Color(Random.INSTANCE.randInt(256 * 256 * 256)));
		embed.setThumbnail("https://cdn.impulsebot.com/3mR7g3RC0O.png");
		embed.setDescription("This bot is an instance of Impulse, a Discord Bot written in Java by PlanetTeamSpeak using JDA. " + "If you want your own bot with all these commands, make sure to check out [the GitHub page](https://github.com/PlanetTeamSpeakk/Impulse \"Yes, it's open source.\") " + "and don't forget to join [the Discord Server](https://discord.gg/tzsmCyk \"Yes, I like advertising.\")" + ", check out [the website](https://impulsebot.com \"Pls, just do it. ;-;\"), " + "and send me all your cash on [my Patreon page](https://patreon.com/PlanetTeamSpeak \"Pls just give me your money.\").");
		embed.setFooter("PS, the color used is #" + Main.colourToHex(embed.build().getColor()) + ".", null);
		event.reply(embed.build());
	}

	@Command(category = "General", help = "Gives you an invite link for this bot.", name = "invite")
	public static void invite(CommandEvent event) {
		event.reply("If you want to invite me to your server, click this link: <" + event.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ">.");
	}

	@Command(category = "General", help = "Tells you the latency of this bot with the Discord servers.", name = "ping", sendTyping = false)
	public static void ping(CommandEvent event) {
		long nanos = System.nanoTime();
		event.getChannel().sendTyping().complete();
		long replyTime = Main.getTime(TimeType.MILLISECONDS) / 1000 - event.getMessage().getCreationTime().toEpochSecond();
		replyTime = replyTime < 0 ? 0 : replyTime;
		event.reply("Ping: **" + (System.nanoTime() - nanos) / 1000000F + " milliseconds**, took **" + replyTime + " second" + (replyTime != 1 ? "s" : "") + "** to reply.");
	}

	@Command(category = "General", help = "Generates a QR code from the given text.", name = "qrcode", arguments = "<text>")
	public static void qrCode(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			DownloadResult result;
			try {
				int rng = Random.INSTANCE.randInt(1000, 9999);
				result = Downloader.downloadFile("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + Main.percentEncode(event.getArgs()), "data/general/" + rng + ".png");
				event.getChannel().sendFile(new File(result.getFileLocation()), new MessageBuilder().append("Here you go:").build()).complete();
				new File(result.getFileLocation()).delete();
			} catch (IOException e) {
				event.reply("An unknown error occurred while creating the QR code, please try again.");
				e.printStackTrace();
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Rolls a number for you.", name = "roll", arguments = "[number]")
	public static void roll(CommandEvent event) {
		int max = 100;
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs())) max = Integer.parseInt(event.getArgs());
		if (max < 1) max = 1;
		event.reply(String.format("You rolled **%s**.", Random.INSTANCE.randInt(max)));
	}

	@Command(category = "General", help = "Let's the bot say something, this does filter out @\u200Beveryone and @\u200Bhere.", name = "say", arguments = "<text>")
	public static void say(CommandEvent event) {
		if (event.getArgs().length() != 0)
			event.reply(event.getArgs().replaceAll("@everyone", "@\u200Beveryone").replaceAll("@here", "@\u200Bhere"));
		else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Tells you in how many servers the bot is in.", name = "servercount")
	public static void serverCount(CommandEvent event) {
		if (Main.getShards().size() != 1)
			event.reply("This shard is currently in **%s** servers and can see **%s** users.\n" + "This bot is currently in **%s** servers and can see **%s** users.", event.getJDA().getGuilds().size(), event.getJDA().getUsers().size(), Main.getGuilds().size(), Main.getUsers().size());
		else event.reply("This bot is currently in **%s** servers and can see **%s** users.", event.getJDA().getGuilds().size(), event.getJDA().getUsers().size());
	}

	@Command(category = "General", help = "Tells you which shard this server is on.", name = "shard")
	public static void shard(CommandEvent event) {
		event.reply("Shard " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal() + ".");
	}

	@Command(category = "General", help = "Shortens a URL.", name = "shorten", cooldown = 30, arguments = "<url>")
	public static void shorten(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Message msg = event.getChannel().sendMessage("Shortening your URL, please wait...").complete();
			Response<ShortenResponse> resp = new BitlyClient(Main.apiKeys.get("bitly")).shorten().setLongUrl(event.getArgs()).call();
			if (resp.status_txt.equals("INVALID_URI"))
				msg.editMessage("The given URL was invalid, according to bit.ly.").complete();
			else msg.editMessage("Here you go: <" + resp.data.url + ">").complete();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Tells you how long I've been up for.", name = "uptime")
	public static void uptime(CommandEvent event) {
		event.reply("I have been up for " + Main.formatMillis(new Date().getTime() - Main.started.getTime()) + ".");
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
				event.reply("An unknown error occurred while trying to get the data, please try again.");
				return;
			}
			if (data.contains("\"result_type\":\"no_results\""))
				event.reply("No results found for the search term **%s**.", searchTerm);
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

	// TODO: make this sysinfo and make is show more things.
	@Command(category = "General", help = "Tells you how much RAM is allocated, how much RAM is used and the amount of processors available.", name = "usage")
	public static void usage(CommandEvent event) {
		event.reply("RAM used: **%s**, RAM allocated: **%s**, cores: **%s**.", Main.formatFileSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()), Main.formatFileSize(Runtime.getRuntime().totalMemory()), Runtime.getRuntime().availableProcessors());
	}

	@Command(category = "General", help = "Tells you who's boss!", name = "botowner")
	public static void botOwner(CommandEvent event) {
		event.reply("My owner is %s.", Main.getOwner().getAsMention());
	}

	@Command(category = "General", help = "Steam general information.", name = "steam")
	public static void steam(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Gives you a lot of information about a user.", name = "getuserinfo", parent = "com.ptsmods.impulse.commands.General.steam", cooldown = 30, arguments = "<user>")
	public static void steamGetUserInfo(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			if (event.getArgs().contains(" "))
				event.reply("Due to a bug in the Steam API, usernames cannot have spaces in them.");
			else {
				String userid = "";
				if (Main.isDouble(event.getArgs()))
					userid = event.getArgs();
				else {
					Map data;
					try {
						data = (Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + Main.apiKeys.get("steam") + "&vanityurl=" + event.getArgs()), Map.class).get("response");
					} catch (IOException e) {
						throw new CommandException("An unknown error occurred while getting the Steam 64 ID from the username.", e);
					}
					if ((double) data.get("success") == 42D) {
						event.reply("That's not a valid username.");
						return;
					} else userid = (String) data.get("steamid");
				}
				Map data;
				List data1;
				try {
					data1 = (List) ((Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + Main.apiKeys.get("steam") + "&steamids=" + userid), Map.class).get("response")).get("players");
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting the player summaries.", e);
				}
				if (data1.isEmpty())
					event.reply("That's not a valid ID.");
				else {
					data = (Map) data1.get(0);
					try {
						data.put("games", ((Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=" + Main.apiKeys.get("steam") + "&steamid=" + userid), Map.class).get("response")).get("games"));
					} catch (JsonSyntaxException | IOException e) {
						throw new CommandException("An unknown error occurred while getting the player's owned games.", e);
					}
					List ownedGames = new ArrayList();
					for (Map game : (List<Map>) data.get("games"))
						ownedGames.add(String.format("%s (%s)", games.get(Integer.parseInt(game.get("appid").toString().split("\\.")[0])), game.get("appid").toString().split("\\.")[0]));
					event.reply(String.format("```fix\nUsername: %s\nSteam ID: %s\nProfile URL: %s\nAvatar: %s\nGame count: %s\nGames owned: \n\t%s```", data.get("personaname"), data.get("steamid"), data.get("profileurl"), data.get("avatarfull"), ((List) data.get("games")).size(), Main.joinCustomChar("\n\t", ownedGames)));
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "See what game or app is attached to this mysterious id :O, or just use a name, whatever you like.", name = "applookup", parent = "com.ptsmods.impulse.commands.General.steam", cooldown = 30, arguments = "<game>")
	public static void steamAppLookup(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			Map data;
			if (Main.isInteger(event.getArgs())) {
				try {
					data = new Gson().fromJson(Main.getHTML("https://steamspy.com/api.php?request=appdetails&appid=" + event.getArgs()), Map.class);
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting the data for that game.", e);
				}
				if (data.get("name") == null) {
					event.reply("A game with the given ID could not be found.");
					return;
				}
			} else {
				int id = -1;
				for (Integer game : games.keySet())
					if (games.get(game).equalsIgnoreCase(event.getArgs())) {
						id = game;
						break;
					}
				if (id == -1) {
					event.reply("A game with the given name could not be found.");
					return;
				} else {
					try {
						data = new Gson().fromJson(Main.getHTML("https://steamspy.com/api.php?request=appdetails&appid=" + id), Map.class);
					} catch (JsonSyntaxException | IOException e) {
						throw new CommandException("An unknown error occurred while getting the data for that game.", e);
					}
					if (data.get("name") == null) {
						event.reply("The given game is too new, no data could be found.");
						return;
					}
				}
			}
			event.reply("```fix\nID: %s\nName: %s\nDeveloper: %s\nPublisher: %s\nDownloads: %.0f\nURL: https://store.steampowered.com/app/%s\nPrice: $%s.%s```", data.get("appid").toString().split("\\.")[0], data.get("name"), data.get("developer"), data.get("publisher"), data.get("owners"), data.get("appid").toString().split("\\.")[0], (int) Double.parseDouble(data.get("price").toString()) / 100, (int) Double.parseDouble(data.get("price").toString()) % 100);
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Shows you the top 100 games played by players since march 2009.", name = "top100forever", parent = "com.ptsmods.impulse.commands.General.steam", cooldown = 30)
	public static void steamTop100Forever(CommandEvent event) throws CommandException {
		Map data;
		try {
			data = new Gson().fromJson(Main.getHTML("https://steamspy.com/api.php?request=top100forever"), Map.class);
		} catch (JsonSyntaxException | IOException e) {
			throw new CommandException("An unknown error occurred while getting the top 100 list.", e);
		}
		String msg = "```fix\n";
		int counter = 0;
		for (Map game : ((Map<String, Map>) data).values()) {
			counter += 1;
			msg += counter + ". " + game.get("name") + "\n";
			if (msg.length() > 1900) {
				event.reply(msg + "```");
				msg = "```fix\n";
			}
		}
		event.reply(msg + "```");
	}

	@Subcommand(help = "Shows you the top 100 games played by players since the last 2 weeks.", name = "top100in2weeks", parent = "com.ptsmods.impulse.commands.General.steam", cooldown = 30)
	public static void steamTop100In2Weeks(CommandEvent event) throws CommandException {
		Map data;
		try {
			data = new Gson().fromJson(Main.getHTML("https://steamspy.com/api.php?request=top100in2weeks"), Map.class);
		} catch (JsonSyntaxException | IOException e) {
			throw new CommandException("An unknown error occurred while getting the top 100 list.", e);
		}
		String msg = "```fix\n";
		int counter = 0;
		for (Map game : ((Map<String, Map>) data).values()) {
			counter += 1;
			msg += counter + ". " + game.get("name") + "\n";
			if (msg.length() > 1900) {
				event.reply(msg + "```");
				msg = "```fix\n";
			}
		}
		event.reply(msg + "```");
	}

	@Subcommand(help = "Tells you how many apps are currently on Steam.", name = "appcount", parent = "com.ptsmods.impulse.commands.General.steam")
	public static void steamAppCount(CommandEvent event) {
		event.reply("There are currently **%s** apps and games on Steam (latest app: **%s**).", games.size(), games.values().toArray(new String[0])[games.values().size() - 1]);
	}

	@Subcommand(help = "Get CS:GO stats from a user.\nExample:\n[p]steam getcsgostats PlanetTeamSpeak (Warning: I am noob)\n[p]steam getcsgostats 76561198187354157 (Same user but with Steam 64 ID)", name = "getcsgostats", parent = "com.ptsmods.impulse.commands.General.steam", cooldown = 30)
	public static void steamGetCSGOStats(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			String userid;
			String username;
			if (Main.isDouble(event.getArgs())) {
				Map data;
				try {
					data = new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=" + Main.apiKeys.get("steam") + "&steamids=" + event.getArgs()), Map.class);
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting the player summaries.", e);
				}
				if (data.isEmpty()) {
					event.reply("That's not a valid user ID.");
					return;
				} else {
					username = ((Map) ((List) ((Map) data.get("response")).get("players")).get(0)).get("personaname").toString();
					userid = new String(event.getArgs().toCharArray());
				}
			} else {
				Map data;
				try {
					data = (Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key=" + Main.apiKeys.get("steam") + "&vanityurl=" + event.getArgs()), Map.class).get("response");
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while resolving the vanity URL.", e);
				}
				if ((double) data.get("success") == 42D) {
					event.reply("That's not a valid username.");
					return;
				} else {
					username = new String(event.getArgs().toCharArray());
					userid = data.get("steamid").toString();
				}
			}
			List data;
			try {
				data = (List) ((Map) new Gson().fromJson(Main.getHTML("http://api.steampowered.com/ISteamUserStats/GetUserStatsForGame/v0002/?appid=730&key=" + Main.apiKeys.get("steam") + "&steamid=" + userid), Map.class).get("playerstats")).get("stats");
			} catch (JsonSyntaxException | IOException e) {
				throw new CommandException("An unknown error occurred while getting the user stats.", e);
			}
			if (data.isEmpty())
				event.reply("The given user does not seem to own a copy of CS:GO.");
			else {
				Map<String, String> data1 = new HashMap();
				for (int i : Main.range(data.size()))
					data1.put(((Map) data.get(i)).get("name").toString(), ((Map) data.get(i)).get("value").toString());
				event.reply("```fix\nUsername: %s\nUser ID: %s\nTotal kills: %s\nTotal deaths: %s\nKDR: %.4f\nTotal time played: %s\nTotal bombs planted: %s\nTotal wins: %s\nTotal damage done: %s\nTotal money earned: %s\nHeadshots done: %s\nTotal shots fired: %s\nTotal shots hit: %s\nHit ratio: %.2f%%\nTotal rounds played: %s```", username, userid, data1.get("total_kills").toString().split("\\.")[0], data1.get("total_deaths").toString().split("\\.")[0], Double.parseDouble(data1.get("total_kills")) / Double.parseDouble(data1.get("total_deaths")), Main.formatMillis(Integer.parseInt(data1.get("total_time_played").toString().split("\\.")[0]) * 1000).replaceAll("\\*", ""), data1.get("total_planted_bombs").toString().split("\\.")[0], data1.get("total_wins").toString().split("\\.")[0], data1.get("total_damage_done").toString().split("\\.")[0], data1.get("total_money_earned").toString().split("\\.")[0], data1.get("total_kills_headshot").toString().split("\\.")[0], data1.get("total_shots_fired").toString().split("\\.")[0], data1.get("total_shots_hit").toString().split("\\.")[0], MathHelper.percentage(Double.parseDouble(data1.get("total_shots_fired")), Double.parseDouble(data1.get("total_shots_hit"))).floatValue(), data1.get("total_rounds_played").toString().split("\\.")[0]);
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Get some user or tank info.", name = "wot")
	public static void wot(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Get some user info.\nUser has to be the Wargaming username and server has to be eu, ru, asia, na, or kr.", name = "getuserinfo", parent = "com.ptsmods.impulse.commands.General.wot", cooldown = 30, arguments = "<user> <server>", guildOnly = true)
	public static void wotGetUserInfo(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(" ").length == 2) {
			String username = event.getArgs().split(" ")[0];
			String server = event.getArgs().split(" ")[1].toLowerCase();
			String userid = null;
			if (server.equals("na")) server = "com";
			if (Lists.newArrayList("eu", "ru", "asia", "com", "kr").contains(server)) {
				Map data;
				try {
					data = new Gson().fromJson(Main.getHTML("https://api.worldoftanks." + server + "/wot/account/list/?application_id=" + Main.apiKeys.get("wargaming") + "&search=" + username), Map.class);
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting the user's id.", e);
				}
				if (data.containsKey("error"))
					event.reply("An unknown error occurred while getting the data: %s, %s.", Main.getIntFromPossibleDouble(((Map) data.get("error")).get("code")), ((Map) data.get("error")).get("message"));
				else {
					Map data1;
					if (Main.getIntFromPossibleDouble(((Map) data.get("meta")).get("count")) > 1) {
						List<Map<String, String>> users = new ArrayList();
						for (int i : Main.range(((List) data.get("data")).size()))
							users.add(Main.newHashMap(new String[] {"username", "id"}, new String[] {((Map) ((List) data.get("data")).get(i)).get("nickname").toString(), String.format("%.0f", ((Map) ((List) data.get("data")).get(i)).get("account_id"))}));
						String msg = "Found multiple results, please pick 1:\n";
						for (int i : Main.range(users.size()))
							msg += i + 1 + ". " + users.get(i).get("username") + "\n";
						event.reply(msg.trim());
						Message response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
						if (response == null) {
							event.reply("No response gotten.");
							return;
						} else if (Main.isInteger(response.getContent())) {
							int choice = Integer.parseInt(response.getContent());
							if (choice > users.size()) {
								event.reply("The chosen number was larger than the amount of choices.");
								return;
							} else if (choice < 1) {
								event.reply("The chosen number was smaller than 1.");
								return;
							} else userid = users.get(choice - 1).get("id");
						}
					} else if (Main.getIntFromPossibleDouble(((Map) data.get("meta")).get("count")) < 1) {
						event.reply("Could not find any results for '%s'.", username);
						return;
					} else userid = ((Map) ((List) data.get("data")).get(0)).get("account_id").toString();
					try {
						data1 = (Map) ((Map) new Gson().fromJson(Main.getHTML("https://api.worldoftanks." + server + "/wot/account/info/?application_id=" + Main.apiKeys.get("wargaming") + "&account_id=" + userid), Map.class).get("data")).get(userid);
					} catch (JsonSyntaxException | IOException e) {
						throw new CommandException("An unknown error occurred while getting the user's data.", e);
					}
					username = data1.get("nickname").toString();
					String globalRating = data1.get("global_rating").toString().split("\\.")[0];
					String clientLang = data1.get("client_language").toString();
					Long lastBattleTime = Main.getLongFromPossibleDouble(data1.get("last_battle_time")) * 1000;
					Long createdAt = Main.getLongFromPossibleDouble(data1.get("created_at")) * 1000;
					data1 = (Map) ((Map) data1.get("statistics")).get("all");
					event.reply("```fix\nUsername: %s\nUser ID: %s\nCreated at: %s (DD/MM/YY)\nLast battle: %s (DD/MM/YY)\nGlobal rating: %s\nClient language: %s\nSpotted: %s\nMax xp earned: %s\nAverage damage blocked: %s\nDirect hits received: %s\nTimes ammoracked player: %s\nPenetrations received: %s\nPenetrations done: %s\nShots: %s\nHits: %s\nHit percentage: %s%%\nFree xp: %s\nBattles done: %s\nSurived battles: %s\nBattles won: %s\nBattles lost: %s\nBattles drawn: %s\nDropped capture points: %s\nTotal damage dealt: %s```", username, userid, new SimpleDateFormat("dd/MM/yyyy").format(new Date(createdAt)), new SimpleDateFormat("dd/MM/yyyy").format(new Date(lastBattleTime)), globalRating, clientLang, data1.get("spotted").toString().split("\\.")[0], data1.get("max_xp").toString().split("\\.")[0], data1.get("avg_damage_blocked"), data1.get("direct_hits_received").toString().split("\\.")[0], data1.get("explosion_hits").toString().split("\\.")[0], data1.get("piercings_received").toString().split("\\.")[0], data1.get("piercings").toString().split("\\.")[0], data1.get("shots").toString().split("\\.")[0], data1.get("hits").toString().split("\\.")[0], MathHelper.percentage((double) data1.get("shots"), (double) data1.get("hits")), data1.get("xp").toString().split("\\.")[0], data1.get("battles").toString().split("\\.")[0], data1.get("survived_battles").toString().split("\\.")[0], data1.get("wins").toString().split("\\.")[0], data1.get("losses").toString().split("\\.")[0], data1.get("draws").toString().split("\\.")[0], data1.get("dropped_capture_points").toString().split("\\.")[0], data1.get("damage_dealt").toString().split("\\.")[0]);
				}
			} else Main.sendCommandHelp(event);
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Get some tank info.", name = "gettankinfo", parent = "com.ptsmods.impulse.commands.General.wot", arguments = "<tank>", cooldown = 30)
	public static void wotGetTankInfo(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			List<Map> tanks;
			try {
				tanks = new ArrayList<>(((Map<String, Map>) new Gson().fromJson(Main.getHTML("https://api.worldoftanks.com/wot/encyclopedia/tanks/?application_id=" + Main.apiKeys.get("wargaming")), Map.class).get("data")).values());
			} catch (JsonSyntaxException | IOException e) {
				throw new CommandException("An unknown error occurred while getting the tank ID matching the given name.", e);
			}
			boolean found = false;
			int tankID = -1;
			for (Map tank : tanks)
				if (tank.get("short_name_i18n").toString().equalsIgnoreCase(event.getArgs())) {
					tankID = Main.getIntFromPossibleDouble(tank.get("tank_id"));
					found = true;
					break;
				}
			if (!found)
				event.reply("A tank with the given name could not be found.");
			else {
				Map data;
				try {
					data = (Map) new Gson().fromJson(Main.getHTML("https://api.worldoftanks.com/wot/encyclopedia/tankinfo/?application_id=" + Main.apiKeys.get("wargaming") + "&tank_id=" + tankID), Map.class).get("data");
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting data for this tank.", e);
				}
				if (data.get(Integer.toString(tankID)) == null)
					event.reply("No data found for that tank.");
				else {
					data = (Map) data.get(Integer.toString(tankID));
					event.reply("```fix\nTank name: %s\nTank ID: %s\nTier: %s\nEngine power: %s\nVision radius: %s (metres)\nMax gun penetration: %s (mm)\nMax health: %s\nWeight (tonnes): %s\nRadio distance: %s\nTank type: %s\nChassis rotation speed (degrees per second): %s\nGun name: %s\nMax ammo: %s\nNation: %s\nTurret rotation speed: %s\nIs premium: %s\nGold price: %s\nCredit price: %s\nXp price: %s\nSpeed limit: %s (km/s)\nMax damage: %s\n```", data.get("name_i18n"), tankID, data.get("level").toString().split("\\.")[0], data.get("engine_power").toString().split("\\.")[0], data.get("circular_vision_radius").toString().split("\\.")[0], data.get("gun_piercing_power_max").toString().split("\\.")[0], data.get("max_health").toString().split("\\.")[0], data.get("weight"), data.get("radio_distance").toString().split("\\.")[0], data.get("type_i18n"), data.get("chassis_rotation_speed").toString().split("\\.")[0], data.get("gun_name"), data.get("gun_max_ammo").toString().split("\\.")[0], data.get("nation_i18n"), data.get("turret_rotation_speed").toString().split("\\.")[0], data.get("is_premium"), data.get("price_gold").toString().split("\\.")[0], data.get("price_credit").toString().split("\\.")[0], data.get("price_xp").toString().split("\\.")[0], data.get("speed_limit").toString().split("\\.")[0], data.get("gun_damage_max").toString().split("\\.")[0]);
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Generates a LMGTFY URL.", name = "lmgtfy")
	public static void lmgtfy(CommandEvent event) {
		if (!event.argsEmpty())
			event.reply("https://lmgtfy.com/?q=" + Main.percentEncode(event.getArgs()));
		else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Generates a name for the given gender.\nGender can either be male, female, or Random.INSTANCE.\nType can either be first, last, or both.", name = "genname", arguments = "[gender] [type]")
	public static void genName(CommandEvent event) {
		int type = event.argsEmpty() || event.getArgs().split(" ").length < 2 || event.getArgs().split(" ")[1].equalsIgnoreCase("both") ? 0 : event.getArgs().split(" ")[1].equalsIgnoreCase("last") ? 2 : 1;
		Gender gender = event.argsEmpty() || event.getArgs().split(" ")[0].equalsIgnoreCase("Random.INSTANCE") ? Random.INSTANCE.choice(Gender.values()) : event.getArgs().split(" ")[0].equalsIgnoreCase("female") ? Gender.FEMALE : Gender.MALE;
		Name name = new NameGenerator().generateName(gender);
		event.reply("Gender: **%s**\nName: **%s**", name.getGender().name(), type == 0 ? name.toString() : type == 1 ? name.getFirstName() : name.getLastName());
	}

	@Command(category = "General", help = "Tells you the time in the given location.", name = "time", arguments = "<location>", cooldown = 30)
	public static void time(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			Map data;
			try {
				data = new Gson().fromJson(Main.getHTML("https://maps.googleapis.com/maps/api/geocode/json?address=" + Main.percentEncode(event.getArgs()) + "&key=" + Main.apiKeys.get("geocoding")), Map.class);
			} catch (JsonSyntaxException | IOException e) {
				throw new CommandException("An unknown error occurred while getting the longitude and latitude from the Google API.", e);
			}
			if (data.get("status").toString().equals("ZERO_RESULTS"))
				event.reply("No results found for **%s**.", event.getArgs());
			else if (data.get("status").toString().equals("OK")) {
				double lng = (double) ((Map) ((Map) ((Map) ((List) data.get("results")).get(0)).get("geometry")).get("location")).get("lng");
				double lat = (double) ((Map) ((Map) ((Map) ((List) data.get("results")).get(0)).get("geometry")).get("location")).get("lat");
				String address = ((Map) ((List) data.get("results")).get(0)).get("formatted_address").toString();
				Map data1;
				try {
					data1 = new Gson().fromJson(Main.getHTML("https://maps.googleapis.com/maps/api/timezone/json?location=" + lat + "," + lng + "&timestamp=" + new Date().getTime() / 1000 + "&key=" + Main.apiKeys.get("timezone")), Map.class);
				} catch (JsonSyntaxException | IOException e) {
					throw new CommandException("An unknown error occurred while getting the time and timezone from the Google API.", e);
				}
				if (data1.get("status").toString().equals("OK"))
					event.reply("**%s**\n\t%s (%s)", new SimpleDateFormat("EEEE d MMM y HH:mm:ss").format(new Date(Calendar.getInstance().getTimeInMillis() + Main.getIntFromPossibleDouble(data1.get("dstOffset")) * 1000 + Main.getIntFromPossibleDouble(data1.get("rawOffset")) * 1000)), address, data1.get("timeZoneName"));
				else event.reply("An unknown error occurred while getting the time and timezone from the Google API.");
			} else event.reply("An unknown error occurred while getting the longitude and latitude from the Google API.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "General", help = "Some general server information.", name = "server")
	public static void server(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Shows you the server icon.", name = "icon", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverIcon(CommandEvent event) {
		event.reply(event.getGuild().getIconUrl());
	}

	@Subcommand(help = "Shows you all the custom emotes this server has.", name = "emotes", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverEmotes(CommandEvent event) {
		String emotes = "";
		for (Emote emote : event.getGuild().getEmotes())
			emotes += emote.getAsMention() + " ";
		emotes += "(" + event.getGuild().getEmotes().size() + " emotes)";
		event.reply(event.getGuild().getEmotes().isEmpty() ? "This server has no custom emotes." : emotes.trim());
	}

	@Subcommand(help = "Shows you all the roles this server has.", name = "roles", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverRoles(CommandEvent event) {
		List<String> roles = new ArrayList();
		for (Role role : event.getGuild().getRoles())
			if (!role.getName().toLowerCase().equals("@everyone") && !role.getName().toLowerCase().equals("@here")) roles.add("**" + role.getName() + "**");
		event.reply("This server has the following roles: %s (%s roles)", Main.joinNiceString(roles), roles.size());
	}

	@Subcommand(help = "Tells you who the owner of this server is.", name = "owner", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true, cooldown = 60)
	public static void serverOwner(CommandEvent event) {
		event.reply("This server's owner is %s.", event.getGuild().getOwner().getAsMention());
	}

	@Subcommand(help = "Shows you some information about the given role.", name = "roleinfo", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true, arguments = "<role>")
	public static void serverRoleInfo(CommandEvent event) {
		if (!event.argsEmpty()) {
			Role role = null;
			if (!event.getMessage().getMentionedRoles().isEmpty())
				role = event.getMessage().getMentionedRoles().get(0);
			else role = event.getGuild().getRolesByName(event.getArgs(), true).isEmpty() ? null : Random.INSTANCE.choice(event.getGuild().getRolesByName(event.getArgs(), true));
			if (role == null)
				event.reply("The given role could not be found.");
			else {
				int userCount = 0;
				for (Member member : event.getGuild().getMembers())
					if (member.getRoles().contains(role)) userCount += 1;
				EmbedBuilder embed = new EmbedBuilder();
				embed.setTitle("Role info");
				embed.setColor(role.getColor() == null ? new Color(0) : role.getColor());
				embed.addField("Name", role.getName(), true);
				embed.addField("Color", "#" + Integer.toHexString(role.getColor() == null ? new Color(0).getRGB() : role.getColor().getRGB()).substring(2).toUpperCase(), true);
				embed.addField("Position", "" + role.getPosition(), true);
				embed.addField("User count", "" + userCount, true);
				embed.addField("Mentionable", "" + role.isMentionable(), true);
				embed.addField("Hoisted", "" + role.isHoisted(), true);
				embed.addField("Administrator", "" + role.hasPermission(Permission.ADMINISTRATOR), true);
				embed.addField("Can ban members", "" + role.hasPermission(Permission.BAN_MEMBERS), true);
				embed.addField("Can kick members", "" + role.hasPermission(Permission.KICK_MEMBERS), true);
				embed.addField("Can change nickname", "" + role.hasPermission(Permission.NICKNAME_CHANGE), true);
				embed.addField("Voice connect", "" + role.hasPermission(Permission.VOICE_CONNECT), true);
				embed.addField("Create instant invites", "" + role.hasPermission(Permission.CREATE_INSTANT_INVITE), true);
				embed.addField("Can deafen members", "" + role.hasPermission(Permission.VOICE_DEAF_OTHERS), true);
				embed.addField("Can embed links", "" + role.hasPermission(Permission.MESSAGE_EMBED_LINKS), true);
				embed.addField("Can use external emotes", "" + role.hasPermission(Permission.MESSAGE_EXT_EMOJI), true);
				embed.addField("Can manage channels", "" + role.hasPermission(Permission.MANAGE_CHANNEL), true);
				embed.addField("Can manage emotes", "" + role.hasPermission(Permission.MANAGE_EMOTES), true);
				embed.addField("Can manage messages", "" + role.hasPermission(Permission.MESSAGE_MANAGE), true);
				embed.addField("Can manage nicknames", "" + role.hasPermission(Permission.NICKNAME_MANAGE), true);
				embed.addField("Can manage roles", "" + role.hasPermission(Permission.MANAGE_ROLES), true);
				embed.addField("Can manage server", "" + role.hasPermission(Permission.MANAGE_SERVER), true);
				embed.addField("Can mention everyone", "" + role.hasPermission(Permission.MESSAGE_MENTION_EVERYONE), true);
				embed.addField("Can move members", "" + role.hasPermission(Permission.VOICE_MOVE_OTHERS), true);
				embed.addField("Can mute members", "" + role.hasPermission(Permission.VOICE_MUTE_OTHERS), true);
				embed.addField("Can read message history", "" + role.hasPermission(Permission.MESSAGE_HISTORY), true);
				event.reply(embed.build());
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Counts the members in this server.", name = "membercount", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverMemberCount(CommandEvent event) {
		int members = 0;
		int bots = 0;
		for (Member member : event.getGuild().getMembers())
			if (member.getUser().isBot())
				bots += 1;
			else members += 1;
		event.reply("This server has **%s users** and **%s bots** with a total of **%s members**.", members, bots, members + bots);
	}

	@Subcommand(help = "Shows you the ID of this server and this channel.", name = "id", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverId(CommandEvent event) {
		event.reply("This server's ID is **%s**, this channel's ID is **%s**.", event.getGuild().getId(), event.getChannel().getId());
	}

	@Subcommand(help = "Tells you what the default channel of this server is.", name = "defaultchannel", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverDefaultChannel(CommandEvent event) {
		event.reply("This server's default channel is %s.", event.getGuild().getDefaultChannel() == null ? "deleted" : event.getGuild().getDefaultChannel().getAsMention());
	}

	@Subcommand(help = "Tells you some information about the given user.", name = "userinfo", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true, arguments = "<user>")
	public static void serverUserInfo(CommandEvent event) {
		if (!event.argsEmpty()) {
			Member member = Main.getMemberFromInput(event.getMessage());
			List<String> roles = new ArrayList();
			for (Role role1 : member.getRoles())
				if (!role1.getName().equalsIgnoreCase("@everyone") && !role1.getName().equalsIgnoreCase("@here")) roles.add(role1.getName());
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Role info");
			embed.setColor(member.getColor() == null ? new Color(0) : member.getColor());
			embed.addField("Name", member.getUser().getName(), true);
			embed.addField("Discriminator", member.getUser().getDiscriminator(), true);
			embed.addField("Nickname", member.getEffectiveName(), true);
			embed.addField("ID", member.getUser().getId(), true);
			embed.addField("Status", member.getOnlineStatus().name(), true);
			embed.addField("Playing", member.getGame() == null ? "" : member.getGame().getName(), true);
			embed.addField("Is bot", "" + member.getUser().isBot(), true);
			embed.addField("Muted in this server", "" + member.getVoiceState().isMuted(), true);
			embed.addField("Deafened in this server", "" + member.getVoiceState().isDeafened(), true);
			embed.addField("Joined discord at", new SimpleDateFormat("E d MMM y HH:mm:ss").format(new Date(member.getUser().getCreationTime().toEpochSecond() * 1000)), true);
			embed.addField("Joined server at", new SimpleDateFormat("E d MMM y HH:mm:ss").format(new Date(member.getJoinDate().toEpochSecond() * 1000)), true);
			embed.addField("Color", "#" + Integer.toHexString(member.getColor() == null ? -16777216 : member.getColor().getRGB()).substring(2).toUpperCase(), true);
			embed.addField("Roles", Main.joinNiceString(roles), true);
			event.reply(embed.build());
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Some general information about this server.", name = "info", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverInfo(CommandEvent event) {
		Guild guild = event.getGuild();
		int bots = 0;
		int botsOnline = 0;
		int members = 0;
		int membersOnline = 0;
		for (Member member : guild.getMembers())
			if (member.getUser().isBot()) {
				bots += 1;
				if (member.getOnlineStatus() != OnlineStatus.OFFLINE) botsOnline += 1;
			} else {
				members += 1;
				if (member.getOnlineStatus() != OnlineStatus.OFFLINE) membersOnline += 1;
			}
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Server info");
		embed.setColor(Color.CYAN);
		embed.addField("Name", guild.getName(), true);
		embed.addField("ID", guild.getId(), true);
		embed.addField("Region", guild.getRegion().getName(), true);
		embed.addField("Verification level", guild.getVerificationLevel().name(), true);
		embed.addField("Created at", new Date(guild.getCreationTime().toEpochSecond() * 1000).toString(), true);
		embed.addField("Roles", "" + guild.getRoles().size(), true);
		embed.addBlankField(false);
		embed.addField("Owner", Main.str(guild.getOwner()), true);
		embed.addBlankField(true);
		embed.addField("Owner ID", guild.getOwner().getUser().getId(), true);
		embed.addBlankField(false);
		embed.addField("Bots", "" + bots, true);
		embed.addField("Members", "" + members, true);
		embed.addField("Total users", "" + (bots + members), true);
		embed.addField("Bots percentage", MathHelper.percentage(bots + members, bots).toString(), true);
		embed.addField("Bots online", "" + botsOnline, true);
		embed.addField("Bots online percentage", MathHelper.percentage(bots, botsOnline).toString(), true);
		embed.addField("Members online", "" + membersOnline, true);
		embed.addBlankField(true);
		embed.addField("Members online percentage", MathHelper.percentage(members, membersOnline).toString(), true);
		event.reply(embed.build());
	}

	@Subcommand(help = "Shows you all of the channels in this server.", name = "channels", parent = "com.ptsmods.impulse.commands.General.server", guildOnly = true)
	public static void serverChannels(CommandEvent event) {
		String msg = "**Text channels**";
		for (TextChannel channel : event.getGuild().getTextChannels()) {
			msg += "\n\t" + channel.getAsMention();
			if (msg.length() > 1990) {
				event.reply(msg);
				msg = "";
			}
		}
		msg += "\n\n**Voice Channels**";
		for (VoiceChannel channel : event.getGuild().getVoiceChannels()) {
			msg += "\n\t" + channel.getName();
			if (msg.length() > 1990) {
				event.reply(msg);
				msg = "";
			}
		}
		event.reply(msg);
	}

	@Command(category = "General", help = "Have a conversation with the bot.", name = "cleverbot")
	public static void Cleverbot(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			String response = Cleverbot.newBot().askQuestion(event.getArgs());
			event.reply(response == null || response.isEmpty() ? "No response gotten, please try again." : response);
		} else Main.sendCommandHelp(event);
	}

}
