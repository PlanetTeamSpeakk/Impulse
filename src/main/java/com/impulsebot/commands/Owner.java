package com.impulsebot.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.impulsebot.Main;
import com.impulsebot.Main.LogType;
import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.client.ClientCustomPacket;
import com.impulsebot.net.server.ServerCustomPacket;
import com.impulsebot.utils.Config;
import com.impulsebot.utils.UsageMonitorer;
import com.impulsebot.utils.VMManagement;
import com.impulsebot.utils.commands.Command;
import com.impulsebot.utils.commands.CommandEvent;
import com.impulsebot.utils.commands.CommandException;
import com.impulsebot.utils.commands.CommandPermissionException;
import com.impulsebot.utils.commands.Subcommand;
import com.impulsebot.utils.compiler.CompilationException;

import javafx.application.Platform;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class Owner {

	static {
		Main.addCommandHook(event -> {
			if (!event.getAuthor().getId().equals(Main.getOwner().getId()) && Main.devMode()) throw new CommandPermissionException("Developer mode is enabled which means only my owner can use commands.");
		});
	}

	public static volatile boolean	isAnnouncing	= false;
	public static volatile Message	status;
	public static volatile int		successes		= 0;
	public static volatile int		failures		= 0;

	@Command(category = "Owner", help = "Sends a message to every server.", name = "announce", arguments = "<message>", ownerCommand = true)
	public static void announce(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			status = event.getChannel().sendMessageFormat("Sending announcement to every guild...", Main.getGuilds().size()).complete();
			isAnnouncing = true;
			System.currentTimeMillis();
			ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

				private static final long serialVersionUID = -2091220554133059892L;

				@Override
				public void runOnServer() {
					int shardId = getShardId();
					for (String id : Main.getIdentifications().keySet())
						ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

							private static final long serialVersionUID = 4070235468236076253L;

							@Override
							public void runOnClient() {
								int succeeded = 0;
								int failed = 0;
								for (Guild guild : Main.getGuilds())
									try {
										Main.getSendChannel(guild).sendMessageFormat("%s ~ %s", event.getArgs(), Main.str(event.getAuthor())).queue();
										succeeded += 1;
									} catch (Exception ignored) { // this could be caused by Main#getSendChannel(Guild) returning null or not
																	// having the right permissions.
										failed += 1;
									}
								int successes = succeeded;
								int failures = failed;
								ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

									private static final long serialVersionUID = 4049317402740308008L;

									@Override
									public void runOnServer() {
										int shardId0 = getShardId();
										ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

											private static final long serialVersionUID = 6663832942656744408L;

											@Override
											public void runOnClient() {
												Owner.successes += successes;
												Owner.failures += failures;
												if (shardId0 + 1 == Main.getShardCount()) {
													status.editMessageFormat("Successfully sent the announcement to **%s** guilds, could not send the announcement to **%s** guilds.", Owner.successes, Owner.failures).queue();
													Owner.successes = 0;
													Owner.failures = 0;
													isAnnouncing = false;
													status = null;
												}
											}

										}, shardId);
									}
								});
							}
						}, id);
				}
			});
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Send a message to my owner.", name = "contact", cooldown = 60, arguments = "<message>")
	public static void contact(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Main.sendPrivateMessage(Main.getOwner(), String.format("**%s** (%s) has sent you a message from **%s** (%s):\n\n", Main.str(event.getAuthor()), event.getAuthor().getId(), event.getGuild() == null ? "direct messages" : event.getGuild().getName(), event.getGuild() == null ? "null" : event.getGuild().getId()) + event.getArgs());
			event.reply("Successfully sent your message to my owner! Maybe you'll even get a response! OwO");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Compiles and runs Java code.", name = "debug", ownerCommand = true, hidden = true)
	public static void debug(CommandEvent event) {
		try {
			Object out = String.valueOf(Main.compileAndRunJavaCode(event.getArgs(), Main.newHashMap(new String[] {"event"}, new Object[] {event})));
			out = out == null ? "null" : out.toString();
			List<String> messages = new ArrayList<>();
			while (out.toString().length() > 1990) {
				messages.add(out.toString().substring(0, 1990).trim());
				out = out.toString().substring(1990, out.toString().length());
			}
			messages.add(out.toString());
			event.reply("Input:```java\n" + event.getArgs() + "```\nOutput:```java\n" + messages.get(0) + "```");
			messages.remove(0);
			for (String message : messages)
				event.reply("```java\n" + message + "```");
		} catch (CompilationException e) {
			event.reply("```java\n" + e.toString() + "```");
		} catch (Throwable e) {
			if (e.getMessage() != null && e.getMessage().contains("Cannot run program \"javac\""))
				event.reply("The Java JDK was either not added to the PATH environment variable or not installed.");
			else {
				StackTraceElement stElement = null;
				for (StackTraceElement element : e.getStackTrace())
					if (element.getFileName() != null && element.getClassName().startsWith("com.impulsebot")) stElement = element;
				String output = String.format("A `%s` exception was thrown at line %s in %s while executing the code. Stacktrace:\n```java\n%s```", e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), Main.generateStackTrace(e));
				if (output.length() < 1997)
					event.getChannel().sendMessage(output).queue();
				else while (output.length() > 1997) {
					event.getChannel().sendMessage(output.substring(0, 1997) + "```").queue();
					output = output.substring(1997);
				}
			}
		}
	}

	@Command(category = "Owner", help = "Basically just the same as debug, but it runs the code on the JFX thread.", name = "debugjfx", ownerCommand = true, hidden = true)
	public static void debugJFX(CommandEvent event) {
		Platform.runLater(() -> {
			debug(event);
		});
	}

	@Command(category = "Owner", help = "Shows you all the servers the bot is in.", name = "servers", ownerCommand = true, hidden = true)
	public static void servers(CommandEvent event) {
		Map<Integer, String> pages = new HashMap();
		int counter = 0;
		int counter1 = 0;
		String page = "```css\n[Page 1/%s]\n\t";
		int id = 0;
		List<String> guilds = new ArrayList();
		for (Guild guild : Main.getGuilds())
			guilds.add(guild.getName());
		for (String guild : Main.sort(guilds)) {
			page += guild + "\n\t";
			counter += 1;
			counter1 += 1;
			if (counter == 10 || counter1 == guilds.size()) {
				counter = 0;
				id += 1;
				pages.put(id, page.trim() + "```");
				page = "```css\n[Page " + (id + 1) + "/%s]\n\t";
			}
		}
		int pageN = 1;
		if (event.getArgs().length() != 0 && Main.isInteger(event.getArgs().split(" ")[0])) pageN = Integer.parseInt(event.getArgs().split(" ")[0]);
		if (pageN > pages.size())
			event.reply("The maximum page is " + (pages.size() - 1) + ".");
		else event.reply(pages.get(pageN), pages.size());
	}

	@Command(category = "Owner", help = "Manage settings.", name = "set", ownerCommand = true)
	public static void set(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the bot's status.", name = "status", parent = "com.impulsebot.commands.Owner.set", arguments = "<status>", ownerCommand = true)
	public static void setStatus(CommandEvent event) {
		if (!event.getArgs().isEmpty() && event.getArgs().split(" ")[0].equalsIgnoreCase("OFFLINE") || event.getArgs().split(" ")[0].equalsIgnoreCase("INVISIBLE") || event.getArgs().split(" ")[0].equalsIgnoreCase("DND") || event.getArgs().split(" ")[0].equalsIgnoreCase("DO_NOT_DISTURB") || event.getArgs().split(" ")[0].equalsIgnoreCase("IDLE") || event.getArgs().split(" ")[0].equalsIgnoreCase("ONLINE")) {
			Main.setOnlineStatus(Main.getStatusFromString(event.getArgs().split(" ")[0]));
			event.reply("Successfully set the online status to " + Main.getStatusFromString(event.getArgs().split(" ")[0]).name() + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the bot's game.", name = "game", parent = "com.impulsebot.commands.Owner.set", arguments = "[game]", ownerCommand = true)
	public static void setGame(CommandEvent event) {
		Main.setGame(event.getArgs());
		event.reply(event.getArgs().isEmpty() ? "The game has been reset." : "Successfully set the game to " + event.getArgs() + ".");
	}

	@Subcommand(help = "Set the bot's avatar.", name = "avatar", parent = "com.impulsebot.commands.Owner.set", arguments = "<avatar>", ownerCommand = true)
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

	@Subcommand(help = "Set the bot's nickname.", name = "nickname", parent = "com.impulsebot.commands.Owner.set", arguments = "<nickname>", userPermissions = {Permission.NICKNAME_MANAGE}, botPermissions = {Permission.NICKNAME_CHANGE})
	public static void setNickname(CommandEvent event) {
		event.getGuild().getController().setNickname(event.getGuild().getSelfMember(), event.getArgs()).queue();
		event.reply(event.getArgs().isEmpty() ? "Successfully reset my nickname." : "Successfully set my nickname to " + event.getArgs() + ".");
	}

	@Subcommand(help = "Set the bot's name, the given name should be bigger than 2 characters and smaller than 32 characters.", name = "name", parent = "com.impulsebot.commands.Owner.set", ownerCommand = true)
	public static void setName(CommandEvent event) {
		if (event.getArgs().length() >= 2 && event.getArgs().length() <= 32) {
			event.getJDA().getSelfUser().getManager().setName(event.getArgs()).queue();
			event.reply("Successfully set my name to " + event.getArgs() + ".");
		} else Main.sendCommandHelp(event);
	}

	// TODO: test and optionally fix the shutdown and announce commands
	@Command(category = "Owner", help = "Shuts the bot down.", name = "shutdown", ownerCommand = true)
	public static void shutdown(CommandEvent event) {
		event.getChannel().sendMessage("Kk, shutting down.").complete();
		String author = Main.str(event.getAuthor());
		ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

			private static final long serialVersionUID = -2091220554133059892L;

			@Override
			public void runOnServer() {
				for (String id : Main.getIdentifications().keySet())
					ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

						private static final long serialVersionUID = 4070235468236076253L;

						@Override
						public void runOnClient() {
							Main.print(LogType.WARN, author + " requested the bot to be shut down.");
							Main.shutdown(0);
						}
					}, id);
				Main.shutdown(0);
			}
		});
	}

	@Command(category = "Owner", help = "Toggles developer mode.", name = "toggledevmode", ownerCommand = true, hidden = true)
	public static void toggleDevMode(CommandEvent event) {
		Main.devMode(!Main.devMode());
		Main.setGame(Game.of(GameType.DEFAULT, Main.devMode() ? "DEVELOPER MODE" : "try " + Config.INSTANCE.get("prefix") + "help!"));
		event.reply("Developer mode has been " + (Main.devMode() ? "enabled" : "disabled") + ", " + (Main.devMode() ? "errors will no longer be sent privately and only you will now be able to use commands." : "errors will once again be sent privately and everyone will be able to use commands again."));
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
			Message msg = event.getChannel().sendMessage("Executing the given command on the command line...").complete();
			if (Main.isWindows()) event.setArgs("cmd /c " + event.getArgs());
			Process process;
			try {
				process = Runtime.getRuntime().exec(event.getArgs());
			} catch (IOException e) {
				throw new CommandException("An error occurred while parsing the given command.", e);
			}
			String output = "";
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			try {
				while ((line = input.readLine()) != null)
					output += line + "\n";
			} catch (IOException e) {
				throw new CommandException("An error occurred while parsing the given command.", e);
			}
			msg.editMessage("Input:\n```\n" + event.getOriginalArgs() + "```\nOutput:\n```\n" + output.trim() + "```").queue();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Calculates the average amount of messages the bot receives per second.", name = "averagemessages", ownerCommand = true, arguments = "<seconds>")
	public static void averageMessages(CommandEvent event) {
		if (!event.argsEmpty() && Main.isFloat(event.getArgs())) {
			Message status = event.getTextChannel().sendMessage("Retrieving messages, please wait...").complete();
			long msgs = Main.getReceivedMessages().size();
			long millis = System.currentTimeMillis();
			try {
				Thread.sleep((long) Float.parseFloat(event.getArgs()) * 1000L);
			} catch (InterruptedException e) {
			}
			status.editMessageFormat("Done! Received **%s** messages in **%s** seconds with an average of **%s** messages per second.", Main.getReceivedMessages().size() - msgs, (System.currentTimeMillis() - millis) / 1000, (float) (Main.getReceivedMessages().size() - msgs) / (float) ((System.currentTimeMillis() - millis) / 1000)).queue();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Sent a message to either a user, a channel, or a guild.", name = "whisper", arguments = "<id> <message>", ownerCommand = true)
	public static void whisper(CommandEvent event) {
		if (!event.argsEmpty() && event.getArgs().split(" ").length > 1 && Main.isLong(event.getArgs().split(" ")[0])) {
			String id = event.getArgs().split(" ")[0];
			String message = Main.join(Main.removeArg(event.getArgs().split(" "), 0)) + " ~ " + Main.str(event.getAuthor());
			Object o = Main.getUserById(id);
			o = o == null ? Main.getTextChannelById(id) : Main.getGuildById(id);
			if (o instanceof User)
				Main.sendPrivateMessage((User) o, message);
			else if (o instanceof TextChannel)
				((TextChannel) o).sendMessage(message).queue();
			else if (o instanceof Guild) {
				if (Main.getSendChannel((Guild) o) != null) Main.getSendChannel((Guild) o).sendMessage(message).queue();
			} else {
				event.reply("Could not find a user, channel, or guild with the given ID.");
				return;
			}
			event.reply("Successfully sent the message to a %s.", o instanceof User ? "user" : o instanceof TextChannel ? "channel" : "guild");
		}
	}

	@Command(category = "Owner", help = "Copies text to the system clipboard, useful for transferring text from your main PC to the VPS.", name = "copy", ownerCommand = true, arguments = "<text>")
	public static void copy(CommandEvent event) {
		if (!event.argsEmpty()) {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(event.getArgs()), null);
			event.reply("Successfully copied the given text to the system clipboard.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Owner", help = "Tells you the current version of this bot.", name = "version")
	public static void version(CommandEvent event) {
		event.reply("This bot is running Impulse **%s**, JDA **%s**, and Java **%s**.", Main.version, JDAInfo.VERSION, System.getProperty("java.version"));
	}

	@Command(category = "Owner", help = "Checks if this version of Impulse is up-to-date.", name = "checkforupdate", ownerCommand = true)
	public static void checkForUpdate(CommandEvent event) throws CommandException {
		Map data;
		try {
			data = (Map) new Gson().fromJson(Main.getHTML("https://api.github.com/repos/PlanetTeamSpeakk/Impulse/releases"), List.class).get(0);
		} catch (JsonSyntaxException | IOException e) {
			throw new CommandException("An unknown error occurred while retreiving the latest releases.", e);
		}
		String version = data.get("tag_name").toString();
		int major = Integer.parseInt(version.split("\\.")[0]);
		int minor = Integer.parseInt(version.split("\\.")[1].split("-")[0]);
		int revision = Integer.parseInt(version.split("\\.")[2].split("-")[0]);
		event.reply(major > Main.major || minor > Main.minor || revision > Main.revision ? String.format("This version of Impulse, **%s**, is outdated. The newest version is **%s**, you can download it here: https://github.com/PlanetTeamSpeakk/Impulse/releases/tag/%s.", Main.version, version, version) : String.format("This version of Impulse, **%s**, is up-to-date.", Main.version));
	}

	@Command(category = "Owner", help = "Shows you the top 10 biggest guilds this bot is in.", name = "toptenguilds")
	public static void topTenGuilds(CommandEvent event) {
		ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

			private static final long serialVersionUID = -5274770409958036168L;

			@Override
			public void runOnServer() {
				List<List<Guild>> topGuilds = new ArrayList();
				AtomicInteger shardId = new AtomicInteger(0);
				ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

					private static final long serialVersionUID = -350473131157281057L;

					@Override
					public void runOnClient() {
						ServerCustomPacket packet0 = this;
						List<Guild> guilds0 = Main.getGuilds();
						guilds0.sort((guild1, guild2) -> {
							return guild1.getMembers().size() > guild2.getMembers().size() ? -1 : guild2.getMembers().size() > guild1.getMembers().size() ? 1 : 0;
						});
						List<Guild> guilds1 = new ArrayList(guilds0.subList(0, guilds0.size() > 10 ? 10 : guilds0.size()));
						ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

							private static final long serialVersionUID = 109819427502660922L;

							@Override
							public void runOnServer() {
								ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

									private static final long serialVersionUID = -238726197669128222L;

									@Override
									public void runOnClient() {
										topGuilds.add(guilds1);
										if (shardId.get() + 1 == Main.getShardCount()) {
											List<Guild> guilds = new ArrayList();
											for (List<Guild> list : topGuilds) {
												Main.print(LogType.DEBUG, guilds);
												guilds.addAll(list);
											}
											guilds.sort((guild1, guild2) -> {
												return guild1.getMembers().size() > guild2.getMembers().size() ? -1 : guild2.getMembers().size() > guild1.getMembers().size() ? 1 : 0;
											});
											guilds = guilds.subList(0, guilds.size() > 10 ? 10 : guilds.size());
											int maxLength = 6;
											for (Guild guild : guilds)
												if (guild.getName().length() > maxLength) maxLength = guild.getName().length() + 1;
											String msg = "```\nGuild" + Main.multiplyString(" ", maxLength - 5) + "Members\n";
											for (Guild guild : guilds)
												msg += "\n" + guild.getName() + Main.multiplyString(" ", maxLength - guild.getName().length()) + guild.getMembers().size();
											event.reply(msg + "```");
											guilds.clear();
										} else ClientHandler.INSTANCE.sendPacket(new ClientCustomPacket() {

											private static final long serialVersionUID = 4134368866394222003L;

											@Override
											public void runOnServer() {
												ServerHandler.INSTANCE.sendPacket(packet0, shardId.incrementAndGet());
											}

										});
									}

								}, shardId.get());
							}

						});
					}

				}, 0);
			}

		});
	}

	@Command(category = "Owner", help = "Shows you pc information, JVM information, drive information, RAM information and CPU information.", name = "sysinfo")
	public static void sysinfo(CommandEvent event) {
		// @formatter:off
		String output = "**Host PC**:";
		output += 	"\n\tName: **" + VMManagement.getVMM().getOsName() +
					"**\n\tVersion: **" + VMManagement.getVMM().getOsVersion() +
					"**\n\tArchitecture: **" + VMManagement.getVMM().getOsArch();
		output += 	"**\n\n**JVM**:" +
					"\n\tLoaded classes: **" + VMManagement.getVMM().getLoadedClassCount() +
					"**\n\tUnloaded classes: **" + VMManagement.getVMM().getUnloadedClassCount() +
					"**\n\tTotal classes: **" + VMManagement.getVMM().getTotalClassCount() +
					"**\n\tClasses initialized: **" + VMManagement.getVMM().getInitializedClassCount() +
					"**\n\tLive threads: **" + VMManagement.getVMM().getLiveThreadCount() +
					"**\n\tDaemon threads: **" + VMManagement.getVMM().getDaemonThreadCount() +
					"**\n\tTotal threads: **" + VMManagement.getVMM().getTotalThreadCount() +
					"**\n\tPeak livethreadcount: **" + VMManagement.getVMM().getPeakThreadCount();
		output += "**\n\n**Drives**:";
		for (Path root : FileSystems.getDefault().getRootDirectories())
			try {
				FileStore store = Files.getFileStore(root);
				output += "\n\t**" + root +
						"**\n\t\tLeft: **" + Main.formatFileSize(store.getUsableSpace()).split("\\.")[0] + "." + Main.formatFileSize(store.getUsableSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(store.getUsableSpace()).split(" ")[1] +
						"**\n\t\tUsed: **" + Main.formatFileSize(store.getTotalSpace() - store.getUsableSpace()).split("\\.")[0] + "." + Main.formatFileSize(store.getTotalSpace() - store.getUsableSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(store.getTotalSpace() - store.getUsableSpace()).split(" ")[1] +
						"**\n\t\tTotal: **" + Main.formatFileSize(store.getTotalSpace()).split("\\.")[0] + "." + Main.formatFileSize(store.getTotalSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(store.getTotalSpace()).split(" ")[1] + "**";
			} catch (IOException e) {
			}
		output += 	"\n\n\tTotal left: **" + Main.formatFileSize(UsageMonitorer.getFreeSpace()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getFreeSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getFreeSpace()).split(" ")[1] +
					"**\n\tTotal used: **" + Main.formatFileSize(UsageMonitorer.getUsedSpace()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getUsedSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getUsedSpace()).split(" ")[1] +
					"**\n\tTotal: **" + Main.formatFileSize(UsageMonitorer.getTotalSpace()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getTotalSpace()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getTotalSpace()).split(" ")[1] + "**";
		output += "\n\n**RAM**:" +
				"\n\tUsed by process: **" + Main.formatFileSize(UsageMonitorer.getProcessRamUsage()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getProcessRamUsage()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getProcessRamUsage()).split(" ")[1] +
				"**\n\tAllocated to process: **" + Main.formatFileSize(UsageMonitorer.getProcessRamMax()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getProcessRamMax()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getProcessRamMax()).split(" ")[1] +
				"**\n\tUsed by host pc: **" + Main.formatFileSize(UsageMonitorer.getSystemRamUsage()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getSystemRamUsage()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getSystemRamUsage()).split(" ")[1] +
				"**\n\tRAM of host pc: **" + Main.formatFileSize(UsageMonitorer.getSystemRamMax()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getSystemRamMax()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getSystemRamMax()).split(" ")[1] +
				"**\n\tSwap used by host pc: **" + Main.formatFileSize(UsageMonitorer.getSystemSwapUsage()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getSystemSwapUsage()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getSystemSwapUsage()).split(" ")[1] +
				"**\n\tSwap of host pc: **" + Main.formatFileSize(UsageMonitorer.getSystemSwapMax()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getSystemSwapMax()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getSystemSwapMax()).split(" ")[1] +
				"**\n\tTotal RAM used by host pc: **" + Main.formatFileSize(UsageMonitorer.getTotalSystemRamUsage()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getTotalSystemRamUsage()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getTotalSystemRamUsage()).split(" ")[1] +
				"**\n\tTotal RAM of host pc: **" + Main.formatFileSize(UsageMonitorer.getTotalSystemRamMax()).split("\\.")[0] + "." + Main.formatFileSize(UsageMonitorer.getTotalSystemRamMax()).split("\\.")[1].substring(0, 3).split(" ")[0] + " " + Main.formatFileSize(UsageMonitorer.getTotalSystemRamMax()).split(" ")[1] + "**";
		output += "\n\n**CPU**:" +
				"\n\tCores: **" + UsageMonitorer.getProcessorCount() +
				"**\n\tUsed by process: **" + UsageMonitorer.getProcessCpuLoad() +
				"**\n\tUsed by system: **" + UsageMonitorer.getSystemCpuLoad() + "**";
		event.reply(output);
		// @formatter:on
	}

}
