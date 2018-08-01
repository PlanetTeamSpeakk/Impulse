package com.impulsebot;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JComponent;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.impulsebot.miscellaneous.EventHandler;
import com.impulsebot.miscellaneous.ImpulseSM;
import com.impulsebot.miscellaneous.SubscribeEvent;
import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.DataPacket;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.SideOnly;
import com.impulsebot.net.SideOnly.Side;
import com.impulsebot.net.client.ClientDataRequestPacket;
import com.impulsebot.net.client.ClientIdentificationPacket;
import com.impulsebot.net.client.ClientLoginPacket;
import com.impulsebot.net.server.ServerClientIdentifierPacket;
import com.impulsebot.net.server.ServerConnectionRefusedPacket;
import com.impulsebot.utils.ArrayMap;
import com.impulsebot.utils.ArraySet;
import com.impulsebot.utils.AtomicObject;
import com.impulsebot.utils.Config;
import com.impulsebot.utils.ConsoleColours;
import com.impulsebot.utils.Dashboard;
import com.impulsebot.utils.DataIO;
import com.impulsebot.utils.Downloader;
import com.impulsebot.utils.HookedPrintStream;
import com.impulsebot.utils.HookedPrintStream.PrintHook;
import com.impulsebot.utils.ImageManipulator;
import com.impulsebot.utils.Random;
import com.impulsebot.utils.commands.Command;
import com.impulsebot.utils.commands.CommandContainer;
import com.impulsebot.utils.commands.CommandEvent;
import com.impulsebot.utils.commands.CommandExecutionHook;
import com.impulsebot.utils.commands.CommandManager;
import com.impulsebot.utils.commands.CommandPermissionException;
import com.impulsebot.utils.commands.ConsoleCommandEvent;
import com.impulsebot.utils.commands.Extension;
import com.impulsebot.utils.commands.Subcommand;
import com.impulsebot.utils.compiler.CompilationException;
import com.impulsebot.utils.compiler.MemoryJavaCompiler;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.exc.ReqlDriverError;
import com.rethinkdb.net.Connection;
import com.thoughtworks.xstream.XStream;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.RoleImpl;
import net.dv8tion.jda.core.entities.impl.TextChannelImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.entities.impl.VoiceChannelImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import sun.misc.Unsafe;
import sun.reflect.Reflection;

public class Main implements Extension {

	public static final int												major					= 2;
	public static final int												minor					= 0;
	public static final int												revision				= 0;
	public static final String											type					= "beta";
	public static final String											version					= String.format("%s.%s.%s-%s", major, minor, revision, type);
	public static final Object											nil						= null;
	public static final Date											started					= new Date();
	public static final Map<String, String>								apiKeys					= new HashMap<>();
	public static final Thread											mainThread				= Thread.currentThread();
	public static final List<Permission>								defaultPermissions		= Collections.unmodifiableList(getDefaultPermissions());
	public static final Permission[]									defaultPermissionsArray	= defaultPermissions.toArray(new Permission[0]);
	private static Unsafe												theUnsafe;
	private static final BitlyClient									bitlyClient;
	private static volatile boolean										done					= false;
	private static final ThreadPoolExecutor								commandsExecutor		= newTPE();
	private static final ThreadPoolExecutor								miscellaneousExecutor	= newTPE();
	private static final String											osName					= System.getProperty("os.name");
	private static final boolean										isWindows				= osName.toLowerCase().contains("windows");
	private static final boolean										isMac					= osName.toLowerCase().contains("mac");
	private static final boolean										isUnix					= osName.toLowerCase().contains("nix") || osName.toLowerCase().contains("nux") || osName.toLowerCase().contains("aix");
	private static final boolean										isSolaris				= osName.toLowerCase().contains("sunos");
	private static List<CommandExecutionHook>							commandHooks			= new ArrayList<>();
	@SideOnly(Side.CLIENT) private static JDA							jda;
	private static boolean												devMode					= false;
	private static boolean												eclipse					= false;
	private static boolean												headless				= false;
	private static boolean												useSwing				= false;
	private static User													owner					= null;
	@SideOnly(Side.SERVER) private static SelfUser						self					= null;
	private static List<User>											coOwners				= new ArrayList();
	private static AtomicObject<List<Message>>							messages				= new AtomicObject(new ArrayList<>());
	private static EventHandler											eventHandler			= new EventHandler();
	private static Map<String, Map<String, Long>>						cooldowns				= new HashMap();
	private static volatile boolean										shutdown				= false;
	private static Map<Method, Integer>									usages					= new HashMap();
	private static Map<String, String>									serverPrefixes			= new HashMap();
	private static String												globalPrefix			= "";
	private static Map<String, Map<String, Long>>						userCommandUsages		= new HashMap();
	private static Connection											rdbConnection;
	private static String												id;
	private static Config												Config					= com.impulsebot.utils.Config.INSTANCE;
	@SideOnly(Side.CLIENT) private static int							shardId;
	@SideOnly(Side.SERVER) private static int							shardCount;
	@SideOnly(Side.SERVER) private static Map<String, Integer>			identifications			= new HashMap();
	@SideOnly(Side.SERVER) private static Map<String, ChannelPipeline>	pipelines				= new HashMap();
	@SideOnly(Side.SERVER) private static Map<String, ChannelPipeline>	failures				= new HashMap();
	@SideOnly(Side.CLIENT) private static ChannelPipeline				pipeline;
	private static int													totalGuildCount;
	private static int													totalUserCount;
	private static XStream												xstream					= new XStream();
	private static Kryo													kryo					= new Kryo();

	static {
		try {
			kryo.setWarnUnregisteredClasses(false);
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

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return "The main commands of the Impulse discord bot.";
	}

	@Override
	public String getCommandsPackage() {
		return "com.impulsebot.commands";
	}

	@Override
	public void onEnable() {
		return;
	}

	@Override
	public void onDisable() {
		return;
	}

	public static final void main(String[] args) {
		List<String> argsList = Lists.newArrayList(args);
		devMode = argsList.contains("-devMode");
		eclipse = argsList.contains("-eclipse");
		headless = argsList.contains("-headless") || argsList.contains("-noGui");
		useSwing = argsList.contains("-useSwing") || argsList.contains("-noJfx");
		if (eclipse) {
			int lines = 0;
			for (File file : getFilesInDir(new File("src/main/java/com/impulsebot/")))
				try {
					for (String line : Files.readAllLines(file.toPath()))
						if (!line.split("//")[0].trim().isEmpty() && !line.startsWith("import")) lines += 1;
				} catch (IOException e) {
					e.printStackTrace();
				}
			print(LogType.DEBUG, "Impulse is currently made up of", lines, "lines of code! Woah!");
		}
		try {
			main0(args);
		} catch (Throwable e) {
			try {
				print(LogType.ERROR, "An unknown error occurred, please contact PlanetTeamSpeak#4157.", e);
			} catch (Throwable e1) {
				System.err.println("An unknown error occurred, please contact PlanetTeamSpeak#4157 " + e1 + "\n" + e);
			}
			e.printStackTrace();
			System.exit(1);
		}
		List<Class> classes = new ArrayList();
		classes.addAll(getClassesInPackage("net.dv8tion.jda.core.entities.impl"));
		classes.addAll(getClassesInPackage("net.dv8tion.jda.core.events"));
		for (Class clazz : classes)
			xstream.omitField(clazz, "api");
	}

	@SuppressWarnings("deprecation")
	private static final void main0(String[] args) throws Throwable {
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
				try {
					deleteAllFilesInDir(new File("data/tmp/"));
				} catch (Exception e) {
				}
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
		AtomicInteger shardCount = new AtomicInteger(-1);
		if (!isInteger(Config.get("shards")))
			print(LogType.WARN, "The value of key 'shards' isn't an integer, expecting 1.");
		else shardCount.set(Integer.parseInt(Config.get("shards")));
		if (shardCount.get() < 1) shardCount.set(1);
		Main.shardCount = shardCount.get();
		print(LogType.INFO, "Connecting to the database, please wait...");
		long millis = System.currentTimeMillis();
		try {
			rdbConnection = RethinkDB.r.connection().hostname(Config.getOrDefault("hostname", "localhost")).port(Integer.parseInt(Config.getOrDefault("port", "28015"))).user(Config.getOrDefault("username", "admin"), Config.getOrDefault("password", "")).db(Config.getOrDefault("database", "impulse")).connect();
		} catch (NullPointerException e) {
			print(LogType.ERROR, "A NullPointerException was thrown, this is most likely because the request did not get a response, are you sure the RethinkDB settings set in the config are correct?");
			e.printStackTrace();
			System.exit(1);
		} catch (ReqlDriverError e) {
			print(LogType.ERROR, "Could not connect to the database, are you sure the server is online?");
			System.exit(0);
		}
		if (!RethinkDB.r.dbList().contains(Config.getOrDefault("database", "impulse")).<Boolean>run(getRDBConnection())) {
			print(LogType.WARN, "The database set in the config does not yet exist, creating it now...");
			RethinkDB.r.dbCreate(Config.getOrDefault("database", "impulse")).run(getRDBConnection());
			print(LogType.SUCCESSFUL, "Successfully created database", Config.getOrDefault("database", "impulse") + "!");
		}
		print(LogType.SUCCESSFUL, "Successfully connected to the database! Took", System.currentTimeMillis() - millis, "milliseconds.");
		if (Lists.newArrayList(args).contains("-shard") || shardCount.get() == 1) {
			int shardId = -1;
			for (int i = 0; i < args.length; i++)
				if (args[i].equalsIgnoreCase("-shard") && i + 1 < args.length && isInteger(args[i + 1])) {
					shardId = Integer.parseInt(args[i + 1]);
					break;
				}
			Main.shardId = shardId;
			String hostname = "localhost";
			for (int i = 0; i < args.length; i++)
				if (args[i].equalsIgnoreCase("-hostname") && i + 1 < args.length) {
					hostname = args[i + 1];
					break;
				}
			EventLoopGroup group = new NioEventLoopGroup();
			try {
				Bootstrap clientBootstrap = new Bootstrap();
				clientBootstrap.group(group);
				clientBootstrap.channel(NioSocketChannel.class);
				clientBootstrap.remoteAddress(new InetSocketAddress(hostname, 62192));
				clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						print(LogType.INFO, "Client connection established.");
						socketChannel.pipeline().addLast(new ObjectEncoder(), new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(ClassLoader.getSystemClassLoader())), ClientHandler.INSTANCE);
						pipeline = socketChannel.pipeline();
					}
				});
				clientBootstrap.connect().sync();
				print(LogType.SUCCESSFUL, "Successfully started the client and connected to the server!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			globalPrefix = Config.get("prefix");
			print(LogType.INFO, "Loading commands...");
			CommandManager.initialize();
			print(LogType.INFO, CommandManager.getCommands().size(), "commands and", CommandManager.getSubcommands().size(), "subcommands loaded, loading server prefixes...");
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
			print(LogType.INFO, serverPrefixes.size(), "server prefixes loaded of which", guildsWithDefaultPrefix, "waiting for a packet from the server to log in...");
		} else {
			if (!headless) if (!useSwing)
				try {
					MainJFXGUI.startBlocking(args);
					MainJFXGUI.logLine("Welcome to Impulse v" + version + ", if you have any experience with JavaFX schemes and you'd like to change this GUI to look a bit better, make sure to take a look at the jfxGui.css file in this JAR file.", ConsoleColours.CYAN.toColour());
				} catch (IllegalAccessException e) {
					print(LogType.DEBUG, "Could not initialize the Main GUI, it seems to have already been initialized.");
				}
			else MainGUI.initialize();
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
				Config.put("kryptoKey", Random.INSTANCE.genKey(16));
				Config.addComment("The key used to send bot stats to https://carbinotex.net");
				Config.put("carbonitexKey", "");
				Config.addComment("The key used to send bot stats to https://discordbots.org");
				Config.put("discordBotListKey", "");
				Config.addComment("The key used to send bot stats to https://bots.discord.pw");
				Config.put("discordBotsKey", "");
				Config.addComment("The rest of the settings are for the RethinkDB database, if you do not know how to set up a RethinkDB server, go to https://rethinkdb.com for instructions as this is mandatory to have.");
				Config.addComment("The hostname where the RethinkDB server is hosted. Defaults to localhost.");
				Config.put("hostname", "localhost");
				Config.addComment("The port on which the RethinkDB server is hosted. Defaults to the RethinkDB default that being 28015.");
				Config.put("port", "28015");
				Config.addComment("The username of the account to use to log in on the RethinkDB server, to make a new user, go to http://hostname:8080 where hostname is the aforementioned hostname, click on 'Data Explorer' on the top and enter r.db('rethinkdb').table('users').insert({id: '<username>', password: '<password>'}) and click run, it should say inserted: 1.. Defaults to admin.");
				Config.put("username", "admin");
				Config.addComment("The password of the aforementioned account to use to log in on the RethinkDB server.");
				Config.put("password", "");
				Config.addComment("The database to connect to, this database does not have to exist already. Defaults to impulse.");
				Config.put("database", "impulse");
				Config.addComment("The prefix of the table names, this could be left blank if the set database is only for this bot, but you should not change this later on since it will reset all data. Defaults to ip_.");
				Config.put("tablePrefix", "ip_");
				print(LogType.WARN, "The config hasn't been changed yet, please go to the settings tab or open config.cfg and change the variables.");
				return;
			} else if (Config.get("token").isEmpty() || Config.get("ownerId").isEmpty() || Config.get("prefix").isEmpty() || Config.get("shards").isEmpty()) {
				print(LogType.WARN, "The config hasn't been changed yet, please go to the settings tab or open config.cfg and change the variables.");
				return;
			}
			print(LogType.INFO, "Starting the server, please wait...");
			EventLoopGroup group = new NioEventLoopGroup();
			try {
				ServerBootstrap serverBootstrap = new ServerBootstrap();
				serverBootstrap.group(group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress("localhost", 62192));
				serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
						ChannelPipeline pipeline = socketChannel.pipeline();
						if (pipelines.size() < shardCount.get()) {
							print(LogType.DEBUG, "Server connection established.", socketChannel.id());
							// Aww yiss, 2 gb packets, why not?
							pipeline.addLast(new ObjectEncoder(), new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(ClassLoader.getSystemClassLoader())), new ServerHandler());
							pipeline.writeAndFlush(new DataPacket(new ServerClientIdentifierPacket(socketChannel.id())));
							pipelines.put(socketChannel.id().asLongText(), pipeline);
						} else {
							pipeline.addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(ClassLoader.getSystemClassLoader())));
							pipeline.writeAndFlush(new DataPacket(new ServerConnectionRefusedPacket()));
						}
					}
				});
				serverBootstrap.bind().sync();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			try {
				Dashboard.initialize();
			} catch (IOException e) {
				print(LogType.ERROR, "The Dashboard could not be initialized, are you sure port 61192 isn't used?");
			}
			print(LogType.SUCCESSFUL, "Successfully started the server! Waiting for the clients...");
		}
	}

	public static void login() {
		print(LogType.INFO, "Logging in, please wait...");
		Throwable cause = null;
		try {
			eventHandler = new EventHandler();
			try {
				jda = new JDABuilder(AccountType.BOT).setToken(Config.get("token")).addEventListener(eventHandler).useSharding(shardId, shardCount).setGame(Game.playing("Starting...")).buildBlocking();
			} catch (LoginException | InterruptedException e1) {
				e1.printStackTrace();
			}
			owner = getUserById(Config.get("ownerId"));
			for (String coOwnerId : (Config.get("coOwnerIds") == null ? "" : Config.get("coOwnerIds")).split(";"))
				try {
					coOwners.add(getUserById(coOwnerId));
				} catch (Exception e) {
				}
		} catch (Throwable e) {
			cause = e;
		}
		ClientHandler.INSTANCE.sendPacket(new ClientLoginPacket(cause == null, owner != null, cause, jda.getSelfUser(), getUserById(Config.INSTANCE.get("ownerId"))));
	}

	public static void finish() {
		jda.getPresence().setGame(Game.playing(devMode() ? "DEVELOPER MODE" : "try " + globalPrefix + "help!"));
		done = true;
		List<Class> passedClasses = new ArrayList();
		for (CommandContainer command : CommandManager.getCommands()) {
			Class clazz = command.toMethod().getDeclaringClass();
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
		print(LogType.INFO, "Shard", shardId, "is ready to be used!");
	}

	public static <T> boolean print(String threadName, LogType logType, T... message) {
		if (logType == LogType.DEBUG && !devMode()) return true;
		String[] args = new String[(message == null ? new Object[0] : message).length];
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
		return print(jda != null ? jda.getSelfUser().getName() : "Discord Bot", logType, message);
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
			if (jda != null) if (status == 2) {
				jda.shutdownNow();
				commandsExecutor.shutdownNow();
				miscellaneousExecutor.shutdownNow();
			} else {
				long timeout = calculateShutdownTimeout();
				shutdown = true;
				jda.shutdown();
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
							print(LogType.WARN, "Execution of a hook took longer than", timeout, "milliseconds, this is abnormal. Hook:", thread.getName());
							executionThread.interrupt();
						}
					}
					map.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.exit(status);
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
		return getGuildCount() * 10 < 15000 ? 15000 : getGuildCount() * 10;
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
		User user = null;
		for (int i = 0; i < input.getContentDisplay().split(" ").length; i++)
			try {
				if ((user = !input.getMentionedUsers().isEmpty() ? input.getMentionedUsers().get(0) : getUsernameAtArg(input.getContentDisplay(), input.getGuild(), i) != null && !input.getGuild().getMembersByName(getUsernameAtArg(input.getContentDisplay(), input.getGuild(), i), true).isEmpty() ? input.getGuild().getMembersByName(getUsernameAtArg(input.getContentDisplay(), input.getGuild(), i), true).get(0).getUser() : null) != null) return user;
			} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}

	public static Member getMemberFromInput(Message input) {
		return getUserFromInput(input) == null ? null : input.getGuild().getMember(getUserFromInput(input));
	}

	public static String getUsernameAtArg(String string, Guild guild, int arg) {
		return getUsernameFromArgs(removeArgs(string.split(" "), intArrayToIntegerArray(new int[arg])), guild);
	}

	private static String getUsernameFromArgs(String[] args, Guild guild) {
		String username = "";
		for (String arg : args) {
			username += arg + " ";
			if (!guild.getMembersByName(username.trim(), true).isEmpty()) return username.trim();
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
			if (!CommandManager.getSubcommands(cmd).isEmpty()) {
				cmdSubcommands = "**Subcommands**\n\t";
				List<String> subcommandNames = new ArrayList();
				for (CommandContainer scmdContainer : CommandManager.getSubcommands(cmd))
					subcommandNames.add(scmdContainer.toSubcommand().name());
				for (String scmdName : Main.sort(subcommandNames))
					for (CommandContainer scommand : CommandManager.getSubcommands(cmd)) {
						Subcommand scmd = scommand.toSubcommand();
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
			if (CommandManager.getSubcommands(cmd).size() != 0) {
				cmdSubcommands = "**Subcommands**\n\t";
				List<String> subcommandNames = new ArrayList();
				for (CommandContainer scmdContainer : CommandManager.getSubcommands(cmd))
					subcommandNames.add(scmdContainer.toSubcommand().name());
				for (String scmdName : Main.sort(subcommandNames))
					for (CommandContainer scmdMethod : CommandManager.getSubcommands(cmd)) {
						Subcommand scmd = scmdMethod.toSubcommand();
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
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd) {
		return sendCommandHelp(event.getChannel(), event, cmd, null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(CommandEvent event) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(channel, event, event.getCommand(), null)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event) {
		return sendCommandHelp(channel, event, event.getCommand(), null);
	}

	/**
	 * Equivalent to calling
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, cmd, null)}
	 */
	public static Message sendCommandHelp(CommandEvent event, Method cmd, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, cmd, extraMsg);
	}

	/**
	 * Equivalent to calling
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(event.getChannel(), event, event.getCommand(),
	 * extraMsg)}
	 */
	public static Message sendCommandHelp(CommandEvent event, String extraMsg) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand(), extraMsg);
	}

	/**
	 * Equivalent to calling
	 * {@link com.impulsebot.Main#sendCommandHelp(MessageChannel, CommandEvent, Method, String)
	 * Main#sendCommandHelp(channel, event, event.getCommand(), extraMsg)}
	 */
	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event, String extraMsg) {
		return sendCommandHelp(channel, event, event.getCommand(), extraMsg);
	}

	public static Method getParentCommand(Subcommand child) {
		try {
			// format should be : package.class.method
			String parent = child.parent();
			String methodName = parent.split("\\.")[parent.split("\\.").length - 1];
			// this should make com.impulsebot.commands.Economy from
			// com.impulsebot.commands.Economy.bank
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

	public static TextChannel getTextChannelByName(Guild guild, String name, boolean ignoreCase) {
		List<TextChannel> channels = guild.getTextChannelsByName(name, ignoreCase);
		return channels.size() == 0 ? null : channels.get(0);
	}

	public static VoiceChannel getVoiceChannelByName(Guild guild, String name, boolean ignoreCase) {
		List<VoiceChannel> channels = guild.getVoiceChannelsByName(name, ignoreCase);
		return channels.size() == 0 ? null : channels.get(0);
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
				if (!event.getMessage().getContentDisplay().startsWith(prefix)) return;
				String[] parts = null;
				String rawContent = event.getMessage().getContentRaw();
				if (rawContent.toLowerCase().startsWith(prefix.toLowerCase())) parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+", 2), 2);
				if (parts != null) if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
					String name = parts[0];
					String args = parts[1] == null ? "" : parts[1];
					int i = -1;
					for (int x = 0; x < CommandManager.getCommands().size(); x++)
						if (CommandManager.getCommands().get(x).toCommand().name().equalsIgnoreCase(name)) {
							i = x;
							break;
						}
					if (i != -1) {
						if (userCommandUsages.get(event.getAuthor().getId()).get("used") > 5) {
							event.getChannel().sendMessageFormat("You have used more than 5 commands in the last 10 seconds, please wait %s before running another command.", formatMillis(10000 - (System.currentTimeMillis() - userCommandUsages.get(event.getAuthor().getId()).get("lastUsedMillis")))).queue();;
							return;
						}
						Method command = CommandManager.getCommands().get(i).toMethod();
						while (args.split(" ").length != 0 && !CommandManager.getSubcommands(command).isEmpty()) {
							boolean found = false;
							for (CommandContainer subcommand : CommandManager.getSubcommands(command))
								if (subcommand.toSubcommand().name().equals(args.split(" ")[0])) {
									command = subcommand.toMethod();
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
			if (element.getFileName() != null && element.getClassName().startsWith("com.impulsebot.commands")) stElement = element;
		event.getChannel().sendMessageFormat("A `%s` exception was thrown at line %s in %s while parsing the command%s.%s", e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), e.getMessage() != null ? String.format(": `%s`", e.getMessage()) : "", Main.devMode() ? "" : String.format("\nMy owner, %s, has been informed.", Main.getOwner().getAsMention())).queue();
		if (!Main.devMode()) {
			String output = String.format("A `%s` exception was thrown at line %s in %s while parsing the message `%s`. Stacktrace:\n", e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), event.getMessage().getContentDisplay());
			String stacktrace = generateStackTrace(e);
			String currentPart = "```fix\n";
			List<String> parts = Lists.newArrayList(output);
			for (String part : stacktrace.split("\n"))
				if ((currentPart + part + "\n").length() <= 1997)
					currentPart += part + "\n";
				else {
					parts.add(currentPart + "```");
					currentPart = "```fix\n";
				}
			parts.add(currentPart + "```");
			for (String part : parts)
				sendPrivateMessage(getOwner(), part);
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

	public static boolean isHexInteger(String s) {
		try {
			Integer.parseInt(s, 16);
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
		if (devMode && jda != null && !jda.getPresence().getGame().equals(Game.playing("DEVELOPER MODE")) && done()) setGame("DEVELOPER MODE");
		return devMode;
	}

	public static void devMode(boolean bool) {
		devMode = bool;
		devMode();
	}

	public static UserImpl getOwner() {
		return (UserImpl) owner;
	}

	public static void setOwner(User owner) {
		if (owner.getId().equals(Config.INSTANCE.get("ownerId"))) Main.owner = owner;
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

	public static List<String> getCommandNames() {
		List<String> names = new ArrayList<>();
		for (CommandContainer cmd : CommandManager.getCommands())
			names.add(cmd.toCommand().name());
		return names;
	}

	public static List<String> getSubcommandNames() {
		List<String> names = new ArrayList<>();
		for (CommandContainer cmd : CommandManager.getSubcommands())
			names.add(cmd.toSubcommand().name());
		return names;
	}

	public static CommandContainer getCommandByName(String name) {
		for (CommandContainer cmd : CommandManager.getCommands())
			if (cmd.toCommand().name().equals(name)) return cmd;
		return null;
	}

	public static void mute(Member member) {
		for (Channel channel : Main.getAllChannels(member.getGuild()))
			Main.getPermissionOverride(member, channel).getManager().deny(Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_SPEAK).queue();
	}

	public static void unmute(Member member) {
		for (Channel channel : Main.getAllChannels(member.getGuild()))
			if (channel.getPermissionOverride(member) != null) channel.getPermissionOverride(member).delete().queue();
	}

	@Nullable
	public static Message waitForInput(Member author, MessageChannel channel, int timeoutMillis) {
		return waitForInput(author.getUser(), channel, timeoutMillis);
	}

	@Nullable
	public static Message waitForInput(User author, MessageChannel channel, int timeoutMillis) {
		int startSize = messages.get().size();
		int previousSize = startSize;
		long currentMillis = System.currentTimeMillis();
		while (true) {
			if (!messages.get().isEmpty() && messages.get().size() != previousSize) {
				for (int i : range(messages.get().size() - previousSize)) {
					Message lastMsg = messages.get().get(previousSize + i);
					if (messages.get().size() > startSize && lastMsg.getAuthor().getIdLong() == author.getIdLong() && lastMsg.getChannel().getIdLong() == channel.getIdLong())
						return lastMsg;
					else if (System.currentTimeMillis() - currentMillis >= timeoutMillis) return null;
				}
				previousSize = messages.get().size();
			}
			sleep(250);
		}
	}

	@Nullable
	public static Message waitForInput(MessageChannel channel, int timeoutMillis) {
		int startSize = messages.get().size();
		int previousSize = startSize;
		long currentMillis = System.currentTimeMillis();
		while (true) {
			if (!messages.get().isEmpty() && messages.get().size() != previousSize) {
				for (int i : range(messages.get().size() - previousSize)) {
					Message lastMsg = messages.get().get(previousSize + i);
					if (messages.get().size() > startSize && lastMsg.getChannel().getIdLong() == channel.getIdLong())
						return lastMsg;
					else if (System.currentTimeMillis() - currentMillis >= timeoutMillis) return null;
				}
				previousSize = messages.get().size();
			}
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
	 * {@link com.impulsebot.Main#formatMillis(long, boolean, boolean, boolean, boolean, boolean, boolean)
	 * formatMillis(millis, true, true, true, true, false, false)}.
	 */
	public static String formatMillis(long millis) {
		return formatMillis(millis, true, true, true, true, false, false);
	}

	public static int getTotalGuildCount() {
		return totalGuildCount;
	}

	@SideOnly(Side.CLIENT)
	public static int getGuildCount() {
		return getGuilds().size();
	}

	@SideOnly(Side.CLIENT)
	public static List<Guild> getGuilds() {
		return new ArrayList(jda.getGuilds());
	}

	@SideOnly(Side.CLIENT)
	public static List<String> getGuildNames() {
		List<String> guilds = new ArrayList<>();
		for (Guild guild : getGuilds())
			guilds.add(guild.getName());
		return guilds;
	}

	public static int getTotalUserCount() {
		return totalUserCount;
	}

	@SideOnly(Side.CLIENT)
	public static int getUserCount() {
		return getUsers().size();
	}

	@SideOnly(Side.CLIENT)
	public static List<User> getUsers() {
		return new ArrayList(jda.getUsers());
	}

	@SideOnly(Side.CLIENT)
	public static List<String> getUserNames() {
		List<String> users = new ArrayList<>();
		for (User user : getUsers())
			users.add(user.getName());
		return users;
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
		for (User user1 : getUsers())
			if (user1.getName().equals(name)) {
				user = user1;
				break;
			}
		return user;
	}

	public static User getUserById(String id) {
		User user = null;
		for (User user1 : getUsers())
			if (user1.getId().equals(id)) {
				user = user1;
				break;
			}
		if (user == null && done) {
			ClientDataRequestPacket CDR = new ClientDataRequestPacket(User.class, id);
			ClientHandler.INSTANCE.sendPacket(CDR);
			while (!CDR.received)
				sleep(25);
			user = (User) CDR.data;
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
		return getHTML(url, new HashMap());
	}

	public static String getHTML(String url, Map<String, String> requestProperties) throws IOException {
		StringBuilder result = new StringBuilder();
		URL URL = new URL(url);
		URLConnection connection = URL.openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		if (requestProperties != null) for (Entry<String, String> property : requestProperties.entrySet())
			connection.setRequestProperty(property.getKey(), property.getValue());
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

	public static <K, V> HashMap<K, V> newHashMap(K[] keys, V[] values) {
		HashMap<K, V> map = new HashMap<>();
		for (int x = 0; x < keys.length && x < values.length; x++)
			map.put(keys[x], values[x]);
		return map;
	}

	public static <T extends Number> T toNumber(Class<T> clazz, Object obj) {
		Number returnValue = null;
		if (obj instanceof Number) switch (clazz.getSimpleName()) {
		case "Short":
			returnValue = ((Number) obj).shortValue();
			break;
		case "Integer":
			returnValue = ((Number) obj).intValue();
			break;
		case "Long":
			returnValue = ((Number) obj).longValue();
			break;
		case "Float":
			returnValue = ((Number) obj).floatValue();
			break;
		default:
			returnValue = 0;
			break;
		}
		return (T) returnValue;
	}

	public static Integer getIntFromPossibleDouble(Object d) {
		return toNumber(Integer.class, d);
	}

	public static Long getLongFromPossibleDouble(Object d) {
		return toNumber(Long.class, d);
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

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean reverse) {
		return map.entrySet().stream().sorted((e1, e2) -> {
			return reverse ? -e1.getValue().compareTo(e2.getValue()) : e1.getValue().compareTo(e2.getValue());
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static <T extends Comparable<? super T>> List<T> sort(List<T> list) {
		Collections.sort(list);
		return list;
	}

	public static <T> List<T> setToList(Set<T> set, Class<T[]> clazz) {
		return Arrays.asList(Arrays.copyOf(set.toArray(), set.size(), clazz));
	}

	public static String multiplyString(String string, int times) {
		if (times <= 0) return "";
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
					if (field1.getName().equals(field) && (type == null || field1.getType() == type)) return field1;
				return null;
			}
		}.getField();
	}

	public static void setOnlineStatus(OnlineStatus status) {
		jda.getPresence().setStatus(status);
	}

	public static void setGame(Game game) {
		jda.getPresence().setGame(game);
	}

	public static void setGame(String game) {
		setGame(Game.playing(game));
	}

	public static OnlineStatus getStatusFromString(String string) {
		switch (string.toUpperCase()) {
		case "OFFLINE": {
			return OnlineStatus.OFFLINE;
		}
		case "INVISIBLE": {
			return OnlineStatus.INVISIBLE;
		}
		case "DND":
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
		jda.getSelfUser().getManager().setAvatar(avatar).queue();
	}

	public static String getPrefix(Guild guild) {
		return guild == null ? globalPrefix : serverPrefixes.getOrDefault(guild.getId(), globalPrefix);
	}

	public static SelfUser getSelfUser() {
		return jda == null ? self : jda.getSelfUser();
	}

	public static void setSelfUser(SelfUser self) {
		if (jda == null) Main.self = self;
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

	/**
	 * Adds a hook that should be ran everytime a command is ran.
	 * If this throws a
	 * {@link com.impulsebot.utils.commands.CommandPermissionException
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

	public static <T> List<T> add(List<T> list, T object, int index) {
		list.add(index, object);
		return list;
	}

	public static <T> List<T> addAll(List<T> list, T... objects) {
		list.addAll(Lists.newArrayList(objects));
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
		return new RoleImpl(role.getIdLong(), role.getGuild()).setColor(role.getColorRaw()).setHoisted(role.isHoisted()).setManaged(role.isManaged()).setMentionable(role.isMentionable()).setName(role.getName()).setRawPermissions(role.getPermissionsRaw()).setRawPosition(role.getPositionRaw());
	}

	public static TextChannel cloneChannel(TextChannel channel) {
		TextChannelImpl channel1 = new TextChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild()).setName(channel.getName()).setNSFW(channel.isNSFW()).setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong()).setPosition(channel.getPositionRaw()).setTopic(channel.getTopic());
		try {
			channel1.setLastMessageId(channel.getLatestMessageIdLong());
		} catch (IllegalStateException e) {
		}
		return channel1;
	}

	public static VoiceChannel cloneChannel(VoiceChannel channel) {
		return new VoiceChannelImpl(channel.getIdLong(), (GuildImpl) channel.getGuild()).setBitrate(channel.getBitrate()).setName(channel.getName()).setParent(channel.getParent() == null ? -1 : channel.getParent().getIdLong()).setPosition(channel.getPositionRaw()).setUserLimit(channel.getUserLimit());
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
		if (done) {
			ClientDataRequestPacket CDR = new ClientDataRequestPacket(TextChannel.class, id);
			ClientHandler.INSTANCE.sendPacket(CDR);
			while (!CDR.received)
				sleep(25);
			return (TextChannel) CDR.data;
		}
		return null;
	}

	public static VoiceChannel getVoiceChannelById(String id) {
		for (VoiceChannel channel : getVoiceChannels())
			if (channel.getId().equals(id)) return channel;
		if (done) {
			ClientDataRequestPacket CDR = new ClientDataRequestPacket(VoiceChannel.class, id);
			ClientHandler.INSTANCE.sendPacket(CDR);
			while (!CDR.received)
				sleep(25);
			return (VoiceChannel) CDR.data;
		}
		return null;
	}

	public static Guild getGuildById(String id) {
		for (Guild guild : getGuilds())
			if (guild.getId().equals(id)) return guild;
		if (done) {
			ClientDataRequestPacket CDR = new ClientDataRequestPacket(Guild.class, id);
			ClientHandler.INSTANCE.sendPacket(CDR);
			while (!CDR.received)
				sleep(25);
			return (Guild) CDR.data;
		}
		return null;
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
			ignored.printStackTrace();
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
		client.setAjaxController(new AjaxController() {
			private static final long serialVersionUID = 7739773024960187330L;

			@Override
			public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
				return true;
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
		for (CommandContainer command : CommandManager.getCommands()) {
			Command cann = command.toCommand();
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
		object.put("count", CommandManager.getCommands().size());
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
	public static Object compileAndRunJavaCode(String code, Map<String, Object> variables) throws Throwable {
		if (devMode) System.setProperty("java.home", "E:\\Program Files\\Java\\jdk1.8.0_162");
		Map<String, Object> variables0 = variables == null ? new HashMap() : variables;
		String fileName = "TempClass_" + Random.INSTANCE.randInt();
		String code0 = "public class " + fileName + " {public static Object returnValue = null; public static final void run(Class<" + fileName + "> thisClass, ";
		for (String var : variables0.keySet())
			code0 += variables0.get(var).getClass().getName() + " " + var + ", ";
		code0 = code0.substring(0, code0.length() - 2);
		code0 += ") {\n" + (code.contains("return ") ? code.substring(0, code.lastIndexOf("return ")) + "returnValue = " + code.substring(code.lastIndexOf("return ") + 7, code.length()) : code) + ";\n}}";
		List<Class> varClasses = Lists.newArrayList(Class.class);
		for (Object var : variables0.values())
			varClasses.add(var.getClass());
		List<Class> importClasses = new ArrayList();
		importClasses.addAll(varClasses);
		importClasses.addAll(new Reflections("com.impulsebot", new SubTypesScanner(false)).getSubTypesOf(Object.class));
		importClasses.addAll(new Reflections("net.dv8tion.jda", new SubTypesScanner(false)).getSubTypesOf(Object.class));
		List<Class> importedClasses = new ArrayList();
		List<String> blacklist = devMode ? Lists.newArrayList("net.dv8tion.jda.core.utils.SimpleLog") : new ArrayList();
		addAll(blacklist, "com.impulsebot.MainGUI", "net.dv8tion.jda.core.requests.restaction.PermOverrideData"); // deprecated class, private class
		for (Class clazz : importClasses) {
			boolean shouldBreak = blacklist.contains(clazz.getName()) || clazz.getName().contains("$") || Modifier.isPrivate(clazz.getModifiers()) || clazz.getSimpleName().equalsIgnoreCase("package-info");
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
		Method method = null;
		try {
			(method = MemoryJavaCompiler.compileStaticMethod("run", fileName, code0)).invoke(null, add(new ArrayList(variables0.values()), method.getDeclaringClass(), 0).toArray(new Object[0]));
		} catch (InvocationTargetException e) {
			throw new CompilationException(e.getCause().toString());
		}
		return getField(method.getDeclaringClass(), "returnValue", Object.class).get(null);

	}

	public static void addJarToClassPath(File jar) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new File(jar.getAbsolutePath()).toURI().toURL());
	}

	@SuppressWarnings("all") // revision is not 0 in every version and since it's final it'll give a warning.
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
		Scanner s = new Scanner(is);
		Scanner s0 = s.useDelimiter("\\A");
		String s1 = s0.hasNext() ? s0.next() : "";
		s.close();
		s0.close();
		return s1;
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
		for (File file : dir.listFiles()) {
			if (dir.isDirectory()) deleteAllFilesInDir(file);
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
	public static <T> T getInstanceWithoutConstructor(Class<T> clazz) throws InstantiationException {
		return (T) theUnsafe.allocateInstance(clazz);
	}

	/**
	 * Using this class can really mess things up, you can, for instance, use it to
	 * create arrays bigger than the RAM of the host pc.
	 *
	 * @return
	 */
	@Deprecated
	public static Unsafe getUnsafe() {
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

	public static MessageEmbed getInfoEmbed() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(jda.getSelfUser().getName());
		embed.setColor(new Color(Random.INSTANCE.randInt(256 * 256 * 256)));
		embed.setThumbnail("https://cdn.impulsebot.com/3mR7g3RC0O.png");
		embed.setDescription("This bot is an instance of Impulse, a Discord Bot written in Java by PlanetTeamSpeak using JDA. If you want your own bot with all these commands, make sure to check out [the GitHub page](https://github.com/PlanetTeamSpeakk/Impulse \"Yes, it's open source.\") and don't forget to join [the Discord Server](https://discord.gg/tzsmCyk \"Yes, I like advertising.\"), check out [the website](https://impulsebot.com \"Pls, just do it. ;-;\"), and send me all your cash on [my Patreon page](https://patreon.com/PlanetTeamSpeak \"Pls just give me your money.\").");
		embed.setFooter("PS, the color used is #" + Main.colourToHex(embed.build().getColor()) + ".", null);
		return embed.build();
	}

	public static ArraySet<Thread> getThreads() {
		return new ArraySet(Thread.getAllStackTraces().keySet());
	}

	public static List<File> getFilesInDir(File dir) {
		if (!dir.isDirectory()) return new ArraySet();
		List<File> files = new ArraySet();
		for (File file : dir.listFiles())
			if (file.isDirectory())
				files.addAll(getFilesInDir(file));
			else files.add(file);
		return Collections.unmodifiableList(files);
	}

	public static Integer[] intArrayToIntegerArray(int... array) {
		IntStream stream = Arrays.stream(array);
		Stream stream0 = stream.boxed();
		Integer[] modernArray = (Integer[]) stream0.<Integer>toArray(Integer[]::new);
		stream.close();
		stream0.close();
		return modernArray;
	}

	public static Long[] longArrayToLongArray(long... array) {
		LongStream stream = Arrays.stream(array);
		Stream stream0 = stream.boxed();
		Long[] modernArray = (Long[]) stream0.<Long>toArray(Long[]::new);
		stream.close();
		stream0.close();
		return modernArray;
	}

	public static Double[] doubleArrayToDoubleArray(double... array) {
		DoubleStream stream = Arrays.stream(array);
		Stream stream0 = stream.boxed();
		Double[] modernArray = (Double[]) stream0.<Double>toArray(Double[]::new);
		stream.close();
		stream0.close();
		return modernArray;
	}

	public static boolean isWholeNumber(double d) {
		return d - (long) d == 0;
	}

	public static List<Class> getClassesInPackage(String packageS) {
		return new ArraySet(new Reflections(packageS, new SubTypesScanner(false)).getSubTypesOf(Object.class));
	}

	public static String getMD5(File file) throws IOException {
		return getMD5(Files.newInputStream(file.toPath()));
	}

	public static String getMD5(InputStream stream) {
		String MD5 = null;
		try {
			MD5 = DigestUtils.md5Hex(stream);
		} catch (IOException e2) {
		}
		return MD5;
	}

	public static Connection getRDBConnection() {
		return rdbConnection;
	}

	public static String getJarVersion(File jar) throws IOException {
		try {
			ZipFile zipFile = new ZipFile(jar);
			List<ZipEntry> entries = new ArrayList();
			Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
			while (entriesEnum.hasMoreElements())
				entries.add(entriesEnum.nextElement());
			for (ZipEntry entry : entries)
				if (entry.getName().endsWith("MANIFEST.MF")) {
					for (String line : convertStreamToString(zipFile.getInputStream(entry)).split(System.lineSeparator()))
						if (line.startsWith("Implementation-Version: ")) {
							zipFile.close();
							return line.split(" ", 2)[1];
						}
					break;
				}
			zipFile.close();
		} catch (Exception e) {
		}
		return "";
	}

	public static String getLatestVersion() {
		try {
			return new Gson().fromJson(getHTML("https://api.github.com/repos/PlanetTeamSpeakk/Impulse/releases/latest"), Map.class).get("tag_name").toString();
		} catch (JsonSyntaxException | IOException e) {
			Main.throwCheckedExceptionWithoutDeclaration(e);
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	public static void setIdentifier(ServerClientIdentifierPacket identifierPacket) {
		id = identifierPacket.getIdentifier();
	}

	@SideOnly(Side.CLIENT)
	public static String getIdentifier() {
		return id;
	}

	@SideOnly(Side.SERVER)
	public static boolean identifyClient(ClientIdentificationPacket identificationPacket) {
		if (new ArrayList(identifications.values()).contains(identificationPacket.getShardId()) || identificationPacket.getShardId() >= shardCount)
			return false;
		else {
			identifications.put(identificationPacket.getIdentifier(), identificationPacket.getShardId());
			return true;
		}
	}

	@SideOnly(Side.CLIENT)
	public static int getShardId() {
		return shardId;
	}

	@SideOnly(Side.CLIENT)
	public static ChannelPipeline getPipeline() {
		return pipeline;
	}

	@SideOnly(Side.SERVER)
	public static Map<String, ChannelPipeline> getPipelines() {
		return new HashMap(pipelines);
	}

	@SideOnly(Side.SERVER)
	public static Map<String, Integer> getIdentifications() {
		return new HashMap(identifications);
	}

	/**
	 * This method can only serialize objects that implement Serializable since it
	 * uses Java's default serializer.
	 *
	 * @param obj
	 *            The object implementing Serializable to serialize.
	 * @return The given object represented in bytes.
	 * @see #deserializeByteArray(byte[])
	 * @see #deserializeByteArrayWithKryo(byte[], Class)
	 * @see #serializeObjectWithKryo(Object)
	 */
	public static byte[] serializeObject(Serializable obj) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(bos));
			objectOutputStream.writeObject(obj);
			objectOutputStream.close();
			return bos.toByteArray();
		} catch (Throwable e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	/**
	 * This method can only deserialize bytes gotten from Java's default serializer.
	 *
	 * @param bytes
	 *            The byte array representing an object which implements
	 *            Serializable.
	 * @return The object represented in the given bytes.
	 * @see #serializeObject(Serializable)
	 * @see #deserializeByteArrayWithKryo(byte[], Class)
	 * @see #serializeObjectWithKryo(Object)
	 */
	public static Object deserializeByteArray(byte[] bytes) {
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bytes)));
			Object object = objectInputStream.readObject();
			objectInputStream.close();
			return object;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This method can serialize any object since it uses Kryo as a serializer.
	 *
	 * @param obj
	 *            The object to serialize.
	 * @return The given object represented in bytes.
	 * @see #deserializeByteArrayWithKryo(byte[], Class)
	 * @see #serializeObject(Serializable)
	 * @see #deserializeByteArray(byte[])
	 */
	public static byte[] serializeObjectWithKryo(Object object) {
		Output output = new Output(1024, -1);
		kryo.writeClassAndObject(output, object);
		return output.toBytes();
	}

	/**
	 * This method can only deserialize bytes gotten from
	 * {@link #serializeObjectWithKryo(Object)} since it uses Kryo to do the
	 * serialization.
	 * <br>
	 * <br>
	 * Little downside of Kryo is that each class needs a zero-arg constructor, but
	 * I fixed this by editing 1 method in the entire library which is responsible
	 * for creating the instances of the objects to prefer using Unsafe to create
	 * instances, which means it doesn't need a zero-arg constructor, instead of how
	 * it used to be done.
	 *
	 * @param bytes
	 *            The bytes gotten from {@link #serializeObjectWithKryo(Object)}.
	 * @return The object represented by the given bytes.
	 * @see #deserializeByteArray(byte[])
	 * @see #serializeObjectWithKryo(Object)
	 * @see #serializeObject(Serializable)
	 */
	public static <T> T deserializeByteArrayWithKryo(byte[] bytes) {
		return (T) kryo.readClassAndObject(new Input(new ByteArrayInputStream(bytes)));
	}

	public static int getShardCount() {
		return shardCount;
	}

	public static void setShardCount(int shardCount) {
		Main.shardCount = shardCount;
	}

	public static JDA getJDA() {
		return jda;
	}

	public static XStream getXStream() {
		return xstream;
	}

	public static Kryo getKryo() {
		return kryo;
	}

	public static void addGuilds(int guilds) {
		totalGuildCount += guilds;
	}

	public static void addUsers(int users) {
		totalUserCount += users;
	}

	public static void toggleModifier(Field field, int modifier) {
		try {
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			int modifiersInt = (int) modifiers.get(field);
			modifiers.set(field, (modifiersInt & modifier) != 0 ? modifiersInt & ~modifier : modifiersInt & modifier);
		} catch (Exception e) {
			print(LogType.DEBUG, e);
			throwCheckedExceptionWithoutDeclaration(e);
		}
	}

	public static void setFinalField(Object object, String fieldName, Class fieldClass, Object newValue) {
		try {
			Field field = getField(object.getClass(), fieldName, fieldClass);
			if (field == null) return;
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.set(field, (int) modifiers.get(field) & ~Modifier.FINAL);
			field.setAccessible(true);
			field.set(object, newValue);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	// This is like the only well-documented method in this entire project.
	/**
	 * When objects are sent over the Netty connection and are serialized into
	 * XML, the serialization purposely does not serialize the field api since
	 * this would make packets way bigger than they should be. This has its pros but
	 * also its cons, a pro: it makes packets smaller, a con: it could break certain
	 * features which requires this method in order to fix them.
	 * <br>
	 * <br>
	 * This method basically sets the field 'api' which <i>almost</i>* every entity
	 * class contains to this client's jda, this does mean it is not the same, but
	 * it will often fix all features that require the api field.
	 * <br>
	 * <br>
	 * <sub>* Not every entity class has it, but every entity class certainly has
	 * features that use a feature of another class which does have it.</sub>
	 *
	 * @param entity
	 *            The entity which has partially or fully broken, necessary
	 *            features.
	 */
	public static void fixJDAEntity(Object entity) {
		if (entity.getClass().getName().startsWith("net.dv8tion.jda.core.entities"))
			setFinalField(entity, "api", JDAImpl.class, jda);
		else throw new IllegalArgumentException("The class of the given entity, " + entity.getClass().getName() + ", is not a class of net.dv8tion.core.jda.entities.");
	}

	public static <T> T deepCopy(T object) {
		return kryo.copy(object);
	}

	public static <T> T shallowCopy(T object) {
		return kryo.copyShallow(object);
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
		SUCCESSFUL("SUCCESFUL", ConsoleColours.GREEN),
		INFO("INFO", ConsoleColours.UNKNOWN),
		DEBUG("DEBUG", ConsoleColours.HIGHINTENSITY_BLUE);

		public final String			name;
		public final ConsoleColours	colour;

		private LogType(String name, ConsoleColours colour) {
			this.name = name;
			this.colour = colour;
		}

	}

}
