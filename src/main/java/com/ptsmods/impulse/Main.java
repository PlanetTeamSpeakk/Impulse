package com.ptsmods.impulse;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JComponent;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandExecutionHook;
import com.ptsmods.impulse.miscellaneous.CommandPermissionException;
import com.ptsmods.impulse.miscellaneous.ConsoleCommandEvent;
import com.ptsmods.impulse.miscellaneous.EventHandler;
import com.ptsmods.impulse.miscellaneous.ImpulseSM;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;
import com.ptsmods.impulse.utils.ArrayMap;
import com.ptsmods.impulse.utils.AtomicObject;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.ConsoleColours;
import com.ptsmods.impulse.utils.Dashboard;
import com.ptsmods.impulse.utils.DataIO;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.EventListenerManager;
import com.ptsmods.impulse.utils.HookedPrintStream;
import com.ptsmods.impulse.utils.HookedPrintStream.PrintHook;
import com.ptsmods.impulse.utils.ImageManipulator;
import com.ptsmods.impulse.utils.Random;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
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
import sun.misc.Unsafe;
import sun.reflect.Reflection;

public class Main {

	public static final int							major					= 1;
	public static final int							minor					= 9;
	public static final int							revision				= 3;
	public static final String						type					= "stable";
	public static final String						version					= String.format("%s.%s.%s-%s", major, minor, revision, type);
	public static final Object						nil						= null;
	public static final Date						started					= new Date();
	public static final Map<String, String>			apiKeys					= new HashMap<>();
	public static final Thread						mainThread				= Thread.currentThread();
	public static final List<Permission>			defaultPermissions		= Collections.unmodifiableList(getDefaultPermissions());
	public static final Permission[]				defaultPermissionsArray	= defaultPermissions.toArray(new Permission[0]);
	private static Unsafe							theUnsafe;
	private static final BitlyClient				bitlyClient;
	private static volatile boolean					done					= false;
	private static final ThreadPoolExecutor			commandsExecutor		= newTPE();
	private static final ThreadPoolExecutor			miscellaneousExecutor	= newTPE();
	private static final String						osName					= System.getProperty("os.name");
	private static final boolean					isWindows				= osName.toLowerCase().contains("windows");
	private static final boolean					isMac					= osName.toLowerCase().contains("mac");
	private static final boolean					isUnix					= osName.toLowerCase().contains("nix") || osName.toLowerCase().contains("nux") || osName.toLowerCase().contains("aix");
	private static final boolean					isSolaris				= osName.toLowerCase().contains("sunos");
	private static Map<String, Integer>				commandIndex			= new HashMap();
	private static List<CommandExecutionHook>		commandHooks			= new ArrayList<>();
	private static List<JDA>						shards					= new ArrayList<>();
	private static boolean							devMode					= false;
	private static boolean							eclipse					= false;
	private static boolean							headless				= false;
	private static boolean							useSwing				= false;
	private static User								owner					= null;
	private static List<User>						coOwners				= new ArrayList();
	private static List<Method>						commands				= new ArrayList<>();
	private static List<Method>						subcommands				= new ArrayList<>();
	private static AtomicObject<List<Message>>		messages				= new AtomicObject(new ArrayList<>());
	private static Map<String, List<Method>>		linkedSubcommands		= new HashMap();
	private static EventHandler						eventHandler			= new EventHandler();
	private static Map<String, Map<String, Long>>	cooldowns				= new HashMap();
	private static List<String>						categories				= new ArrayList();
	private static volatile boolean					shutdown				= false;
	private static Map<Method, Integer>				usages					= new HashMap();
	private static Map<String, String>				serverPrefixes			= new HashMap();
	private static String							globalPrefix			= "";
	private static Map<String, Map<String, Long>>	userCommandUsages		= new HashMap();

	static {
		try {
			PrintStream originalOut = new PrintStream(System.out);
			System.setOut(new HookedPrintStream(new FileOutputStream("bot.log"), new PrintHook() {
				@SuppressWarnings("deprecation")
				@Override
				public void onPrint(String string, boolean newLine) {
					originalOut.print((eclipse ? string : ConsoleColours.getCleanString(string)) + (newLine ? System.lineSeparator() : ""));
					if (useSwing)
						MainGUI.logLine(ConsoleColours.getCleanString(string).trim());
					else MainJFXGUI.logLine(ConsoleColours.getCleanString(string), ConsoleColours.getFirstColour(string).toColour());
				}
			}));
			PrintStream originalErr = new PrintStream(System.err);
			System.setErr(new HookedPrintStream(new FileOutputStream("error.log"), new PrintHook() {
				@SuppressWarnings("deprecation")
				@Override
				public void onPrint(String string, boolean newLine) {
					originalErr.print((eclipse ? string : ConsoleColours.getCleanString(string)) + (newLine ? System.lineSeparator() : ""));
					if (useSwing)
						MainGUI.logLine(ConsoleColours.getCleanString(string).trim());
					else MainJFXGUI.logLine(ConsoleColours.getCleanString(string), Color.RED);
				}
			}));
		} catch (FileNotFoundException e) {
		}
		miscellaneousExecutor.allowCoreThreadTimeOut(true);
		apiKeys.put("bitly", "dd800abec74d5b12906b754c630cdf1451aea9e0");
		bitlyClient = new BitlyClient(apiKeys.get("bitly"));
		Constructor<Unsafe> unsafeConstructor = null;
		try {
			unsafeConstructor = Unsafe.class.getDeclaredConstructor();
			unsafeConstructor.setAccessible(true);
		} catch (Exception e) {
		}
		if (unsafeConstructor != null)
			try {
				theUnsafe = unsafeConstructor.newInstance();
			} catch (Exception e) {
				theUnsafe = null;
			}
		else theUnsafe = null;
	}

	public static final void main(String[] args) {
		List<String> argsList = Lists.newArrayList(args);
		devMode = argsList.contains("-devMode");
		eclipse = argsList.contains("-eclipse");
		headless = argsList.contains("-headless") || argsList.contains("-noGui");
		useSwing = argsList.contains("-useSwing") || argsList.contains("-noJfx");
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

	@SuppressWarnings("deprecation")
	private static final void main0(String[] args) throws Throwable {
		if (!headless) if (!useSwing)
			try {
				MainJFXGUI.startBlocking(args);
				MainJFXGUI.logLine("Welcome to Impulse v" + version + ", if you have any experience with JavaFX schemes and you'd like to change this GUI to look a bit better, make sure to take a look at the jfxGui.css file in this JAR file.", ConsoleColours.CYAN.toColour());
			} catch (IllegalAccessException e) {
				print(LogType.DEBUG, "Could not initialize the Main GUI, it seems to have already been initialized.");
			}
		else MainGUI.initialize();
		try {
			Dashboard.initialize();
		} catch (IOException e) {
			print(LogType.ERROR, "The webserver could not be initialized, are you sure port 61192 isn't used?");
		}
		Locale.setDefault(Locale.US);
		System.setProperty("user.timezone", "UTC");
		TimeZone.setDefault(null); // forcibly setting the default TimeZone and its system property to UTC.
		TimeZone.getDefault(); // recalculating default TimeZone before setting the security manager as that'll
								// block it.
		System.setSecurityManager(new ImpulseSM());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (devMode())
				print(LogType.DEBUG, "Bot shutting down, deleting all temporary files.");
			else print(LogType.INFO, "Shutting down...");
			int counter = 0;
			if (new File("data/tmp").exists() && new File("data/tmp").isDirectory())
				deleteAllFilesInDir(new File("data/tmp/"));
			else new File("data/tmp/").mkdirs();
			print(LogType.DEBUG, String.format("Temporary files deleted, deleted %s file%s.", counter, counter == 1 ? "" : "s"));
			try (PrintWriter writer = new PrintWriter(new FileWriter("data/tmp/README.txt"))) {
				writer.println("This directory is meant for temporary files only.");
				writer.println("This directory is cleared on bot shutdown.");
				writer.println("DO NOT STORE FILES IN THIS DIRECTORY!");
				IOUtils.closeQuietly(writer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (Thread thread : Thread.getAllStackTraces().keySet())
				if (!thread.equals(mainThread)) thread.interrupt();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}));
		if (devMode) print(LogType.DEBUG, "Developer mode is turned on, this means that errors will NOT be sent privately to the owner of the bot and only the owner can use commands.");
		try {
			if (!new File("data/").isDirectory()) new File("data/").mkdirs();
			if (Config.get("token") == null || globalPrefix == null || Config.get("ownerId") == null || Config.get("shards") == null) {
				Config.addComment("Config file created by Impulse Discord Bot written by PlanetTeamSpeak.");
				Config.addComment("The bot token used to log in, example: MzIzMTc2ODk2NTAwMzM0NTk0.DLfRfw.zZ7V1ljky12M5sKEVUZBAzwYUbo.");
				Config.put("token", "");
				Config.addComment("The prefix used to execute commands, this CANNOT be 2 or more slashes!");
				Config.put("prefix", "\\");
				Config.addComment("The ID of the owner, this should be your id as anyone who has this can do anything with your bot in any server.");
				Config.put("ownerId", "");
				Config.addComment("Any potential co-owners, these users have exactly the same permissions as the owner. You can divide the IDs with semi-colons (;).");
				Config.put("coOwnerIds", "");
				Config.addComment("The amount of shards you want, for every shard the startup time takes at least 5 more seconds, this is due to rate limiting.");
				Config.addComment("If you don't know what shards are, maybe you should learn some stuff about computers before making your own Discord bot.");
				Config.put("shards", "1");
				Config.addComment("The key used to encrypt the JSON files, this should *never* be changed as it'll make the JSON files unreadable. This has to be 16 characters.");
				Config.put("kryptoKey", Random.genKey(16));
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
			if (Config.get("kryptoKey") == null || Config.get("kryptoKey").length() != 16) {
				print(LogType.ERROR, "The variable kryptoKey in the config is not 16 characters long.");
				return;
			}
			globalPrefix = Config.get("prefix");
			int shardAmount = 1;
			if (!isInteger(Config.get("shards")))
				print(LogType.WARN, "The value of key 'shards' isn't an integer, expecting 1.");
			else shardAmount = Integer.parseInt(Config.get("shards"));
			if (shardAmount < 1) shardAmount = 1;
			print(LogType.INFO, "Loading commands...");
			for (Class clazz : new Reflections("com.ptsmods.impulse.commands", new SubTypesScanner(false)).getSubTypesOf(Object.class)) {
				runAsynchronously(() -> {
					// initializing class asynchronously.
					try {
						Class.forName(clazz.getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				EventListenerManager.registerListenersFromClass(clazz);
				for (Method method : getMethods(clazz))
					if (method.isAnnotationPresent(Command.class)) {
						if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class))) {
							commands.add(method);
							if (!categories.contains(method.getAnnotation(Command.class).category())) categories.add(method.getAnnotation(Command.class).category());
							commandIndex.put(method.getAnnotation(Command.class).name(), commands.size() - 1);
						} else print(LogType.DEBUG, "Found a command that requires more than only a CommandEvent.", method.toString());
					} else if (method.isAnnotationPresent(Subcommand.class)) if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class)))
						try {
							Class.forName(joinCustomChar(".", removeArg(method.getAnnotation(Subcommand.class).parent().split("\\."), method.getAnnotation(Subcommand.class).parent().split("\\.").length - 1)), false, ClassLoader.getSystemClassLoader());
							subcommands.add(method);
						} catch (ClassNotFoundException e) {
							print(LogType.DEBUG, "Found a subcommand that has an invalid parent.", method.toString());
						}
					else print(LogType.DEBUG, "Found a command that requires more than only a CommandEvent.", method.toString());
			}
			for (Method subcommand : subcommands) {
				Method parentCommand = getParentCommand(subcommand.getAnnotation(Subcommand.class));
				if (!linkedSubcommands.containsKey(parentCommand.toString()))
					linkedSubcommands.put(parentCommand.toString(), Lists.newArrayList(subcommand));
				else((List) linkedSubcommands.get(parentCommand.toString())).add(subcommand);
			}
			for (Method command : commands)
				if (!linkedSubcommands.containsKey(command.toString())) linkedSubcommands.put(command.toString(), new ArrayList());
			print(LogType.INFO, commands.size(), "commands and", subcommands.size(), "subcommands loaded, loading server prefixes...");
			int guildsWithDefaultPrefix = 0;
			Map<String, Object> modSets = DataIO.loadJsonOrDefault("data/mod/settings.json", Map.class, new HashMap());
			for (String guild : modSets.keySet()) {
				String serverPrefix = globalPrefix;
				try {
					serverPrefix = ((Map) modSets.get(guild)).get("serverPrefix").toString();
					serverPrefix = serverPrefix == null || serverPrefix.trim().isEmpty() ? globalPrefix : serverPrefix;
				} catch (Exception e) {
					serverPrefix = globalPrefix;
				}
				serverPrefixes.put(guild, serverPrefix);
				if (serverPrefix.equals(globalPrefix)) guildsWithDefaultPrefix += 1;
			}
			print(LogType.INFO, serverPrefixes.size(), "server prefixes loaded of which", guildsWithDefaultPrefix, "had a default prefix, logging in...");
			ShardedRateLimiter rateLimiter = new ShardedRateLimiter();
			eventHandler = new EventHandler();
			for (int i : range(shardAmount)) {
				JDA shard = new JDABuilder(AccountType.BOT).setToken(Config.get("token")).addEventListener(eventHandler).useSharding(shards.size(), shardAmount).setReconnectQueue(new SessionReconnectQueue()).setShardedRateLimiter(rateLimiter).setGame(Game.of("Starting...")).buildBlocking();
				shards.add(shard);
				if (i != shardAmount - 1) {
					print(LogType.INFO, "Started shard " + i + ", waiting 5 seconds before starting shard " + (i + 1) + "/" + shardAmount + ".");
					Thread.sleep(5000);
				}
			}
			owner = getUserById(Config.get("ownerId"));
			for (String coOwnerId : (Config.get("coOwnerIds") == null ? "" : Config.get("coOwnerIds")).split(";"))
				try {
					coOwners.add(getUserById(coOwnerId));
				} catch (Exception e) {
				}
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
		print(LogType.INFO, String.format("Succesfully logged in as %s, took %s milliseconds. Owner = %s, prefix = %s.", str(shards.get(0).getSelfUser()), System.currentTimeMillis() - started.getTime(), str(owner), globalPrefix));
		for (JDA shard : shards)
			shard.getPresence().setGame(Game.of(devMode() ? "DEVELOPER MODE" : "try " + globalPrefix + "help!"));
		done = true;
		List<Class> passedClasses = new ArrayList();
		for (Method command : commands) {
			Class clazz = command.getDeclaringClass();
			if (!passedClasses.contains(clazz)) {
				passedClasses.add(clazz);
				if (getMethod(clazz, "onFullyBooted") != null && getMethod(clazz, "onFullyBooted").isAnnotationPresent(SubscribeEvent.class)) try {
					getMethod(clazz, "onFullyBooted").invoke(null);
				} catch (InvocationTargetException e) {
					e.getCause().printStackTrace();
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> boolean print(String threadName, LogType logType, T... message) {
		if (logType == LogType.DEBUG && !devMode()) return true;
		String[] args = new String[message.length];
		for (int x = 0; x < message.length; x++)
			if (message[x] == null)
				args[x] = "null";
			else args[x] = message[x].toString();
		String output = String.format(ConsoleColours.RESET + "%s[%s] [%s/%s]: %s\n" + ConsoleColours.RESET, logType.colour, getFormattedTime(), threadName, logType.name, join(args));
		System.out.print(output);
		return true;
	}

	public static String getFormattedTime() {
		return joinCustomChar(":", "" + (LocalDateTime.now().getHour() < 10 ? "0" : "") + LocalDateTime.now().getHour(), "" + (LocalDateTime.now().getMinute() < 10 ? "0" : "") + LocalDateTime.now().getMinute(), "" + (LocalDateTime.now().getSecond() < 10 ? "0" : "") + LocalDateTime.now().getSecond());
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
			data += array[x] + (x + 1 == array.length ? "" : character);
		return data.trim();
	}

	public static void shutdown(int status) {
		runAsynchronously(() -> {
			if (status == 2) {
				for (JDA shard : shards)
					shard.shutdownNow();
				commandsExecutor.shutdownNow();
				miscellaneousExecutor.shutdownNow();
				System.exit(status);
			} else {
				long timeout = calculateShutdownTimeout();
				shutdown = true;
				for (JDA shard : shards)
					shard.shutdown();
				commandsExecutor.shutdown();
				miscellaneousExecutor.shutdown();
				try {
					IdentityHashMap<Thread, Thread> map = getShutdownHooks();
					for (Thread thread : new IdentityHashMap<>(map).keySet()) {
						long currentMillis = System.currentTimeMillis();
						AtomicBoolean bool = new AtomicBoolean(false);
						Thread executionThread = new Thread(() -> {
							thread.start();
							try {
								thread.join();
							} catch (InterruptedException ignored) {
							}
							bool.set(true);
						});
						executionThread.start();
						while (!bool.get() && System.currentTimeMillis() - currentMillis < timeout)
							sleep(250); // trying not to use too much CPU.
						if (!bool.get()) {
							print(LogType.WARN, "Execution of a hook took longer than", timeout, " milliseconds, this is abnormal. Hook:", thread.getName());
							executionThread.interrupt();
						}
					}
					map.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.exit(status);
			}
		});
	}

	public static IdentityHashMap<Thread, Thread> getShutdownHooks() {
		try {
			Field field = getField(Class.forName("java.lang.ApplicationShutdownHooks"), "hooks", IdentityHashMap.class);
			field.setAccessible(true);
			return (IdentityHashMap) field.get(null);
		} catch (Exception e) {
			throwCheckedExceptionWithoutDeclaration(e);
			return null;
		}
	}

	public static int calculateShutdownTimeout() {
		return getGuilds().size() * 10 < 15000 ? 15000 : getGuilds().size() * 10;
	}

	public static int calculateTotalShutdownTimeout() {
		return getShutdownHooks().size() * calculateShutdownTimeout();
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

	public static void createDirectoryIfNotExisting(String dir) {
		if (!new File(dir).isDirectory()) new File(dir).mkdirs();
	}

	public static void createFileIfNotExisting(String file) throws IOException {
		if (!new File(file).exists()) new File(file).createNewFile();
	}

	public static User getUserFromInput(Message input) {
		try {
			return !input.getMentionedUsers().isEmpty() ? input.getMentionedUsers().get(0) : input.getContent().startsWith(globalPrefix) ? input.getGuild().getMembersByName(getUsernameFirstArg(input), true).get(0).getUser() : null;
		} catch (Throwable e) {
			return null;
		}
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
		deleteCooldown(event.getAuthor(), cmd);
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
						if (!event.isOwner() && !event.isCoOwner()) if (scmd.hidden() || scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) || event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions())) continue;
						cmdSubcommands += String.format("**%s**", scmd.name()) + (scmd.help() == null || scmd.help().isEmpty() ? "" : ": " + scmd.help().split("\n")[0]) + "\n\t";
					}
				cmdSubcommands = cmdSubcommands.trim();
			}
			String output = String.format("**%s**%s%s%s", globalPrefix + cmdName, cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp, extraMsg == null || extraMsg.isEmpty() ? "" : (cmdHelp.isEmpty() ? ":" : "") + "\n\n" + extraMsg, cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands);
			if (event instanceof ConsoleCommandEvent) {
				event.reply(output);
				return null;
			} else return channel.sendMessage(output).complete();

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
						if (!event.isOwner() && !event.isCoOwner()) if (scmd.hidden() || scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) || event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions())) continue;
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
			String output = String.format("**%s**%s%s%s", globalPrefix + cmdName, cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp, extraMsg == null || extraMsg.isEmpty() ? "" : (cmdHelp.isEmpty() ? ":" : "") + "\n\n" + extraMsg, cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands);
			if (event instanceof ConsoleCommandEvent) {
				event.reply(output);
				return null;
			} else return channel.sendMessage(output).complete();
		}
		return null;
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd) {
		return sendCommandHelp(event.getChannel(), event, cmd, null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(CommandEvent event) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(channel, event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event) {
		return sendCommandHelp(channel, event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, cmd, extraMsg);
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, event.getCommand(),
	 * extraMsg)}
	 */
	public static Message sendCommandHelp(CommandEvent event, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), extraMsg);
	}

	/**
	 * Equivalent to calling
	 * {@link com.ptsmods.impulse.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event, String extraMsg) {
		return sendCommandHelp(channel, event, event.getCommand(), extraMsg);
	}

	public static Method getParentCommand(Subcommand child) {
		try {
			// format should be : package.class.method
			String parent = child.parent();
			String methodName = parent.split("\\.")[parent.split("\\.").length - 1];
			// this should make com.ptsmods.impulse.commands.Economy from
			// com.ptsmods.impulse.commands.Economy.bank
			Class clazz = Class.forName(joinCustomChar(".", removeArg(parent.split("\\."), parent.split("\\.").length - 1)), false, ClassLoader.getSystemClassLoader());
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
				if (!userCommandUsages.containsKey(event.getAuthor().getId())) userCommandUsages.put(event.getAuthor().getId(), newHashMap(new String[] {"lastUsedMillis", "used"}, new Long[] {System.currentTimeMillis(), 0L}));
				if (System.currentTimeMillis() - userCommandUsages.get(event.getAuthor().getId()).get("lastUsedMillis") < 10000) {
					if (userCommandUsages.get(event.getAuthor().getId()).get("used") <= 5) { // this is in a different if-statement so I can set used to 0 in an
																								// else-statement instead of an else-if-statement.
						userCommandUsages.get(event.getAuthor().getId()).put("used", userCommandUsages.get(event.getAuthor().getId()).get("used") + 1);
						userCommandUsages.get(event.getAuthor().getId()).put("lastUsedMillis", System.currentTimeMillis());
					}
				} else {
					userCommandUsages.get(event.getAuthor().getId()).put("used", 0L);
					userCommandUsages.get(event.getAuthor().getId()).put("lastUsedMillis", System.currentTimeMillis());
				}
				String prefix = getPrefix(event.getGuild());
				if (!event.getMessage().getRawContent().startsWith(prefix)) return;
				String[] parts = null;
				String rawContent = event.getMessage().getRawContent();
				if (rawContent.toLowerCase().startsWith(prefix.toLowerCase())) parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+", 2), 2);
				if (parts != null) if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
					String name = parts[0];
					String args = parts[1] == null ? "" : parts[1];
					int i = Main.getCommandIndex().getOrDefault(name.toLowerCase(), -1);
					if (i != -1) {
						if (userCommandUsages.get(event.getAuthor().getId()).get("used") > 5) {
							event.getChannel().sendMessageFormat("You have used more than 5 commands in the last 10 seconds, please wait %s before running another command.", formatMillis(10000 - (System.currentTimeMillis() - userCommandUsages.get(event.getAuthor().getId()).get("lastUsedMillis")))).queue();;
							return;
						}
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
						boolean dmOnly = false;
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
							dmOnly = annotation.dmOnly();
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
							dmOnly = annotation.dmOnly();
							serverOwnerCommand = annotation.serverOwnerCommand();
							ownerCommand = annotation.ownerCommand();
							cooldown = annotation.cooldown();
							sendTyping = annotation.sendTyping();
						}
						String errorMsg = "";
						boolean isCoOwner = isCoOwner(event.getAuthor());
						if (!event.getAuthor().getId().equals(Main.getOwner().getId()) && !isCoOwner) if (cooldowns.getOrDefault(event.getAuthor().getId(), new HashMap()).containsKey(command.toString()) && System.currentTimeMillis() - cooldowns.get(event.getAuthor().getId()).get(command.toString()) < cooldown * 1000)
							errorMsg = "You're still on cooldown, please try again in " + Main.formatMillis((long) (cooldown * 1000 - (System.currentTimeMillis() - cooldowns.get(event.getAuthor().getId()).get(command.toString()))), true, true, true, true, true, false) + ".";
						else if (event.getGuild() == null && guildOnly)
							errorMsg = "That command cannot be used in direct messages.";
						else if (event.getGuild() != null && dmOnly)
							errorMsg = "That command can only be used in DMs.";
						else if (serverOwnerCommand && event.getGuild() != null && event.getAuthor().getId().equals(event.getGuild().getOwner().getUser().getId()))
							errorMsg = "That command can only be used by this server's owner.";
						else if (ownerCommand)
							errorMsg = "That command can only be used by my owner.";
						else if (event.getMember() != null && !event.getMember().hasPermission(permissions)) {
							List<String> nonPresentPerms = new ArrayList();
							for (Permission perm : permissions)
								if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
							errorMsg = "You need the " + joinNiceString(nonPresentPerms) + " permissions to use that.";
						}
						if (event.getGuild() != null && !event.getGuild().getSelfMember().hasPermission(botPermissions)) {
							List<String> nonPresentPerms = new ArrayList();
							for (Permission perm : botPermissions)
								if (!event.getGuild().getMember(Main.getSelfUser()).hasPermission(perm)) nonPresentPerms.add(perm.getName());
							errorMsg = "I need the " + joinNiceString(nonPresentPerms) + " permissions to do that.";
						}
						if (!errorMsg.isEmpty())
							event.getChannel().sendMessage(errorMsg).queue(RestAction.DEFAULT_SUCCESS, t -> {
								if (t instanceof InsufficientPermissionException)
									try {
										sendPrivateMessage(event.getAuthor(), "I cannot parse the command as I don't have permissions to talk in the channel you ran the command in.");
									} catch (Exception e) {
									}
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
							} catch (Throwable e) {
							}
							command.setAccessible(true);
							try {
								if (!getOwner().getId().equals(event.getAuthor().getId())) {
									Map userCooldowns = cooldowns.getOrDefault(event.getAuthor().getId(), new HashMap());
									userCooldowns.put(command.toString(), System.currentTimeMillis());
									cooldowns.put(event.getAuthor().getId(), userCooldowns);
								}
								command.invoke(obj, cevent);
								usages.put(command, usages.getOrDefault(command, 0) + 1);
							} catch (InvocationTargetException e) {
								deleteCooldown(event.getAuthor(), command);
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
		event.getChannel().sendMessageFormat("A `%s` exception was thrown at line %s in %s while parsing the command%s.%s", e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), e.getMessage() != null ? String.format(": `%s`", e.getMessage()) : "", Main.devMode() ? "" : String.format("\nMy owner, %s, has been informed.", Main.getOwner().getAsMention())).queue();
		if (!Main.devMode()) {
			String output = String.format("A `%s` exception was thrown at line %s in %s while parsing the message `%s`. Stacktrace:\n```java\n%s```", e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), event.getMessage().getContent(), Main.generateStackTrace(e));
			while (output.length() > 1997) {
				Main.sendPrivateMessage(owner, output.substring(0, 1997) + "```");
				output = output.substring(1997);
			}
			if (!output.isEmpty()) Main.sendPrivateMessage(owner, output + "```");
		}
	}

	public static boolean isCoOwner(User user) {
		for (User coOwner : coOwners)
			if (coOwner != null && user.getId().equals(coOwner.getId())) return true;
		return false;
	}

	public static boolean isOwner(User user) {
		return user.getId().equals(getOwner().getId());
	}

	public static boolean hasOwnerPerms(User user) {
		return isOwner(user) || isCoOwner(user);
	}

	public static void deleteCooldown(User user, Method command) {
		Map userCooldowns = cooldowns.getOrDefault(user.getId(), new HashMap());
		if (userCooldowns.containsKey(command.toString())) {
			userCooldowns.remove(command.toString());
			cooldowns.put(user.getId(), userCooldowns);
		}
	}

	public static void runAsynchronously(Runnable runnable) {
		miscellaneousExecutor.execute(() -> {
			try {
				runnable.run();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

	public static void runAsynchronously(Object obj, Method method, Object... args) {
		runAsynchronously(() -> {
			try {
				method.invoke(obj, args);
			} catch (InvocationTargetException e) {
				e.getCause().printStackTrace();
			} catch (IllegalAccessException | IllegalArgumentException e) {
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
		return castArray(data.toArray(), args);
	}

	public static <T> T[] removeArgs(T[] args, Integer... arg) {
		for (int i : arg)
			args = removeArg(args, i);
		return args;
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
		return Collections.unmodifiableList(commands);
	}

	public static List<String> getCommandNames() {
		List<String> names = new ArrayList<>();
		for (Method cmd : commands)
			names.add(cmd.getAnnotation(Command.class).name());
		return names;
	}

	public static List<Method> getSubcommands() {
		return Collections.unmodifiableList(subcommands);
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
		int startSize = messages.get().size();
		long currentMillis = System.currentTimeMillis();
		while (true) {
			if (messages.get().isEmpty()) continue;
			Message lastMsg = messages.get().get(messages.get().size() - 1);
			if (messages.get().size() > startSize && lastMsg.getAuthor().getIdLong() == author.getUser().getIdLong() && (lastMsg.getGuild() == null || lastMsg.getGuild().getIdLong() == author.getGuild().getIdLong()) && lastMsg.getChannel().getIdLong() == channel.getIdLong())
				return lastMsg;
			else if (System.currentTimeMillis() - currentMillis >= timeoutMillis) return null;
			sleep(250);
		}
	}

	@Nullable
	public static Message waitForInput(MessageChannel channel, int timeoutMillis) {
		int startSize = messages.get().size();
		long currentMillis = System.currentTimeMillis();
		while (true) {
			if (messages.get().isEmpty()) continue;
			Message lastMsg = messages.get().get(messages.get().size() - 1);
			if (messages.get().size() > startSize && lastMsg.getChannel().getId().equals(channel.getId()) && !lastMsg.getAuthor().getId().equals(getSelfUser().getId()))
				return lastMsg;
			else if (System.currentTimeMillis() - currentMillis >= timeoutMillis) return null;
			sleep(250);
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
		if (seconds != 0 && millis != 0 && sortLogical)
			output[3] = "**" + seconds + "." + millis + "** seconds";
		else if (seconds != 0)
			output[3] = "**" + seconds + "** second" + (seconds != 1 ? "s" : "");
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
	 * This is equivalent to
	 * {@link com.ptsmods.impulse.Main#formatMillis(long, boolean, boolean, boolean, boolean, boolean, boolean)
	 * formatMillis(millis, true, true, true, true, false, false)}.
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
		case 'a':
			return '\u0250';
		case 'b':
			return 'q';
		case 'c':
			return '\u0254';
		case 'd':
			return 'p';
		case 'e':
			return '\u01DD';
		case 'f':
			return '\u025F';
		case 'g':
			return '\u0183';
		case 'h':
			return '\u0265';
		case 'i':
			return '\u0131'; // or \u0323
		case 'j':
			return '\u0638';
		case 'k':
			return '\u029E';
		case 'l':
			return '\u05DF';
		case 'm':
			return '\u026F';
		case 'n':
			return 'u';
		case 'o':
			return 'o';
		case 'p':
			return 'd';
		case 'q':
			return 'b';
		case 'r':
			return '\u0279';
		case 's':
			return 's';
		case 't':
			return '\u0287';
		case 'u':
			return 'n';
		case 'v':
			return '\u028C';
		case 'w':
			return '\u028D';
		case 'x':
			return 'x';
		case 'y':
			return '\u028E';
		case 'z':
			return 'z';
		case '[':
			return ']';
		case ']':
			return '[';
		case '(':
			return ')';
		case ')':
			return '(';
		case '{':
			return '}';
		case '}':
			return '{';
		case '?':
			return '\u00BF';
		case '\u00BF':
			return '?';
		case '!':
			return '\u00A1';
		case '\'':
			return ',';
		case ',':
			return '\'';
		default:
			return c;
		}
	}

	public static boolean addReceivedMessage(Message message) {
		return messages.get().add(message);
	}

	public static List<Message> getReceivedMessages() {
		return Collections.unmodifiableList(messages.get());
	}

	/**
	 * Just don't use it.
	 * Use {@link Guild#getMembersByName(String, boolean)} instead.
	 */
	@Deprecated
	public static User getUserByName(String name) {
		User user = null;
		for (JDA shard : shards)
			for (User user1 : shard.getUsers())
				if (user1.getName().equals(name)) {
					user = user1;
					break;
				}
		return user;
	}

	public static User getUserById(String id) {
		User user = null;
		for (JDA shard : shards)
			for (User user1 : shard.getUsers())
				if (user1.getId().equals(id)) {
					user = user1;
					break;
				}
		return user;
	}

	public static User getUserById(long id) {
		return getUserById(Long.toString(id));
	}

	public static String formatFileSize(double bytes) {
		return Downloader.formatFileSize(bytes);
	}

	public static double formatFileSizeDoubleMb(double bytes) {
		return Downloader.formatFileSizeDoubleMb(bytes);
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
			if (x + 1 == list.size())
				output += list.get(x).toString();
			else if (x + 2 != list.size())
				output += list.get(x).toString() + ", ";
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

	public static Integer getIntFromPossibleDouble(Object d) {
		if (d instanceof Double)
			return ((Double) d).intValue();
		else if (d instanceof Integer)
			return (Integer) d;
		else try {
			return (int) d;
		} catch (Throwable e) {
			return -1;
		}
	}

	public static Long getLongFromPossibleDouble(Object d) {
		if (d instanceof Double)
			return ((Double) d).longValue();
		else if (d instanceof Long)
			return (Long) d;
		else try {
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
	 *
	 * @param text
	 *            The text that needs encoding.
	 * @param encodeAlphanumeric
	 *            A boolean which when true will also encode characters like a, b,
	 *            c, 1, 2, and 3.
	 * @return A never-null String encoded for use with URLs (which is probably 3
	 *         times as long as the input text).
	 */
	public static String percentEncode(String text, boolean encodeAlphanumeric) {
		String[] parts = text.split("\n");
		String[] outputParts = new String[parts.length];
		for (int x = 0; x < parts.length; x++) {
			String output = "";
			for (Character ch : parts[x].toCharArray())
				if (encodeAlphanumeric || !isAlphanumeric(ch))
					output += "%" + Integer.toHexString(ch);
				else output += ch;
			outputParts[x] = output;
		}
		return joinCustomChar("%0A", outputParts);
	}

	public static boolean isAlphanumeric(char ch) {
		ch = Character.toLowerCase(ch);
		return ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9';
	}

	public static <K, V> Map<K, V> reverseMap(Map<K, V> map) {
		Map<K, V> copy = new HashMap<>(map);
		map.clear();
		List<K> keys = Lists.reverse(new ArrayList(copy.keySet()));
		List<V> values = Lists.reverse(new ArrayList(copy.values()));
		for (int i : range(keys.size()))
			map.put(keys.get(i), values.get(i));
		return map;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		return reverseMap(map.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
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
				if (getMethod(clazz.getMethods()) != null)
					return getMethod(clazz.getMethods());
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

	@Nullable
	public static Field getField(Class clazz, String field, @Nullable Class type) {
		return new Object() {
			Field getField() {
				if (getField(clazz.getFields()) != null)
					return getField(clazz.getFields());
				else return getField(clazz.getDeclaredFields());
			}

			Field getField(Field[] fields) {
				for (Field field1 : fields)
					if (field1.getName().equals(field) && field1.getType() == type) return field1;
				return null;
			}
		}.getField();
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
		case "OFFLINE": {
			return OnlineStatus.OFFLINE;
		}
		case "INVISIBLE": {
			return OnlineStatus.INVISIBLE;
		}
		case "DND": {
			return OnlineStatus.DO_NOT_DISTURB;
		}
		case "DO_NOT_DISTURB": {
			return OnlineStatus.DO_NOT_DISTURB;
		}
		case "IDLE": {
			return OnlineStatus.IDLE;
		}
		case "ONLINE": {
			return OnlineStatus.ONLINE;
		}
		default: {
			return OnlineStatus.UNKNOWN;
		}
		}
	}

	public static void setAvatar(Icon avatar) {
		shards.get(0).getSelfUser().getManager().setAvatar(avatar).queue();
	}

	public static String getPrefix(Guild guild) {
		return guild == null ? globalPrefix : serverPrefixes.getOrDefault(guild.getId(), globalPrefix);
	}

	public static SelfUser getSelfUser() {
		return shards.get(0).getSelfUser();
	}

	/**
	 * @return Basically the same as what gets printed when you do
	 *         {@link java.lang.Throwable#printStackTrace()
	 *         cause.printStackTrace()}.
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
	 * If this throws a
	 * {@link com.ptsmods.impulse.miscellaneous.CommandPermissionException
	 * CommandPermissionException} then the command isn't executed and instead the
	 * message given with the exception is sent.
	 *
	 * @param hook
	 *            The hook to add.
	 */
	public static void addCommandHook(CommandExecutionHook hook) {
		commandHooks.add(hook);
	}

	public static List<CommandExecutionHook> getCommandHooks() {
		return commandHooks;
	}

	public static List<Method> getSubcommands(Method parent) {
		return linkedSubcommands.getOrDefault(parent.toString(), new ArrayList());
	}

	/**
	 * @param obj
	 *            The object to clone
	 * @return A cloned version of the given object.
	 * @author WillingLearner&nbsp;(https://stackoverflow.com/a/25338780)
	 */
	public static <T> T clone(T obj) {
		try {
			Object clone = obj.getClass().newInstance();
			for (Field field : obj.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(obj) == null || Modifier.isFinal(field.getModifiers())) continue;
				if (field.getType().isPrimitive() || field.getType().equals(String.class) || field.getType().getSuperclass() != null && field.getType().getSuperclass().equals(Number.class) || field.getType().equals(Boolean.class))
					field.set(clone, field.get(obj));
				else {
					Object childObj = field.get(obj);
					if (childObj == obj)
						field.set(clone, clone);
					else field.set(clone, clone(field.get(obj)));
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
		if (list == null || index >= list.size() || index < 0)
			return fallback;
		else return list.get(index);
	}

	public static final boolean done() {
		return done;
	}

	public static void sleep(long millis) {
		sleep(millis, TimeUnit.MILLISECONDS);
	}

	public static void sleep(long units, TimeUnit unit) {
		long start = System.currentTimeMillis();
		try {
			unit.sleep(units);
		} catch (InterruptedException e) {
			// TimeUnit.sleep requires way less CPU, but this is only if the Thread was
			// interrupted.
			long stop = System.currentTimeMillis() - (start + unit.toMillis(units) - System.currentTimeMillis());
			while (System.currentTimeMillis() < stop);
		}
	}

	public static String colourToHex(Color colour) {
		return Integer.toHexString((colour == null ? new Color(0) : colour).getRGB()).toUpperCase().substring(2);
	}

	public static String plural(String object) {
		if (object.endsWith("o"))
			object += "es";
		else if (object.endsWith("y"))
			object = object.substring(0, object.length() - 1) + "ies";
		else if (object.endsWith("s"))
			;
		else object += "s";
		return object;
	}

	public static URL shorten(URL url) throws MalformedURLException {
		Response<ShortenResponse> resp = bitlyClient.shorten().setLongUrl(url.toString()).call();
		if (resp.status_txt.equals("INVALID_URI"))
			throw new MalformedURLException("The given URL was invalid, according to bit.ly.");
		else return new URL(resp.data.url);
	}

	public static long getTime(TimeType type) {
		switch (type) {
		case SECONDS:
			return System.currentTimeMillis() / 1000;
		case MILLISECONDS:
			return System.currentTimeMillis();
		case MICROSECONDS:
			return System.nanoTime() / 1000;
		case NANOSECONDS:
			return System.nanoTime();
		default:
			return -1;
		}
	}

	public static <T> String str(T obj) {
		if (obj instanceof User)
			return obj == null ? "null#0000" : ((User) obj).getName() + "#" + ((User) obj).getDiscriminator();
		else if (obj instanceof Member)
			return obj == null ? "null#0000" : ((Member) obj).getUser().getName() + "#" + ((Member) obj).getUser().getDiscriminator();
		else if (obj instanceof Guild)
			return obj == null ? "null" : ((Guild) obj).getName();
		else if (obj instanceof Channel)
			return obj == null ? "null" : ((Channel) obj).getName();
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
		return new RoleImpl(role.getIdLong(), role.getGuild()).setColor(role.getColor()).setHoisted(role.isHoisted()).setManaged(role.isManaged()).setMentionable(role.isMentionable()).setName(role.getName()).setRawPermissions(role.getPermissionsRaw()).setRawPosition(role.getPositionRaw());
	}

	public static TextChannel cloneChannel(TextChannel channel) {
		TextChannelImpl channel1 = new TextChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild()).setName(channel.getName()).setNSFW(channel.isNSFW()).setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong()).setRawPosition(channel.getPositionRaw()).setTopic(channel.getTopic());
		try {
			channel1.setLastMessageId(channel.getLatestMessageIdLong());
		} catch (IllegalStateException e) {
		}
		return channel1;
	}

	public static VoiceChannel cloneChannel(VoiceChannel channel) {
		return new VoiceChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild()).setBitrate(channel.getBitrate()).setName(channel.getName()).setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong()).setRawPosition(channel.getPositionRaw()).setUserLimit(channel.getUserLimit());
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
	 *
	 * @param sound
	 *            {@code new File(fileLocation).getAbsoluteFile()}
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
	 *
	 * @param sound
	 *            {@code class.getClassLoader().getResourceAsStream(fileLocation)}
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
	 *
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

	public static URL getResource(String name) {
		return Main.class.getResource(name) == null ? Main.class.getClassLoader().getResource(name) : Main.class.getResource(name);
	}

	public static boolean isSuperClass(Class clazz, Class superClass) {
		while (clazz.getSuperclass() != null)
			if (clazz.getSuperclass().equals(superClass))
				return true;
			else clazz = clazz.getSuperclass();
		return false;
	}

	public static ThreadPoolExecutor getThreadPool(String name) {
		switch (name.toLowerCase()) {
		case "miscellaneous":
			return miscellaneousExecutor;
		case "commands":
			return commandsExecutor;
		default:
			return null;
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

	public static Method getCallerMethod() {
		StackTraceElement ste = new Exception().getStackTrace()[3];
		try {
			return getMethod(Class.forName(ste.getClassName(), false, ClassLoader.getSystemClassLoader()), ste.getMethodName());
		} catch (ClassNotFoundException ignored) { // should not be possible
			return null;
		}
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
		while (string.startsWith(" "))
			string = string.substring(1, string.length());
		while (string.endsWith(" "))
			string = string.substring(0, string.length() - 1);
		return string;
	}

	/**
	 * HtmlUnit can be used to bypass APIs you have to pay for by for example just
	 * using their website, e.g. cleverbot has a paid API, but using HtmlUnit you
	 * can use their main website to still use cleverbot without paying for it.
	 */
	public static WebClient newSilentWebClient() {
		WebClient client = new WebClient(BrowserVersion.BEST_SUPPORTED);
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
		client.setCssErrorHandler(new ErrorHandler() {
			@Override
			public void error(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void warning(CSSParseException arg0) throws CSSException {
			}
		});
		client.setJavaScriptErrorListener(new JavaScriptErrorListener() {
			@Override
			public void scriptException(HtmlPage page, ScriptException scriptException) {
			}

			@Override
			public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
			}

			@Override
			public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
			}

			@Override
			public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
			}
		});
		client.setIncorrectnessListener(new IncorrectnessListener() {
			@Override
			public void notify(String message, Object origin) {
			}
		});
		client.setHTMLParserListener(new HTMLParserListener() {
			@Override
			public void error(String message, URL url, String html, int line, int column, String key) {
			}

			@Override
			public void warning(String message, URL url, String html, int line, int column, String key) {
			}
		});
		client.getOptions().setCssEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		return client;
	}

	public static void toLowerCase(List<String> strings) {
		for (String string : new ArrayList<>(strings)) {
			strings.remove(string);
			strings.add(string.toLowerCase());
		}
	}

	public static int getOpposite(int max, int current) {
		if (current > max)
			throw new IllegalArgumentException("Argument 'current' cannot be greater than 'max'.");
		else return max == current ? 0 : reverse(range(max))[current] + 1;
	}

	public static <T> T[] reverse(T[] array) {
		ArrayUtils.reverse(array);
		return array;
	}

	public static <T> List<T> removeNulls(List<T> list) {
		list = new ArrayList(list);
		while (new ArrayList(list).contains(null))
			list.remove(null);
		return list;
	}

	public static synchronized boolean isShuttingDown() {
		return shutdown;
	}

	public static JSONObject commandsToJson() {
		long totalUsages = 0;
		JSONObject commands = new JSONObject();
		try {
			setMapToArrayMap(commands, new ArrayMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Method command : getCommands()) {
			Command cann = command.getAnnotation(Command.class);
			List<String> botPerms = new ArrayList();
			for (Permission perm : cann.botPermissions())
				botPerms.add(perm.getName());
			List<String> userPerms = new ArrayList();
			for (Permission perm : cann.userPermissions())
				userPerms.add(perm.getName());
			ArrayMap map = new ArrayMap();
			map.put("help", cann.help());
			map.put("category", cann.category());
			map.put("guildOnly", cann.guildOnly());
			map.put("dmOnly", cann.dmOnly());
			map.put("hidden", cann.hidden());
			map.put("sendTyping", cann.sendTyping());
			map.put("arguments", cann.arguments());
			map.put("botPermissions", botPerms);
			map.put("userPermissions", userPerms);
			map.put("cooldown", cann.cooldown());
			map.put("serverOwnerCommand", cann.serverOwnerCommand());
			map.put("ownerCommand", cann.ownerCommand());
			map.put("requiredRole", cann.requiredRole());
			map.put("obeyDashboard", cann.obeyDashboard());
			map.put("usages", usages.getOrDefault(command, 0));
			JSONObject mapJ = new JSONObject();
			try {
				setMapToArrayMap(mapJ, map);
			} catch (Exception e) {
				e.printStackTrace();
			}
			commands.put(cann.name(), mapJ);
			totalUsages += (int) map.get("usages");
		}
		JSONObject object = new JSONObject();
		try {
			setMapToArrayMap(object, new ArrayMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		object.put("count", getCommands().size());
		object.put("totalUsages", totalUsages);
		object.put("commands", commands);
		return object;
	}

	public static void setMapToArrayMap(JSONObject object, ArrayMap map) throws Exception {
		Field mapField = getField(JSONObject.class, "map", Map.class);
		mapField.setAccessible(true);
		deleteFinalModifier(mapField);
		mapField.set(object, map);
	}

	public static void deleteFinalModifier(Field field) throws Exception {
		if (Modifier.isFinal(field.getModifiers())) {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		}
	}

	public static String getImageType(String URL) throws IOException {
		URL url = new URL(URL);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		String type = connection.getHeaderField("Content-Type");
		if (type.startsWith("image/"))
			return type.substring(6).split(";")[0];
		else return "unknown";
	}

	public static InputStream openStream(String url) throws IOException {
		return openStream(newUrl(url));
	}

	public static InputStream openStream(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		return connection.getInputStream();
	}

	public static String getAsOrdinal(int i) {
		String ordinal = "" + i;
		if (ordinal.endsWith("1"))
			ordinal += "st";
		else if (ordinal.endsWith("2"))
			ordinal += "nd";
		else if (ordinal.endsWith("3"))
			ordinal += "rd";
		else ordinal += "th";
		return ordinal;
	}

	public static String getUnicode(char ch) {
		return Integer.toHexString(ch | 0x10000).substring(1);
	}

	public static char fromUnicode(String unicode) {
		return (char) Integer.parseInt(unicode, 16);
	}

	public static String stringToWingdings(String string) {
		String wingdings = "";
		for (char ch : string.toCharArray())
			wingdings += (char) Integer.parseInt(getUnicode(ch).replace("00", "F0"), 16);
		return wingdings;
	}

	/**
	 * @param code
	 *            This should <b>only</b> be the code in the main method, the class
	 *            and the main method are created automatically.
	 * @throws Exception
	 * @author PlanetTeamSpeak
	 */
	public static Object compileAndRunJavaCode(String code, Map<String, Object> variables, List<Class> extraImportClasses, boolean beStatic) throws Throwable {
		AtomicObject<Process> process = new AtomicObject();
		AtomicReference returnValue = new AtomicReference();
		if (!runWithTimeout(15000, () -> {
			try {
				final Map<String, Object> variables0 = variables == null ? new HashMap() : variables;
				new ArrayList();
				String fileName = "TempClass_" + Random.randInt();
				while (new File("data/tmp/" + fileName + ".jar").exists())
					fileName = "TempClass_" + Random.randInt();
				File source = new File("data/tmp/" + fileName + ".java");
				String code0 = "public class " + fileName + " {public " + (beStatic ? "static " : "") + "Object returnValue = null; public " + (beStatic ? "static " : "") + "final void run(Class<" + fileName + "> thisClass, ";
				for (String var : variables0.keySet())
					code0 += variables0.get(var).getClass().getName() + " " + var + ", ";
				code0 = code0.substring(0, code0.length() - 2);
				code0 += ") {\n" + (code.contains("return ") ? code.substring(0, code.lastIndexOf("return ")) + "returnValue = " + code.substring(code.lastIndexOf("return ") + 7, code.length()) : code) + ";\n}}";
				List<Class> varClasses = Lists.newArrayList(Class.class);
				for (Object var : variables0.values())
					varClasses.add(var.getClass());
				List<Class> importClasses = new ArrayList();
				importClasses.addAll(varClasses);
				importClasses.addAll(new Reflections("com.ptsmods.impulse", new SubTypesScanner(false)).getSubTypesOf(Object.class));
				importClasses.addAll(new Reflections("net.dv8tion.jda", new SubTypesScanner(false)).getSubTypesOf(Object.class));
				List<Class> importedClasses = new ArrayList();
				List<String> blacklist = devMode ? Lists.newArrayList("net.dv8tion.jda.core.utils.SimpleLog") : new ArrayList();
				blacklist.add("com.ptsmods.impulse.MainGUI"); // deprecated class.
				for (Class clazz : importClasses) {
					boolean shouldBreak = blacklist.contains(clazz.getName()) || clazz.getName().contains("$");
					for (Class clazz1 : importedClasses)
						if (clazz.getSimpleName().equals(clazz1.getSimpleName()) && clazz != clazz1) {
							shouldBreak = true;
							break;
						}
					if (!shouldBreak) {
						code0 = "import " + clazz.getName() + "; " + code0;
						importedClasses.add(clazz);
					}
				}
				Files.write(code0.getBytes("UTF-8"), source);
				String[] args = new String[] {"-classpath", "\"" + getJarFile().getAbsolutePath() + "\"", "-d", "data/tmp/", "\"" + source.getAbsolutePath() + "\""};
				process.set(Runtime.getRuntime().exec("javac " + joinCustomChar(" ", args)));
				process.get().waitFor();
				String error;
				if (!(error = convertStreamToString(process.get().getErrorStream())).isEmpty()) {
					char[] chars = error.split("\n")[2].toCharArray();
					int character = 0;
					for (int i : range(chars.length))
						if (chars[i] == '^') {
							character = i;
							break;
						}
					returnValue.set(new CompilationException(code, character < 0 ? 0 : character, "Error:" + error.split("\n")[0].split(": error:")[1]));
					source.delete();
					return;
				}
				process.set(Runtime.getRuntime().exec("jar cvf " + fileName + ".jar " + fileName + ".class", null, new File("data/tmp/")));
				process.get().waitFor();
				addJarToClassPath(new File("data/tmp/" + fileName + ".jar"));
				List varvs = new ArrayList(variables0.values());
				varvs.add(0, Class.forName(fileName));
				Object instance = beStatic ? null : Class.forName(fileName).newInstance();
				getMethod(Class.forName(fileName), "run", varClasses.toArray(new Class[0])).invoke(instance, varvs.toArray());
				returnValue.set(getField(Class.forName(fileName), "returnValue", Object.class).get(instance));
				source.delete();
				new File("data/tmp/" + fileName + ".class").delete();
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
				returnValue.set(e.getCause());
			} catch (Exception e) {
				e.printStackTrace();
				returnValue.set(e);
			}
		}) && process.get() != null) process.get().destroyForcibly();
		if (returnValue.get() instanceof Throwable) throw (Throwable) returnValue.get();
		return returnValue.get();
	}

	public static void addJarToClassPath(File jar) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new File(jar.getAbsolutePath()).toURI().toURL());
	}

	@SuppressWarnings("unused") // revision is not 0 in every version and since it's final it'll give a warning.
	public static File getJarFile() {
		if (!devMode)
			try {
				CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
				File jarFile;
				if (codeSource.getLocation() != null)
					jarFile = new File(codeSource.getLocation().toURI());
				else {
					String path = Main.class.getResource(Main.class.getSimpleName() + ".class").getPath();
					String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
					jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
					jarFile = new File(jarFilePath);
				}
				return jarFile;
			} catch (Exception e) {
				throwCheckedExceptionWithoutDeclaration(e);
				return null;
			}
		else return new File("E:\\MEGA\\Impulse Java\\Impulse\\build\\libs\\Impulse-" + major + "." + (revision == 0 ? minor - 1 : minor) + ".0-stable-all.jar");
	}

	public static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static boolean runWithTimeout(int timeoutMillis, Runnable runnable) {
		long started = System.currentTimeMillis();
		AtomicBoolean finished = new AtomicBoolean();
		Thread thread = new Thread(() -> {
			runnable.run();
			finished.set(true);
		});
		thread.start();
		while (System.currentTimeMillis() - started < timeoutMillis && !finished.get());
		if (!finished.get()) thread.interrupt();
		return finished.get();
	}

	public static void deleteAllFilesInDir(File dir) {
		if (dir.isDirectory()) for (File file : dir.listFiles()) {
			deleteAllFilesInDir(file);
			file.delete();
		}
	}

	public static String getFullName(Method command) {
		String name = "";
		if (command.isAnnotationPresent(Command.class))
			name = command.getAnnotation(Command.class).name();
		else if (command.isAnnotationPresent(Subcommand.class)) {
			while (Main.getParentCommand(command.getAnnotation(Subcommand.class)) != null) {
				name = command.getAnnotation(Subcommand.class).name() + " " + name;
				command = Main.getParentCommand(command.getAnnotation(Subcommand.class));
			}
			name = Main.getParentCommand(command.getAnnotation(Subcommand.class)).getAnnotation(Command.class).name() + " " + name;
		}
		return name.isEmpty() ? "Unknown" : name;
	}

	public static BufferedImage snapshot(JComponent component) {
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
		component.paint(image.getGraphics());
		return image;
	}

	public static BufferedImage snapshot(Node node) {
		AtomicReference returnValue = new AtomicReference();
		Platform.runLater(() -> {
			returnValue.set(ImageManipulator.toBufferedImage(node.snapshot(new SnapshotParameters(), null)));
		});
		while (returnValue.get() == null)
			sleep(25);
		return (BufferedImage) returnValue.get();
	}

	/**
	 * Sets the {@link java.lang.Boolean#FALSE Boolean#FALSE} field to {@code true}
	 * which means everytime the {@code false} keyword is used, it's actually
	 * {@code true}.
	 *
	 * @return The last {@code false} in existence here.
	 * @throws Exception
	 */
	public static boolean fuckThingsUp() throws Exception {
		boolean lastFalse = false;
		Field field = getField(Boolean.class, "FALSE", Boolean.class);
		field.setAccessible(true);
		deleteFinalModifier(field);
		field.set(null, true);
		return lastFalse;
	}

	public static TextChannel getSendChannel(Guild guild) {
		if (guild.getId().equals("110373943822540800"))
			return null;
		else if (guild.getDefaultChannel() != null && guild.getDefaultChannel().canTalk())
			return guild.getDefaultChannel();
		else for (TextChannel channel : guild.getTextChannels())
			if (channel.canTalk()) return channel;
		return null;
	}

	/**
	 * Returns an instance of the given class without calling any constructors. This
	 * calls nor instance constructors nor static constructors.
	 *
	 * @param clazz
	 *            The class to make an instance of.
	 * @return An instance of the given class.
	 * @throws InstantiationException
	 *             Idk when this is thrown, Unsafe secrets, ig.
	 */
	public <T> T getInstanceWithoutConstructor(Class<T> clazz) throws InstantiationException {
		return (T) theUnsafe.allocateInstance(clazz);
	}

	/**
	 * Using this class can really mess things up, you can, for instance, use it to
	 * create arrays bigger than the RAM of the host pc.
	 *
	 * @return
	 */
	@Deprecated
	public Unsafe getUnsafe() {
		return theUnsafe;
	}

	/**
	 * This method uses {@link sun.misc.Unsafe Unsafe} to throw a Throwable which
	 * doesn't extend RuntimeException, if it does, it's not a problem, without it
	 * having to be declared in the Method.
	 *
	 * @param t
	 *            The throwable which has to be thrown.
	 */
	public static void throwCheckedExceptionWithoutDeclaration(Throwable t) {
		theUnsafe.throwException(t);
	}

	public static boolean hasPermissions(TextChannel channel, User user, Permission... permissions) {
		return channel.getPermissionOverride(channel.getGuild().getMember(user)) == null ? channel.getGuild().getMember(user).hasPermission(permissions) : channel.getPermissionOverride(channel.getGuild().getMember(user)).getAllowed().containsAll(Lists.newArrayList(permissions)) || channel.getGuild().getMember(user).hasPermission(permissions);
	}

	public enum TimeType {
		DAYS(TimeUnit.DAYS),
		HOURS(TimeUnit.HOURS),
		MINUTES(TimeUnit.MINUTES),
		SECONDS(TimeUnit.SECONDS),
		MILLISECONDS(TimeUnit.MILLISECONDS),
		MICROSECONDS(TimeUnit.MICROSECONDS),
		NANOSECONDS(TimeUnit.NANOSECONDS);

		private final TimeUnit unit;

		private TimeType(TimeUnit unit) {
			this.unit = unit;
		}

		public float toSeconds() {
			switch (this) {
			case DAYS:
				return 86400f;
			case HOURS:
				return 3600f;
			case MINUTES:
				return 60f;
			case SECONDS:
				return 1f;
			case MILLISECONDS:
				return 0.0001f;
			case MICROSECONDS:
				return 0.00000001f;
			case NANOSECONDS:
				return 0.000000000001f;
			default:
				return -1f;
			}
		}

		public float toMilliseconds() {
			return toSeconds() * 1000f;
		}

		public TimeUnit toTimeUnit() {
			return unit;
		}
	}

	public enum LogType {
		ERROR("ERROR", ConsoleColours.RED),
		WARN("WARN", ConsoleColours.YELLOW),
		INFO("INFO", ConsoleColours.UNKNOWN),
		DEBUG("DEBUG", ConsoleColours.HIGHINTENSITY_BLUE);

		public final String			name;
		public final ConsoleColours	colour;

		private LogType(String name, ConsoleColours colour) {
			this.name = name;
			this.colour = colour;
		}

	}

	public static class CompilationException extends Exception {

		private static final long	serialVersionUID	= 1234078188854932430L;
		private final String		line;
		private final int			character;
		private final String		message;

		public CompilationException(String line, int character, String message) {
			super();
			this.line = line;
			this.character = character;
			this.message = message;
		}

		public String getLine() {
			return line;
		}

		public int getCharacter() {
			return character;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return message + "\n" + line + "\n" + (character == 0 ? "" : multiplyString(" ", character - 1)) + "^";
		}

		@Override
		public void printStackTrace(PrintStream stream) {
			print(LogType.ERROR, toString());
		}

		@Override
		public void printStackTrace(PrintWriter writer) {
			print(LogType.ERROR, toString());
		}

	}

}
