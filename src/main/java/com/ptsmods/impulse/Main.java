package com.ptsmods.impulse;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandExecutionHook;
import com.ptsmods.impulse.miscellaneous.CommandPermissionException;
import com.ptsmods.impulse.miscellaneous.EventHandler;
import com.ptsmods.impulse.miscellaneous.ImpulseSM;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.ConsoleColors;
import com.ptsmods.impulse.utils.Dashboard;
import com.ptsmods.impulse.utils.DataIO;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.EventListenerManager;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.RoleImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.entities.impl.VoiceChannelImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import sun.reflect.Reflection;

public class Main {

	public static final String version = "1.3.2-stable";
	public static final Object nil = null; // fucking retarded name, imo.
	public static final Date started = new Date();
	public static final Map<String, String> apiKeys = new HashMap<>();
	public static final Thread mainThread = Thread.currentThread();
	public static final List<Permission> defaultPermissions = Collections.unmodifiableList(getDefaultPermissions());
	public static final Permission[] defaultPermissionsArray = defaultPermissions.toArray(new Permission[0]);
	private static final BitlyClient bitlyClient = new BitlyClient(Main.apiKeys.get("bitly"));
	private static boolean done = false;
	private static final ThreadPoolExecutor commandsExecutor = newTPE();
	private static final ThreadPoolExecutor miscellaneousExecutor = newTPE();
	private static final String osName = System.getProperty("os.name");
	private static final boolean isWindows = osName.toLowerCase().contains("windows");
	private static final boolean isMac = osName.toLowerCase().contains("mac");
	private static final boolean isUnix = osName.toLowerCase().contains("nix") || osName.toLowerCase().contains("nux") || osName.toLowerCase().contains("aix");
	private static final boolean isSolaris = osName.toLowerCase().contains("sunos");
	private static Map<String, Integer> commandIndex = new HashMap();
	private static List<CommandExecutionHook> commandHooks = new ArrayList<>();
	private static List<JDA> shards = new ArrayList<>();
	private static boolean devMode = false;
	private static boolean eclipse = false;
	private static boolean headless = false;
	private static User owner = null;
	private static List<Method> commands = new ArrayList<>();
	private static List<Method> subcommands = new ArrayList<>();
	private static List<Message> messages = new ArrayList<>();
	private static Map<String, List<Method>> linkedSubcommands = new HashMap();
	private static EventHandler eventHandler = new EventHandler();
	private static Map<String, Map<String, Long>> cooldowns = new HashMap();
	private static List<String> categories = new ArrayList();

	static {
		try {
			System.setOut(new SystemOutPrintStream());
		} catch (FileNotFoundException e) {}
		miscellaneousExecutor.allowCoreThreadTimeOut(true);
		apiKeys.put("bitly", "dd800abec74d5b12906b754c630cdf1451aea9e0");
	}

	public static void main(String[] args) {
		List<String> argsList = Lists.newArrayList(args);
		devMode = argsList.contains("-devMode");
		eclipse = argsList.contains("-eclipse");
		headless = argsList.contains("-headless") || argsList.contains("-noGui");
		try {
			main0(args);
		} catch (Throwable e) {
			try {
				print(LogType.ERROR, "An unknown error occured, please contact PlanetTeamSpeak#4157.");
			} catch (Throwable e1) {
				System.err.println("An unknown error occurred, please contact PlanetTeamSpeak#4157");
			}
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void main0(String[] args) {
		if (!headless)
			try {
				MainGUI.initialize();
			} catch (IllegalAccessException e1) {
				print(LogType.DEBUG, "Could not initialize the Main GUI, it seems to have already been initialized.");
			}
		try {
			Dashboard.initialize();
		} catch (IOException e) {
			print(LogType.ERROR, "The webserver could not be initialized, are you sure port 61192 is free?");
		}
		if (!MainGUI.isThief()) { // user tried to steal Impulse Java Edition as their own idea.
			Locale.setDefault(Locale.US);
			System.setSecurityManager(new ImpulseSM());
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (devMode()) print(LogType.DEBUG, "Bot shutting down, deleting all temporary files.");
				else print(LogType.DEBUG, "Shutting down...");
				int counter = 0;
				if (new File("data/tmp").exists() && new File("data/tmp").isDirectory())
					for (File file : new File("data/tmp").listFiles())
						try {
							file.delete();
							counter += 1;
						} catch (Throwable e) {}
				else new File("data/tmp/").mkdirs();
				print(LogType.DEBUG, String.format("Temporary files deleted, deleted %s file%s.", counter, counter == 1 ? "" : "s"));
				try {
					new File("data/tmp/info.txt").createNewFile();
				} catch (Throwable e) {}
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(new FileWriter("data/tmp/info.txt"));
				} catch (Throwable e) {
					return;
				} finally {
					writer.println("This directory is meant for temporary files which are created when making new JSON files.");
					writer.println("This directory is cleared on bot shutdown.");
					writer.println("DO NOT STORE FILES IN THIS DIRECTORY!");
					IOUtils.closeQuietly(writer);
				}
				for (Thread thread : Thread.getAllStackTraces().keySet())
					if (!thread.equals(mainThread)) thread.interrupt();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}));
			if (devMode) print(LogType.DEBUG, "Developer mode is turned on, this means that errors will NOT be sent privately to the owner of the bot and only the owner can use commands.");
			try {
				if (!new File("data/").isDirectory())
					new File("data/").mkdirs();
				if (Config.get("token") == null || Config.get("prefix") == null || Config.get("ownerId") == null || Config.get("shards") == null) {
					Config.addComment("Config file created by Impulse Discord Bot written by PlanetTeamSpeak.");
					Config.addComment("The bot token used to log in, example: MzIzMTc2ODk2NTAwMzM0NTk0.DLfRfw.zZ7V1ljky12M5sKEVUZBAzwYUbo.");
					Config.put("token", "");
					Config.addComment("The prefix used to execute commands, this CANNOT be 2 or more slashes!");
					Config.put("prefix", "\\");
					Config.addComment("The ID of the owner, this should be your id as anyone who has this can do anything with your bot in any server.");
					Config.put("ownerId", "");
					Config.addComment("Any potential co-owners, these users have exactly the same permissions as the owner. You can divide the IDs with a semi-colon (;).");
					Config.put("coOwnerIds", "");
					Config.addComment("The amount of shards you want, for every shard the startup time takes at least 5 more seconds, this is due to rate limiting.");
					Config.addComment("If you don't know what shards are, maybe you should learn some stuff about computers before making your own Discord bot.");
					Config.put("shards", "1");
					Config.addComment("The key used to send bot stats to https://carbinotex.net");
					Config.put("carbonitexKey", "");
					Config.addComment("The key used to send bot stats to https://discordbots.org");
					Config.put("discordBotListKey", "");
					Config.addComment("The key used to send bot stats to https://bots.discord.pw");
					Config.put("discordBotsKey", "");
					print(LogType.WARN, "The config hasn't been changed yet, please go to the settings tab or open config.cfg and change the variables.");
					return;
				} else if (Config.get("token").isEmpty() || Config.get("ownerId").isEmpty() || Config.get("prefix").isEmpty() || Config.get("shards").isEmpty()) {
					print(LogType.WARN, "The config hasn't been changed yet, please go to the settings tab or open config.cfg and change the variables.");
					return;
				}
				int shardAmount = 1;
				if (!isInteger(Config.get("shards")))
					print(LogType.WARN, "The value of key 'shards' isn't an integer, expecting 1.");
				else shardAmount = Integer.parseInt(Config.get("shards"));
				if (shardAmount < 1) shardAmount = 1;
				print(LogType.INFO, "Loading commands...");
				for (Class clazz : new Reflections("com.ptsmods.impulse.commands", new SubTypesScanner(false)).getSubTypesOf(Object.class)) {
					EventListenerManager.registerListenersFromClass(clazz);
					for (Method method : getMethods(clazz))
						if (method.isAnnotationPresent(Command.class)) {
							if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class))) {
								commands.add(method);
								if (!categories.contains(method.getAnnotation(Command.class).category())) categories.add(method.getAnnotation(Command.class).category());
								commandIndex.put(method.getAnnotation(Command.class).name(), commands.size()-1);
							} else print(LogType.DEBUG, "Found a command that requires more than only a CommandEvent.", method.toString());
						} else if (method.isAnnotationPresent(Subcommand.class))
							if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class)))
								try {
									Class.forName(joinCustomChar(".", removeArg(method.getAnnotation(Subcommand.class).parent().split("\\."), method.getAnnotation(Subcommand.class).parent().split("\\.").length-1)), false, ClassLoader.getSystemClassLoader());
									subcommands.add(method);
								} catch (ClassNotFoundException e) {
									print(LogType.DEBUG, "Found a subcommand that has an invalid parent.", method.toString());
								}
							else print(LogType.DEBUG, "Found a command that requires more than only a CommandEvent.", method.toString());
				}
				for (Method subcommand : subcommands) {
					Method parentCommand = getParentCommand(subcommand.getAnnotation(Subcommand.class));
					if (!linkedSubcommands.containsKey(parentCommand.toString())) linkedSubcommands.put(parentCommand.toString(), Lists.newArrayList(subcommand));
					else ((List) linkedSubcommands.get(parentCommand.toString())).add(subcommand);
				}
				for (Method command : commands)
					if (!linkedSubcommands.containsKey(command.toString())) linkedSubcommands.put(command.toString(), new ArrayList());
				print(LogType.INFO, commands.size() + " commands and " + subcommands.size() + " subcommands loaded, logging in...");
				ShardedRateLimiter rateLimiter = new ShardedRateLimiter();
				eventHandler = new EventHandler();
				for (int i : range(shardAmount)) {
					JDA shard = new JDABuilder(AccountType.BOT)
							.setToken(Config.get("token"))
							.addEventListener(eventHandler)
							.useSharding(shards.size(), shardAmount)
							.setReconnectQueue(new SessionReconnectQueue())
							.setShardedRateLimiter(rateLimiter)
							.setGame(Game.of("Starting..."))
							.buildBlocking();
					shards.add(shard);
					if (i != shardAmount-1) {
						print(LogType.INFO, "Started shard " + i + ", waiting 5 seconds before starting shard " + (i+1) + "/" + shardAmount + ".");
						Thread.sleep(5000);
					}
				}
				owner = getUserById(Config.get("ownerId"));
				if (owner == null) {
					print(LogType.WARN, "Could not find a user with the given owner ID.");
					shutdown(2);
				}
			} catch (LoginException e) {
				print(LogType.ERROR, "The bot could not log in with the given token, please change the token in config.cfg.");
				return;
			} catch (RateLimitedException e) {
				print(LogType.ERROR, "The bot has been rate limited, please try restarting.");
				return;
			} catch (IllegalStateException e) {
				print(LogType.ERROR, "Timeout, please restart when you have a solid internet connection.");
				return;
			} catch (InterruptedException e) {
				print(LogType.WARN, "The main thread was interrupted, the bot is now being shutdown.");
				return;
			}
			print(LogType.INFO, String.format("Succesfully logged in as %s, took %s milliseconds. Owner = %s, prefix = %s.",
					str(shards.get(0).getSelfUser()),
					System.currentTimeMillis() - started.getTime(),
					str(owner),
					Config.get("prefix")));
			for (JDA shard : shards)
				shard.getPresence().setGame(Game.of(devMode() ? "DEVELOPER MODE" : "try " + Config.get("prefix") + "help!"));
			done = true;
			List<Class> passedClasses = new ArrayList();
			for (Method command : commands) {
				Class clazz = command.getDeclaringClass();
				if (!passedClasses.contains(clazz)) {
					passedClasses.add(clazz);
					if (getMethod(clazz, "onFullyBooted") != null && getMethod(clazz, "onFullyBooted").isAnnotationPresent(SubscribeEvent.class))
						try {
							getMethod(clazz, "onFullyBooted").invoke(null);
						} catch (InvocationTargetException e) {
							e.getCause().printStackTrace();
						} catch (IllegalAccessException | IllegalArgumentException e) {
							e.printStackTrace();
						}
				}
			}
		}
	}

	public static <T> boolean print(String threadName, LogType logType, T... message) {
		if (logType == LogType.DEBUG && !devMode()) return true;
		String[] args = new String[message.length];
		for (int x = 0; x < message.length; x++)
			if (message[x] == null) args[x] = "null";
			else args[x] = message[x].toString();
		String output = String.format(ConsoleColors.RESET + "%s[%s] [%s/%s]: %s\n" + ConsoleColors.RESET, logType.color, getFormattedTime(), threadName, logType.name, join(args));
		if (!eclipse) ConsoleColors.print(output);
		else System.out.print(output); // I use a plugin so Eclipse can support ANSI colors, when using ConsoleColors in Eclipse there are no colors.
		MainGUI.logLine(ConsoleColors.getCleanString(output).trim()); // trimming the cleaned String instead of the dirty one cuz cleaning the dirty one removes the null character so it's no longer an ANSI color and thus isn't cleaned.
		return true;
	}

	public static String getFormattedTime() {
		return joinCustomChar(":", "" + (LocalDateTime.now().getHour() < 10 ? "0" : "") + LocalDateTime.now().getHour(),
				"" + (LocalDateTime.now().getMinute() < 10 ? "0" : "") + LocalDateTime.now().getMinute(),
				"" + (LocalDateTime.now().getSecond() < 10 ? "0" : "") + LocalDateTime.now().getSecond());
	}

	public static String getFormattedDate() {
		return joinCustomChar("-", LocalDateTime.now().getDayOfMonth(), LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear());
	}

	public static <T> boolean print(LogType logType, T... message) {
		return print(shards.size() > 0 ? shards.get(0).getSelfUser().getName() : "Discord Bot", logType, message);
	}

	public static String join(List<String> list) {
		return joinCustomChar(" ", list.toArray(new String[0]));
	}

	public static String join(String... stringArray) {
		return joinCustomChar(" ", stringArray);
	}

	public static String joinCustomChar(String character, List<String> list) {
		return joinCustomChar(character, list.toArray(new String[0]));
	}

	public static String joinCustomChar(String character, String... stringArray) {
		return joinCustomChar(character, (Object[]) stringArray);
	}

	public static String joinCustomChar(String character, Object... array) {
		String data = "";
		for (int x = 0; x < array.length; x++)
			data += array[x] + (x+1 == array.length ? "" : character);
		return data.trim();
	}

	public static void shutdown(int status) {
		if (status != 2) {
			for (JDA shard : shards)
				if (status == 0) shard.shutdown();
				else shard.shutdownNow();
			commandsExecutor.shutdown();
			miscellaneousExecutor.shutdown();
			System.exit(status);
		} else
			for (JDA shard : shards)
				shard.shutdown();
	}

	public static Integer[] range(int range) {
		Integer[] array = new Integer[range];
		for (int x = 0; x < array.length; x++)
			array[x] = x;
		return array;
	}

	public static Double[] range(double range) {
		Double[] array = new Double[(int) range];
		for (double x = 0; x < array.length; x++)
			array[(int) x] = x;
		return array;
	}

	public static void createDirectoryIfNotExisting(String file) {
		if (!new File(file).isDirectory()) new File(file).mkdirs();
	}

	public static void createFileIfNotExisting(String file) throws IOException {
		if (!new File(file).exists()) new File(file).createNewFile();
	}

	public static User getUserFromInput(Message input) {
		try {
			return !input.getMentionedUsers().isEmpty() ? input.getMentionedUsers().get(0) : input.getContent().startsWith(Config.get("prefix")) ? input.getGuild().getMembersByName(getUsernameFirstArg(input), true).get(0).getUser() : null;
		} catch (Throwable e) {return null;}
	}

	public static Member getMemberFromInput(Message input) {
		return getUserFromInput(input) == null ? null : input.getGuild().getMember(getUserFromInput(input));
	}

	public static String getUsernameFirstArg(Message input) {
		return getUsernameFromArgs(removeArg(input.getContent().split(" "), 0), input);
	}

	public static String getUsernameSecondArg(Message input) {
		return getUsernameFromArgs(removeArgs(input.getContent().split(" "), 0, 1), input);
	}

	private static String getUsernameFromArgs(String[] args, Message input) {
		String username = "";
		for (String arg : args) {
			username += arg + " ";
			if (!input.getGuild().getMembersByName(username.trim(), true).isEmpty()) return username.trim();
		}
		return null;
	}

	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event, Method cmd, String extraMsg) {
		if (cmd.isAnnotationPresent(Command.class)) {
			Command command = cmd.getAnnotation(Command.class);
			String cmdName = command.name() + (command.arguments() == null || command.arguments().isEmpty() ? "" : " " + command.arguments());
			String cmdHelp = command.help() == null || command.help().isEmpty() ? "" : command.help().replaceAll("\\[p\\]", getPrefix(event.getGuild()).startsWith("\\") ? "\\\\" : getPrefix(event.getGuild()));
			String cmdSubcommands = "";
			if (!getSubcommands(cmd).isEmpty()) {
				cmdSubcommands = "**Subcommands**\n\t";
				List<String> subcommandNames = new ArrayList();
				for (Method scmdMethod : getSubcommands(cmd))
					subcommandNames.add(scmdMethod.getAnnotation(Subcommand.class).name());
				for (String scmdName : Main.sort(subcommandNames))
					for (Method scommand : getSubcommands(cmd)) {
						Subcommand scmd = scommand.getAnnotation(Subcommand.class);
						if (!scmdName.equals(scmd.name())) continue;
						if (!event.isOwner() && !event.isCoOwner())
							if (scmd.hidden() ||
									scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) ||
									event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions()))
								continue;
						cmdSubcommands += String.format("**%s**", scmd.name()) + (scmd.help() == null || scmd.help().isEmpty() ? "" : ": " + scmd.help().split("\n")[0]) + "\n\t";
					}
				cmdSubcommands = cmdSubcommands.trim();
			}
			return channel.sendMessage(String.format("**%s**%s%s%s",
					Config.get("prefix") + cmdName,
					cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp,
							extraMsg == null || extraMsg.isEmpty() ? "" : (cmdHelp.isEmpty() ? ":" : "") + "\n\n" + extraMsg,
									cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands)).complete();
		} else if (cmd.isAnnotationPresent(Subcommand.class)) {
			Subcommand command = cmd.getAnnotation(Subcommand.class);
			String cmdName = command.name() + (command.arguments() == null || command.arguments().isEmpty() ? "" : " " + command.arguments());
			String cmdHelp = command.help() == null || command.help().isEmpty() ? "" : command.help().replaceAll("\\[p\\]", getPrefix(event.getGuild()).startsWith("\\") ? "\\\\" : getPrefix(event.getGuild()));
			String cmdSubcommands = "";
			if (getSubcommands(cmd).size() != 0) {
				cmdSubcommands = "**Subcommands**\n\t";
				List<String> subcommandNames = new ArrayList();
				for (Method scmdMethod : getSubcommands(cmd))
					subcommandNames.add(scmdMethod.getAnnotation(Subcommand.class).name());
				for (String scmdName : Main.sort(subcommandNames))
					for (Method scmdMethod : getSubcommands(cmd)) {
						Subcommand scmd = scmdMethod.getAnnotation(Subcommand.class);
						if (!scmdName.equals(scmd.name())) continue;
						if (!event.isOwner() && !event.isCoOwner())
							if (scmd.hidden() ||
									scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) ||
									event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions()))
								continue;
						cmdSubcommands += String.format("**%s**", scmd.name()) + (scmd.help() == null || scmd.help().isEmpty() ? "" : ": " + scmd.help().split("\n")[0]) + "\n\t";
					}
				cmdSubcommands = cmdSubcommands.trim();
			}
			while (getParentCommand(command) != null)
				if (getParentCommand(command).isAnnotationPresent(Command.class)) {
					cmdName = getParentCommand(command).getAnnotation(Command.class).name() + " " + cmdName;
					break;
				} else {
					command = getParentCommand(command).getAnnotation(Subcommand.class);
					cmdName = command.name() + " " + cmdName;
				}
			return channel.sendMessage(String.format("**%s**%s%s%s",
					Config.get("prefix") + cmdName,
					cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp,
							extraMsg == null || extraMsg.isEmpty() ? "" : (cmdHelp.isEmpty() ? ":" : "") + "\n\n" + extraMsg,
									cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands)).complete();
		}
		return null;
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd) {
		return sendCommandHelp(event.getChannel(), event, cmd, null);
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(event.getChannel(), event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(CommandEvent event) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(channel, event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event) {
		return sendCommandHelp(channel, event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, cmd, extraMsg);
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(event.getChannel(), event, event.getCommand(), extraMsg)}
	 */
	public static Message sendCommandHelp(CommandEvent event, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), extraMsg);
	}

	/**
	 * Equivalent to calling {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String) Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event, String extraMsg) {
		return sendCommandHelp(channel, event, event.getCommand(), extraMsg);
	}

	public static Method getParentCommand(Subcommand child) {
		try {
			// format should be : package.class.method
			String parent = child.parent();
			String methodName = parent.split("\\.")[parent.split("\\.").length-1];
			// this should make com.ptsmods.impulse.commands.Economy from com.ptsmods.impulse.commands.Economy.bank
			Class clazz = Class.forName(joinCustomChar(".", removeArg(parent.split("\\."), parent.split("\\.").length-1)), false, ClassLoader.getSystemClassLoader());
			return clazz.getMethod(methodName, CommandEvent.class);
		} catch (Exception e) { // should not be possible as it's already checked in Main#main(String[] args).
			return null;
		}
	}

	public static Method getAbsoluteParentCommand(Subcommand child) {
		while (getParentCommand(child) != null && getParentCommand(child).isAnnotationPresent(Subcommand.class))
			child = getParentCommand(child).getAnnotation(Subcommand.class);
		return getParentCommand(child);
	}

	public static Role getRoleByName(Guild guild, String name, boolean ignoreCase) {
		List<Role> roles = guild.getRolesByName(name, ignoreCase);
		return roles.size() == 0 ? null : roles.get(0);
	}

	public static void executeCommand(MessageReceivedEvent event) {
		commandsExecutor.execute(() -> {
			try {
				String prefix = Main.getPrefix(event.getGuild());
				String[] parts = null;
				String rawContent = event.getMessage().getRawContent();
				if (rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
					parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+",2), 2);
				if (parts != null)
					if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
						String name = parts[0];
						String args = parts[1] == null ? "" : parts[1];
						int i = Main.getCommandIndex().getOrDefault(name.toLowerCase(), -1);
						if (i != -1) {
							Method command = Main.getCommands().get(i);
							while (args.split(" ").length != 0 && !Main.getSubcommands(command).isEmpty()) {
								boolean found = false;
								for (Method subcommand : Main.getSubcommands(command))
									if (subcommand.getAnnotation(Subcommand.class).name().equals(args.split(" ")[0])) {
										command = subcommand;
										args = Main.join(Main.removeArg(args.split(" "), 0));
										found = true;
										break;
									}
								if (!found) break;
							}
							Permission[] permissions = {};
							Permission[] botPermissions = {};
							boolean guildOnly = false;
							boolean serverOwnerCommand = false;
							boolean ownerCommand = false;
							boolean sendTyping = true;
							double cooldown = 1D;
							if (command.isAnnotationPresent(Command.class)) {
								Command annotation = command.getAnnotation(Command.class);
								annotation.name();
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								serverOwnerCommand = annotation.serverOwnerCommand();
								ownerCommand = annotation.ownerCommand();
								cooldown = annotation.cooldown();
								sendTyping = annotation.sendTyping();
							} else if (command.isAnnotationPresent(Subcommand.class)) {
								Subcommand annotation = command.getAnnotation(Subcommand.class);
								annotation.name();
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								serverOwnerCommand = annotation.serverOwnerCommand();
								ownerCommand = annotation.ownerCommand();
								cooldown = annotation.cooldown();
								sendTyping = annotation.sendTyping();
							}
							String errorMsg = "";
							if (!event.getAuthor().getId().equals(Main.getOwner().getId()))
								if (cooldowns.getOrDefault(event.getAuthor().getId(), new HashMap()).containsKey(command.toString()) && System.currentTimeMillis()-cooldowns.get(event.getAuthor().getId()).get(command.toString()) < cooldown*1000)
									errorMsg = "You're still on cooldown, please try again in " + Main.formatMillis((long) (cooldown * 1000 - (System.currentTimeMillis()-cooldowns.get(event.getAuthor().getId()).get(command.toString()))), true, true, true, true, true, false) + ".";
								else if (event.getGuild() == null && guildOnly)
									errorMsg = "That command cannot be used in direct messages.";
								else if (serverOwnerCommand && event.getGuild() != null && event.getAuthor().getId().equals(event.getGuild().getOwner().getUser().getId()))
									errorMsg = "That command can only be used by this server's owner.";
								else if (ownerCommand)
									errorMsg = "That command can only be used by my owner.";
								else if (event.getMember() != null && !event.getMember().hasPermission(permissions)) {
									List<String> nonPresentPerms = new ArrayList();
									for (Permission perm : permissions)
										if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
									errorMsg = "You need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to use that.";
								} else if (event.getGuild() != null && !event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(botPermissions)) {
									List<String> nonPresentPerms = new ArrayList();
									for (Permission perm : permissions)
										if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
									errorMsg = "I need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to do that.";
								}
							if (!errorMsg.isEmpty())
								event.getChannel().sendMessage(errorMsg).queue(RestAction.DEFAULT_SUCCESS, t -> {
									if (t instanceof InsufficientPermissionException) sendPrivateMessage(event.getAuthor(), "I cannot parse the command as I don't have permissions to talk in the channel you ran the command in.");
									else t.printStackTrace();
								});
							else {
								if (sendTyping) event.getChannel().sendTyping().complete();
								CommandEvent cevent = new CommandEvent(event, args, command);
								for (CommandExecutionHook hook : Main.getCommandHooks()) // these are useful for e.g., permissions, blacklists, logging, etc.
									try {
										hook.run(cevent);
									} catch (CommandPermissionException e) {
										if (e.getMessage() != null && !e.getMessage().isEmpty()) event.getChannel().sendMessage(e.getMessage()).queue();
										return;
									}
								Object obj = null;
								try {
									obj = command.getDeclaringClass().newInstance(); // so commands that aren't static still work.
								} catch (Throwable e) {}
								command.setAccessible(true);
								try {
									command.invoke(obj, cevent);
									if (!Main.getOwner().getId().equals(event.getAuthor().getId())) {
										Map userCooldowns = cooldowns.getOrDefault(event.getAuthor().getId(), new HashMap());
										userCooldowns.put(command.toString(), System.currentTimeMillis());
										cooldowns.put(event.getAuthor().getId(), userCooldowns);
									}
								} catch (InvocationTargetException e) {
									sendStackTrace(e.getCause(), event);
								}
							}
						}
					}
			} catch (Throwable e) {
				sendStackTrace(e, event);
			}
		});
	}

	private static void sendStackTrace(Throwable e, MessageReceivedEvent event) {
		e.printStackTrace();
		StackTraceElement stElement = null;
		for (StackTraceElement element : e.getStackTrace())
			if (element.getFileName() != null && element.getClassName().startsWith("com.ptsmods.impulse.commands")) stElement = element;
		event.getChannel().sendMessageFormat("A `%s` exception was thrown at line %s in %s while parsing the command%s.%s",
				e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), e.getMessage() != null ? String.format(": `%s`", e.getMessage()) : "", Main.devMode() ? "" : String.format("\nMy owner, %s, has been informed.", Main.getOwner().getAsMention())).queue();
		if (!Main.devMode())
			Main.sendPrivateMessage(Main.getOwner(), String.format("A `%s` exception was thrown at line %s in %s while parsing the message `%s`. Stacktrace:\n```java\n%s```",
					e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), event.getMessage().getContent(), Main.generateStackTrace(e)));
	}

	public static void runAsynchronously(Runnable runnable) {
		miscellaneousExecutor.execute(runnable);
	}

	public static void runAsynchronously(Object obj, Method method, Object... args) {
		runAsynchronously(() -> {
			try {
				method.invoke(obj, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean isShort(String s) {
		try {
			Short.parseShort(s);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean isLong(String s) {
		try {
			Long.parseLong(s);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean isFloat(String s) {
		try {
			Float.parseFloat(s);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean headless() {
		return headless;
	}

	public static boolean devMode() {
		if (devMode && shards.size() > 0 && !shards.get(0).getPresence().getGame().equals(Game.of("DEVELOPER MODE")) && done()) setGame("DEVELOPER MODE");
		return devMode;
	}

	public static void devMode(boolean bool) {
		devMode = bool;
		devMode();
	}

	public static UserImpl getOwner() {
		return (UserImpl) owner;
	}

	public static void sendPrivateMessage(User user, String msg, Object... args) {
		user.openPrivateChannel().complete().sendMessageFormat(msg, args).queue();
	}

	public static <T> T[] removeArg(T[] args, int arg) {
		List<Object> data = new ArrayList<>();
		for (int x = 0; x < args.length; x++)
			if (x != arg) data.add(args[x]);
		return (T[]) Arrays.copyOf(data.toArray(), data.size(), args.getClass());
	}

	public static Object[] removeArgs(Object[] args, Integer... arg) {
		for (int i : arg)
			args = removeArg(args, i);
		return args;
	}

	public static String[] removeArgs(String[] args, Integer... arg) {
		return castStringArray(removeArgs((Object[]) args, arg));
	}

	public static String[] castStringArray(Object[] array) {
		return castArray(array, new String[0]);
	}

	public static Integer[] castIntArray(Object[] array) {
		return castArray(array, new Integer[0]);
	}

	public static Double[] castDoubleArray(Object[] array) {
		return castArray(array, new Double[0]);
	}

	public static <T, U> T[] castArray(U[] original, T[] newType) {
		return (T[]) Arrays.copyOf(original, original.length, newType.getClass());
	}

	public static List<Method> getCommands() {
		return commands;
	}

	public static List<String> getCommandNames() {
		List<String> names = new ArrayList<>();
		for (Method cmd : commands)
			names.add(cmd.getAnnotation(Command.class).name());
		return names;
	}

	public static List<Method> getSubcommands() {
		return subcommands;
	}

	public static List<String> getSubcommandNames() {
		List<String> names = new ArrayList<>();
		for (Method cmd : subcommands)
			names.add(cmd.getAnnotation(Command.class).name());
		return names;
	}

	public static Method getCommandByName(String name) {
		for (Method cmd : commands)
			if (cmd.getAnnotation(Command.class).name().equals(name)) return cmd;
		return null;
	}

	public static void mute(Member member) {
		for (Channel channel : Main.getAllChannels(member.getGuild()))
			Main.getPermissionOverride(member, channel).getManagerUpdatable().deny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_SPEAK).update().queue();
	}

	public static void unmute(Member member) {
		for (Channel channel : Main.getAllChannels(member.getGuild()))
			if (channel.getPermissionOverride(member) != null) channel.getPermissionOverride(member).delete().queue();
	}

	@Nullable
	public static Message waitForInput(Member author, MessageChannel channel, int timeoutMillis) {
		long currentMillis = System.currentTimeMillis();
		long currentSeconds = currentMillis / 1000;
		while (true) {
			if (messages.isEmpty()) continue;
			Message lastMsg = messages.get(messages.size()-1);
			if (lastMsg.getCreationTime().toEpochSecond() > currentSeconds && lastMsg.getAuthor().getIdLong() == author.getUser().getIdLong() && (lastMsg.getGuild() == null || lastMsg.getGuild().getIdLong() == author.getGuild().getIdLong()) && lastMsg.getChannel().getIdLong() == channel.getIdLong()) return lastMsg;
			else if (System.currentTimeMillis()-currentMillis >= timeoutMillis) return null;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}
	}

	@Nullable
	public static Message waitForInput(MessageChannel channel, int timeoutMillis) {
		long currentMillis = System.currentTimeMillis();
		long currentSeconds = currentMillis / 1000;
		while (true) {
			if (messages.isEmpty()) continue;
			Message lastMsg = messages.get(messages.size()-1);
			if (lastMsg.getCreationTime().toEpochSecond() > currentSeconds && lastMsg.getChannel().getId().equals(channel.getId())) return lastMsg;
			else if (System.currentTimeMillis()-currentMillis >= timeoutMillis) return null;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}
	}

	public static String formatMillis(long millis, boolean includeDays, boolean includeHours, boolean includeMinutes, boolean includeSeconds, boolean includeMillis, boolean sortLogical) {
		String[] output = new String[4];
		long days = includeDays ? TimeUnit.MILLISECONDS.toDays(millis) : 0;
		long hours = includeHours ? TimeUnit.MILLISECONDS.toHours(millis) % 24 : 0;
		long minutes = includeMinutes ? TimeUnit.MILLISECONDS.toMinutes(millis) % 60 : 0;
		long seconds = includeSeconds ? TimeUnit.MILLISECONDS.toSeconds(millis) % 60 : 0;
		millis = includeMillis ? millis % 1000 : 0;
		if (days != 0) output[0] = "**" + days + "** day" + (days != 1 ? "s" : "");
		if (hours != 0) output[1] = "**" + hours + "** hour" + (hours != 1 ? "s" : "");
		if (minutes != 0) output[2] = "**" + minutes + "** minute" + (minutes != 1 ? "s" : "");
		if (seconds != 0 && millis != 0 && sortLogical) output[3] = "**" + seconds + "." + millis + "** seconds";
		else if (seconds != 0) output[3] = "**" + seconds + "** second" + (seconds != 1 ? "s" : "");
		else if (millis != 0) {
			output = Arrays.copyOf(output, 5);
			output[4] = "**" + millis + "** millisecond" + (millis != 1 ? "s" : "");
		}
		while (Lists.newArrayList(output).contains(null))
			for (int x = 0; x < output.length; x++)
				if (output[x] == null) output = removeArg(output, x);
		return joinNiceString(output).isEmpty() ? "**0** seconds" : joinNiceString(output);
	}

	/**
	 * This is equivalent to {@link com.ptsmods.impulse.Main#formatMillis(long, boolean, boolean, boolean, boolean, boolean, boolean) formatMillis(millis, true, true, true, true, false, false)}.
	 */
	public static String formatMillis(long millis) {
		return formatMillis(millis, true, true, true, true, false, false);
	}

	public static List<Guild> getGuilds() {
		List<Guild> guilds = new ArrayList<>();
		for (JDA shard : shards)
			guilds.addAll(shard.getGuilds());
		return guilds;
	}

	public static List<String> getGuildNames() {
		List<String> guilds = new ArrayList<>();
		for (Guild guild : getGuilds())
			guilds.add(guild.getName());
		return guilds;
	}

	public static List<User> getUsers() {
		List<User> users = new ArrayList<>();
		for (JDA shard : shards)
			users.addAll(shard.getUsers());
		return users;
	}

	public static List<String> getUserNames() {
		List<String> users = new ArrayList<>();
		for (User user : getUsers())
			users.add(user.getName());
		return users;
	}

	public static List<PrivateChannel> getPrivateChannels() {
		List<PrivateChannel> privateChannels = new ArrayList<>();
		for (JDA shard : shards)
			privateChannels.addAll(shard.getPrivateChannels());
		return privateChannels;
	}

	public static List<String> getPrivateChannelNames() {
		List<String> privateChannels = new ArrayList<>();
		for (PrivateChannel privateChannel : getPrivateChannels())
			privateChannels.add(privateChannel.getName());
		return privateChannels;
	}

	public static List<Group> getGroups() {
		List<Group> groups = new ArrayList<>();
		for (JDA shard : shards)
			groups.addAll(shard.asClient().getGroups());
		return groups;
	}

	public static List<String> getGroupNames() {
		List<String> groups = new ArrayList<>();
		for (Group group : getGroups())
			groups.add(group.getName());
		return groups;
	}

	public static String flipString(String text) {
		text = text.toLowerCase();
		String result = "";

		for (int i = text.length() - 1; i >= 0; --i)
			result += flipChar(text.charAt(i));

		return result;
	}

	public static char flipChar(char c) {
		switch (c) {
		case 'a': return '\u0250';
		case 'b': return 'q';
		case 'c': return '\u0254';
		case 'd': return 'p';
		case 'e': return '\u01DD';
		case 'f': return '\u025F';
		case 'g': return '\u0183';
		case 'h': return '\u0265';
		case 'i': return '\u0131'; // or \u0323
		case 'j': return '\u0638';
		case 'k': return '\u029E';
		case 'l': return '\u05DF';
		case 'm': return '\u026F';
		case 'n': return 'u';
		case 'o': return 'o';
		case 'p': return 'd';
		case 'q': return 'b';
		case 'r': return '\u0279';
		case 's': return 's';
		case 't': return '\u0287';
		case 'u': return 'n';
		case 'v': return '\u028C';
		case 'w': return '\u028D';
		case 'x': return 'x';
		case 'y': return '\u028E';
		case 'z': return 'z';
		case '[': return ']';
		case ']': return '[';
		case '(': return ')';
		case ')': return '(';
		case '{': return '}';
		case '}': return '{';
		case '?': return '\u00BF';
		case '\u00BF': return '?';
		case '!': return '\u00A1';
		case '\'': return ',';
		case ',': return '\'';
		default: return c;
		}
	}

	public static boolean addReceivedMessage(Message message) {
		return messages.add(message);
	}

	public static List<Message> getReceivedMessages() {
		return new ArrayList<>(messages);
	}

	/**
	 * Just don't use it.
	 * Use {@link Guild#getMembersByName(String, boolean)} instead.
	 */
	@Deprecated
	public static User getUserByName(String name) {
		User user = null;
		for (JDA shard : shards)
			for (User user1 : shard.getUsers()) if (user1.getName().equals(name)) {
				user = user1;
				break;
			}
		return user;
	}

	public static User getUserById(String id) {
		User user = null;
		for (JDA shard : shards)
			for (User user1 : shard.getUsers()) if (user1.getId().equals(id)) {
				user = user1;
				break;
			}
		return user;
	}

	public static User getUserById(long id) {
		return getUserById(Long.toString(id));
	}

	public static String formatFileSize(float bytes) {
		return Downloader.formatFileSize(bytes);
	}

	public static String getHTML(String url) throws IOException {
		StringBuilder result = new StringBuilder();
		URL URL = new URL(url);
		URLConnection connection = URL.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null)
			result.append(line);
		rd.close();
		return result.toString();
	}

	public static String getCleanString(String dirtyString) {
		return dirtyString.replaceAll("(\\r)+", "").replaceAll("\\\\\"", "").trim();
	}

	public static <T> String joinNiceString(List<T> list) {
		String output = "";
		for (int x = 0; x < list.size(); x++)
			if (x+1 == list.size()) output += list.get(x).toString();
			else if (x+2 != list.size()) output += list.get(x).toString() + ", ";
			else output += list.get(x).toString() + ", and ";
		return output.trim();
	}

	public static <T> String joinNiceString(T... array) {
		return joinNiceString(Lists.newArrayList(array));
	}

	public static List<JDA> getShards() {
		return shards;
	}

	public static <K, V> HashMap<K, V> newHashMap(K[] keys, V[] values) {
		HashMap<K, V> map = new HashMap<>();
		for (int x = 0; x < keys.length && x < values.length; x++)
			map.put(keys[x], values[x]);
		return map;
	}

	/**
	 * What have I done?
	 */
	public static void somebodyOnceToldMeTheWorldIsGonnaRollMeIAintTheSharpestToolInTheShedSheWasLookingKindOfDumbWithHerFingerAndHerThumbInTheShapeOfAnLOnHerForeheadWellTheYearsStartComingAndTheyDontStopComingFedToTheRulesAndIHitTheGroundRunningDidntMakeSenseNotToLiveForFunYourBrainGetsSmartButYourHeadGetsDumbSoMuchToDoSoMuchToSeeSoWhatsWrongWithTakingTheBackstreetYoullNeverComeIfYouDontGoYoullNeverShineIfYouDontGlow(Object
			HEYNOWYOUREANALLSTARGETYOURGAMEONGOPLAYHEYNOWYOUREAROCKSTARGETYOURSHOWONGETPAIDANDALLTHATGLITTERSISGOLDONLYSHOOTINGSTARSCANBREAKTHEMOLD) {
		return;
	}

	public static Integer getIntFromPossibleDouble(Object d) {
		if (d instanceof Double) return ((Double) d).intValue();
		else if (d instanceof Integer) return (Integer) d;
		else
			try {
				return (int) d;
			} catch (Throwable e) {
				return -1;
			}
	}

	public static Long getLongFromPossibleDouble(Object d) {
		if (d instanceof Double) return ((Double) d).longValue();
		else if (d instanceof Long) return (Long) d;
		else
			try {
				return (long) d;
			} catch (Throwable e) {
				return -1L;
			}
	}

	public static boolean isValidURL(String url) {
		try {
			new URL(url);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	public static String percentEncode(String text) {
		return percentEncode(text, false);
	}

	/**
	 * Replace characters to their URL encoded counter-part.
	 * E.g. ' becomes %27
	 * @param text The text that needs encoding.
	 * @param encodeAlphanumeric A boolean which when true will also encode characters like a, b, c, 1, 2, and 3.
	 * @return A never-null String encoded for use with URLs (which is probably 3 times as long as the input text).
	 */
	public static String percentEncode(String text, boolean encodeAlphanumeric) {
		String[] parts = text.split("\n");
		String[] outputParts = new String[parts.length];
		for (int x = 0; x < parts.length; x++) {
			String output = "";
			for (Character ch : parts[x].toCharArray())
				if (encodeAlphanumeric || !isAlphanumeric(ch)) output += "%" + Integer.toHexString(ch);
				else output += ch;
			outputParts[x] = output;
		}
		return joinCustomChar("%0A", outputParts);
	}

	public static boolean isAlphanumeric(char ch) {
		ch = Character.toLowerCase(ch);
		return ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9';
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static <T extends Comparable<? super T>> List<T> sort(List<T> list) {
		Collections.sort(list);
		return list;
	}

	public static <T> List<T> setToList(Set<T> set, Class<T[]> clazz) {
		return Arrays.asList(Arrays.copyOf(set.toArray(), set.size(), clazz));
	}

	public static String multiplyString(String string, int times) {
		String original = new String(string);
		for (int i : range(times))
			string += original;
		return string;
	}

	@Nullable
	public static Method getMethod(Class clazz, String method, @Nullable Class... parameters) {
		if (parameters == null) parameters = new Class[] {};
		Class[] params = parameters;
		return new Object() {
			Method getMethod() {
				if (getMethod(clazz.getMethods()) != null) return getMethod(clazz.getMethods());
				else return getMethod(clazz.getDeclaredMethods());
			}
			Method getMethod(Method[] methods) {
				for (Method method1 : methods)
					if (method1.getParameterCount() == params.length && method1.getName().equals(method)) {
						for (int x = 0; x < params.length && x < method1.getParameterCount(); x++)
							if (!method1.getParameterTypes()[x].equals(params[x])) return null;
						return method1;
					}
				return null;
			}
		}.getMethod();
	}

	public static void setOnlineStatus(OnlineStatus status) {
		for (JDA shard : shards)
			shard.getPresence().setStatus(status);
	}

	public static void setGame(Game game) {
		for (JDA shard : shards)
			shard.getPresence().setGame(game);
	}

	public static void setGame(String game) {
		setGame(Game.of(game));
	}

	public static OnlineStatus getStatusFromString(String string) {
		switch (string.toUpperCase()) {
		case "OFFLINE": 		{return OnlineStatus.OFFLINE;}
		case "INVISIBLE": 		{return OnlineStatus.INVISIBLE;}
		case "DND": 			{return OnlineStatus.DO_NOT_DISTURB;}
		case "DO_NOT_DISTURB": 	{return OnlineStatus.DO_NOT_DISTURB;}
		case "IDLE": 			{return OnlineStatus.IDLE;}
		case "ONLINE": 			{return OnlineStatus.ONLINE;}
		default: 				{return OnlineStatus.UNKNOWN;}
		}
	}

	public static void setAvatar(Icon avatar) {
		shards.get(0).getSelfUser().getManager().setAvatar(avatar).queue();
	}

	public static String getPrefix(Guild guild) {
		try {
			Map settings = DataIO.loadJsonOrDefault("data/mod/settings.json", Map.class, new HashMap());
			String serverPrefix = Config.get("prefix");
			if (guild != null && settings.containsKey(guild.getId())) {
				serverPrefix = ((Map) settings.get(guild.getId())).get("serverPrefix").toString();
				return serverPrefix == null || serverPrefix.isEmpty() ? Config.get("prefix") : serverPrefix;
			} else return serverPrefix;
		} catch (Exception e) {
			return Config.get("prefix");
		}
	}

	public static SelfUser getSelfUser() {
		return shards.get(0).getSelfUser();
	}

	/**
	 * @return Basically the same as what gets printed when you do {@link java.lang.Throwable#printStackTrace() cause.printStackTrace()}.
	 */
	public static String generateStackTrace(Throwable cause) {
		String stackTrace = String.format("%s: %s\n\t", cause.getClass().toString(), cause.getMessage());
		for (StackTraceElement element : cause.getStackTrace())
			stackTrace += "at " + element.toString() + "\n\t";
		while (cause.getCause() != null) {
			cause = cause.getCause();
			stackTrace = stackTrace.trim();
			stackTrace += String.format("\nCaused by %s: %s\n\t", cause.getClass().getName(), cause.getMessage());
			for (StackTraceElement element : cause.getStackTrace())
				stackTrace += "at " + element.toString() + "\n\t";
		}
		return stackTrace.trim();
	}

	public static String encase(String string) {
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}

	public static String pascalCase(String string) {
		String output = "";
		for (String part : string.split(" "))
			output += encase(part) + " ";
		return output;
	}

	public static String camelCase(String string) {
		return string.split(" ")[0].toLowerCase() + " " + pascalCase(join(removeArg(string.split(" "), 0)));
	}

	public static List<Method> getMethods(Class clazz) {
		List<Method> methods = new ArrayList();
		for (Method method : clazz.getMethods())
			methods.add(method);
		for (Method method : clazz.getDeclaredMethods())
			if (!new Object() {
				boolean methodInList() {
					for (Method method1 : methods)
						if (method1.equals(method)) return true;
					return false;
				}
			}.methodInList()) methods.add(method);
		return methods;
	}

	public static Map<String, Integer> getCommandIndex() {
		return commandIndex;
	}

	/**
	 * Adds a hook that should be ran everytime a command is ran.
	 * If this throws a {@link com.ptsmods.impulse.miscellaneous.CommandPermissionException CommandPermissionException} then the command isn't executed and instead the message given with the exception is sent.
	 * @param hook The hook to add.
	 */
	public static void addCommandHook(CommandExecutionHook hook) {
		commandHooks.add(hook);
	}

	public static List<CommandExecutionHook> getCommandHooks() {
		return commandHooks;
	}

	public static List<Method> getSubcommands(Method parent) {
		return linkedSubcommands.get(parent.toString()) == null ? new ArrayList() : linkedSubcommands.get(parent.toString());
	}

	/**
	 * @param obj The object to clone
	 * @return A cloned version of the given object.
	 * @author WillingLearner&nbsp;(https://stackoverflow.com/a/25338780)
	 */
	public static <T> T clone(T obj) {
		try {
			Object clone = obj.getClass().newInstance();
			for (Field field : obj.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(obj) == null || Modifier.isFinal(field.getModifiers()))
					continue;
				if (field.getType().isPrimitive() || field.getType().equals(String.class)
						|| field.getType().getSuperclass() != null && field.getType().getSuperclass().equals(Number.class)
						|| field.getType().equals(Boolean.class))
					field.set(clone, field.get(obj));
				else {
					Object childObj = field.get(obj);
					if (childObj == obj)
						field.set(clone, clone);
					else
						field.set(clone, clone(field.get(obj)));
				}
			}
			return (T) clone;
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T[] createArray(T... args) {
		return args;
	}

	public static <T> List<T> add(List<T> list, T object) {
		list.add(object);
		return list;
	}

	public static String getOSName() {
		return osName;
	}

	public static boolean isWindows() {
		return isWindows;
	}

	public static boolean isMac() {
		return isMac;
	}

	public static boolean isUnix() {
		return isUnix;
	}

	public static boolean isSolaris() {
		return isSolaris;
	}

	public static <T> T getOrDefault(List<T> list, int index, T fallback) {
		if (list == null || index >= list.size() || index < 0) return fallback;
		else return list.get(index);
	}

	public static final boolean done() {
		return Boolean.valueOf(done);
	}

	public static void sleep(long millis) {
		long stop = System.currentTimeMillis() + millis;
		while (System.currentTimeMillis() < stop) { }
		return;
	}

	public static String colorToHex(Color color) {
		return Integer.toHexString(color == null ? 0 : color.getRGB()).toUpperCase().substring(2);
	}

	public static String plural(String object) {
		if (object.endsWith("o")) object += "es";
		else if (object.endsWith("y")) object = object.substring(0, object.length()-1) + "ies";
		else if (object.endsWith("s"));
		else object += "s";
		return object;
	}

	public static URL shorten(URL url) throws MalformedURLException {
		Response<ShortenResponse> resp = bitlyClient.shorten().setLongUrl(url.toString()).call();
		if (resp.status_txt.equals("INVALID_URI")) throw new MalformedURLException("The given URL was invalid, according to bit.ly.");
		else return new URL(resp.data.url);
	}

	public static long getTime(TimeType type) {
		switch (type) {
		case SECONDS: return System.currentTimeMillis()/1000;
		case MILLISECONDS: return System.currentTimeMillis();
		case MICROSECONDS: return System.nanoTime()/1000;
		case NANOSECONDS: return System.nanoTime();
		default: return -1;
		}
	}

	public static <T> String str(T obj) {
		if 		(obj instanceof User) 		return obj == null ? "null#0000" : ((User) obj).getName() + "#" + ((User) obj).getDiscriminator();
		else if (obj instanceof Member) 	return obj == null ? "null#0000" : ((Member) obj).getUser().getName() + "#" + ((Member) obj).getUser().getDiscriminator();
		else if (obj instanceof Guild) 		return obj == null ? "null" : ((Guild) obj).getName();
		else if (obj instanceof Channel) 	return obj == null ? "null" : ((Channel) obj).getName();
		// more to be added later.
		else return obj == null ? "null" : obj.toString();
	}

	public static EventHandler getEventHandler() {
		return eventHandler;
	}

	public static List<Channel> getAllChannels(Guild guild) {
		List<Channel> channels = new ArrayList();
		channels.addAll(guild.getTextChannels());
		channels.addAll(guild.getVoiceChannels());
		return channels;
	}

	public static List<TextChannel> getTextChannels() {
		List<TextChannel> textChannels = new ArrayList();
		for (Guild guild : getGuilds())
			textChannels.addAll(guild.getTextChannels());
		return textChannels;
	}

	public static List<VoiceChannel> getVoiceChannels() {
		List<VoiceChannel> voiceChannels = new ArrayList();
		for (Guild guild : getGuilds())
			voiceChannels.addAll(guild.getVoiceChannels());
		return voiceChannels;
	}

	public static List<Channel> getChannels() {
		List<Channel> channels = new ArrayList();
		for (Guild guild : getGuilds()) {
			channels.addAll(guild.getTextChannels());
			channels.addAll(guild.getVoiceChannels());
		}
		return channels;
	}

	public static List<Role> getRoles() {
		List<Role> roles = new ArrayList();
		for (Guild guild : getGuilds())
			roles.addAll(guild.getRoles());
		return roles;
	}

	public static Role cloneRole(Role role) {
		return new RoleImpl(role.getIdLong(), role.getGuild())
				.setColor(role.getColor())
				.setHoisted(role.isHoisted())
				.setManaged(role.isManaged())
				.setMentionable(role.isMentionable())
				.setName(role.getName())
				.setRawPermissions(role.getPermissionsRaw())
				.setRawPosition(role.getPositionRaw());
	}

	public static TextChannel cloneChannel(TextChannel channel) {
		TextChannelImpl channel1 = new TextChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild())
				.setName(channel.getName())
				.setNSFW(channel.isNSFW())
				.setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong())
				.setRawPosition(channel.getPositionRaw())
				.setTopic(channel.getTopic());
		try {
			channel1.setLastMessageId(channel.getLatestMessageIdLong());
		} catch (IllegalStateException e) {}
		return channel1;
	}

	public static VoiceChannel cloneChannel(VoiceChannel channel) {
		return new VoiceChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild())
				.setBitrate(channel.getBitrate())
				.setName(channel.getName())
				.setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong())
				.setRawPosition(channel.getPositionRaw())
				.setUserLimit(channel.getUserLimit());
	}

	public static PermissionOverride getPermissionOverride(Member member, Channel channel) {
		return channel.getPermissionOverride(member) == null ? channel.createPermissionOverride(member).complete() : channel.getPermissionOverride(member);
	}

	public static PermissionOverride getPermissionOverride(Role role, Channel channel) {
		return channel.getPermissionOverride(role) == null ? channel.createPermissionOverride(role).complete() : channel.getPermissionOverride(role);
	}

	public static URL newUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Used to play sounds outside of the JAR file.
	 * @param sound {@code new File(fileLocation).getAbsoluteFile()}
	 * @see #playSound(InputStream)
	 * @see #playSound(AudioInputStream)
	 */
	public static void playSound(File sound) {
		try {
			playSound(AudioSystem.getAudioInputStream(sound));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to play sounds from the JAR file.
	 * @param sound {@code class.getClassLoader().getResourceAsStream(fileLocation)}
	 * @see #playSound(File)
	 * @see #playSound(AudioInputStream)
	 */
	public static void playSound(InputStream sound) {
		try {
			playSound(AudioSystem.getAudioInputStream(sound instanceof BufferedInputStream ? sound : new BufferedInputStream(sound)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mainly used by {@link #playSound(File)} and {@link #playSound(InputStream)}.
	 * @param audioInputStream
	 */
	public static void playSound(AudioInputStream audioInputStream) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BufferedInputStream getResourceAsStream(String name) {
		return new BufferedInputStream(Main.class.getResourceAsStream(name) == null ? Main.class.getClassLoader().getResourceAsStream(name) : Main.class.getResourceAsStream(name));
	}

	public static boolean isSuperClass(Class clazz, Class superClass) {
		while (clazz.getSuperclass() != null)
			if (clazz.getSuperclass().equals(superClass)) return true;
			else clazz = clazz.getSuperclass();
		return false;
	}

	public static ThreadPoolExecutor getThreadPool(String name) {
		switch (name.toLowerCase()) {
		case "miscellaneous": return miscellaneousExecutor;
		case "commands": return commandsExecutor;
		default: return null;
		}
	}

	public static Guild getGuildByName(String name) {
		for (Guild guild : getGuilds())
			if (guild.getName().equalsIgnoreCase(name)) return guild;
		return null;
	}

	public static List<Permission> getDefaultPermissions() {
		List<Permission> permissions = new ArrayList();
		Collections.addAll(permissions, new Permission[] {Permission.CREATE_INSTANT_INVITE, Permission.NICKNAME_CHANGE, Permission.VOICE_CONNECT, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD});
		return permissions;
	}

	public static TextChannel getTextChannelById(String id) {
		for (TextChannel channel : getTextChannels())
			if (channel.getId().equals(id)) return channel;
		return null;
	}

	public static Guild getGuildById(String id) {
		for (Guild guild : getGuilds())
			if (guild.getId().equals(id)) return guild;
		return null;
	}

	public static List<String> getCategories() {
		return categories;
	}

	public static boolean startsWith(String string, String[] args) {
		for (String arg : args)
			if (string.startsWith(arg)) return true;
		return false;
	}

	public static boolean startsWith(String string, List<String> args) {
		return startsWith(string, args.toArray(new String[0]));
	}

	public static boolean contains(String string, String[] args) {
		for (String arg : args)
			if (string.contains(arg)) return true;
		return false;
	}

	public static boolean contains(String string, List<String> args) {
		return contains(string, args.toArray(new String[0]));
	}

	public static <K, V> boolean containsKeys(Map<K, V> map, K... keys) {
		for (K key : keys)
			if (!map.containsKey(key)) return false;
		return true;
	}

	public static ThreadPoolExecutor newTPE() {
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public static Class getCallerClass() {
		try {
			return Class.forName(new Exception().getStackTrace()[3].getClassName(), false, ClassLoader.getSystemClassLoader());
		} catch (ClassNotFoundException ignored) { // should not be possible
			return null;
		}
	}

	@Deprecated
	public static Class getCallerClassQuickly() {
		return Reflection.getCallerClass(3);
	}

	public static <K, V> Map<K, V> removeKeys(Map<K, V> map, K... keys) {
		for (K key : keys)
			map.remove(key);
		return map;
	}

	public static String trim(String string) {
		if (string == null) return null;
		while (string.startsWith(" ")) string = string.substring(1, string.length());
		while (string.endsWith(" ")) string = string.substring(0, string.length()-1);
		return string;
	}

	private static final class SystemOutPrintStream extends PrintStream {

		private static final PrintStream originalOut = System.out;

		public SystemOutPrintStream() throws FileNotFoundException {
			super(new FileOutputStream("bot.log"));
		}

		@Override
		public void print(boolean arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(char arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(int arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(long arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(float arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(double arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(char[] arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(String arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void print(Object arg) {
			originalOut.print(arg);
			super.print(arg);
		}

		@Override
		public void println(boolean arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(char arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(int arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(long arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(float arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(double arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(char[] arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(String arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public void println(Object arg) {
			originalOut.println(arg);
			super.println(arg);
		}

		@Override
		public PrintStream printf(String arg, Object... args) {
			originalOut.printf(arg, args);
			return super.printf(arg, args);
		}

		@Override
		public PrintStream append(CharSequence arg) {
			originalOut.append(arg);
			return super.append(arg);
		}

		@Override
		public PrintStream append(CharSequence arg, int start, int end) {
			originalOut.append(arg, start, end);
			return super.append(arg, start, end);
		}

		@Override
		public PrintStream append(char arg) {
			originalOut.append(arg);
			return super.append(arg);
		}

	}

	public enum TimeType {
		SECONDS(),
		MILLISECONDS(),
		MICROSECONDS(),
		NANOSECONDS();
	}

	public enum LogType {
		DEBUG("DEBUG", ConsoleColors.HIGHINTENSITY_BLUE),
		INFO("INFO", ConsoleColors.UNKNOWN),
		WARN("WARN", ConsoleColors.YELLOW),
		ERROR("ERROR", ConsoleColors.RED);

		public final String name;
		public final ConsoleColors color;

		private LogType(String name, ConsoleColors color) {
			this.name = name;
			this.color = color;
		}

	}

}
