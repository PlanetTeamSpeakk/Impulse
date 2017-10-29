package com.ptsmods.impulse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandExecutionHook;
import com.ptsmods.impulse.miscellaneous.EventHandler;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.ConsoleColors;
import com.ptsmods.impulse.utils.DataIO;
import com.ptsmods.impulse.utils.Downloader;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.ShardedRateLimiter;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;

public class Main {

	public static final Date started = new Date();
	public static final Map<String, String> apiKeys = new HashMap<>();
	private static final ThreadPoolExecutor commandsExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	private static final ThreadPoolExecutor miscellaneousExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	private static Map<String, Integer> commandIndex = new HashMap();
	private static final Set<Class<? extends Event>> events = new Reflections("net.dv8tion.jda.core.events").getSubTypesOf(Event.class);
	private static List<CommandExecutionHook> commandHooks = new ArrayList<>();
	private static List<JDA> shards = new ArrayList<>();
	private static boolean devMode = false;
	private static User owner = null;
	private static List<String> categories = new ArrayList<>();
	private static List<Method> commands = new ArrayList<>();
	private static List<Method> subcommands = new ArrayList<>();
	private static List<Message> messages = new ArrayList<>();
	private static List<List<Message>> allMessages = new ArrayList<>();

	public static void main(String[] args) {
		try {
			main0(args);
		} catch (Throwable e) {
			e.printStackTrace(); // so exceptions and errors are printed.
			System.exit(1);
		}
	}

	private static void main0(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			print(LogType.INFO, "Bot shutting down, deleting all temporary files.");
			int counter = 0;
			if (new File("data/tmp").exists() && new File("data/tmp").isDirectory()) {
				for (File file : new File("data/tmp").listFiles())
					if (!file.getName().equals("info.txt"))
						try {
							file.delete();
							counter += 1;
						} catch (Throwable e) {}
			} else new File("data/tmp/").mkdirs();
			print(LogType.INFO, String.format("Temporary files deleted, deleted %s file%s.", counter, counter == 1 ? "" : "s"));
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
		}));
		devMode = Arrays.asList(args).contains("-devMode");
		if (devMode) print(LogType.WARN, "Developer mode is turned on, this means that errors will NOT be sent privately to the owner of the bot and only the owner can use commands.");
		try {
			if (!new File("data/").isDirectory())
				new File("data/").mkdirs();
			if (Config.getValue("token") == null || Config.getValue("prefix") == null || Config.getValue("ownerId") == null || Config.getValue("shards") == null) {
				Config.addComment("Config file created by Impulse Discord Bot written by PlanetTeamSpeak.");
				Config.addComment("The bot token used to log in, example: MzIzMTc2ODk2NTAwMzM0NTk0.DLfRfw.zZ7V1ljky12M5sKEVUZBAzwYUbo.");
				Config.addValuePair("token", "");
				Config.addComment("The prefix used to execute commands, this CANNOT be 2 or more slashes!");
				Config.addValuePair("prefix", "\\");
				Config.addComment("The ID of the owner, this should be your id as anyone who has this can do anything with your bot in any server.");
				Config.addValuePair("ownerId", "");
				Config.addComment("The amount of shards you want, for every shard the startup time takes 5 more seconds.");
				Config.addComment("If you don't know what shards are, maybe you should learn some stuff about bots before making your own.");
				Config.addValuePair("shards", "1");
				Config.addComment("The key used to send bot stats to https://carbinotex.net");
				Config.addValuePair("carbonitexKey", "");
				Config.addComment("The key used to send bot stats to https://discordbots.org");
				Config.addValuePair("discordBotListKey", "");
				Config.addComment("The key used to send bot stats to https://bots.discord.pw");
				Config.addValuePair("discordBotsKey", "");
				print(LogType.WARN, "The config hasn't been changed yet, please open config.cfg and change the variables.");
				System.exit(0);
			} else if (Config.getValue("token").isEmpty() || Config.getValue("ownerId").isEmpty() || Config.getValue("prefix").isEmpty() || Config.getValue("shards").isEmpty()) {
				print(LogType.WARN, "The config hasn't been changed yet, please open config.cfg and change the variables.");
				System.exit(0);
			}
			apiKeys.put("w3hills", "5D58B696-7AF3-4DD0-1251-B5D24E16668C");
			apiKeys.put("geocoding", "AIzaSyCXkFcW0v8XJWGK2Im2_fApsbh3I8OGCDI"); // they're all free, anyway.
			apiKeys.put("timezone", "AIzaSyCXkFcW0v8XJWGK2Im2_fApsbh3I8OGCDI");
			int shardAmount = 1;
			if (!isInteger(Config.getValue("shards")))
				print(LogType.WARN, "The value of key 'shards' isn't an integer, expecting 1.");
			else shardAmount = Integer.parseInt(Config.getValue("shards"));
			if (shardAmount < 1) shardAmount = 1;
			print(LogType.INFO, "Loading commands...");
			for (Class<? extends Object> clazz : new Reflections("com.ptsmods.impulse.commands", new SubTypesScanner(false)).getSubTypesOf(Object.class))
				for (Method method : clazz.getMethods())
					if (method.isAnnotationPresent(Command.class)) {
						commands.add(method);
						getCategory(method.getAnnotation(Command.class).category());
						commandIndex.put(method.getAnnotation(Command.class).name(), commands.size()-1);
					} else if (method.isAnnotationPresent(Subcommand.class))
						subcommands.add(method);
			print(LogType.INFO, commands.size() + " commands and " + subcommands.size() + " subcommands loaded, logging in...");
			ShardedRateLimiter rateLimiter = new ShardedRateLimiter();
			for (int i : range(shardAmount)) {
				JDA shard = new JDABuilder(AccountType.BOT)
						.setToken(Config.getValue("token"))
						.addEventListener(new EventHandler())
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
			owner = shards.get(0).getUserById(Config.getValue("ownerId"));
		} catch (LoginException e) {
			print(LogType.ERROR, "The bot could not log in with the given token, please change the token in config.cfg.");
			System.exit(0);
		} catch (RateLimitedException e) {
			print(LogType.ERROR, "The bot has been rate limited, please try restarting.");
			System.exit(0);
		} catch (Throwable e) {
			print(LogType.ERROR, "An unknown error occured, please contact PlanetTeamSpeak#4157.");
			e.printStackTrace();
			System.exit(1);
		}
		print(LogType.INFO, String.format("Succesfully logged in as %s#%s, took %s milliseconds. Owner = %s#%s, prefix = %s.",
				shards.get(0).getSelfUser().getName(),
				shards.get(0).getSelfUser().getDiscriminator(),
				System.currentTimeMillis() - started.getTime(),
				owner.getName(),
				owner.getDiscriminator(),
				Config.getValue("prefix")));
		for (JDA shard : shards)
			shard.getPresence().setGame(Game.of("try " + Config.getValue("prefix") + "help!"));
	}

	public static <T> void print(String threadName, LogType logType, T... message) {
		String[] args = new String[message.length];
		for (int x = 0; x < message.length; x++)
			if (message[x] == null) args[x] = "null";
			else args[x] = message[x].toString();
		if (logType == LogType.INFO) ConsoleColors.print("[" + getFormattedTime() + "] [" + threadName + "/INFO]: " + join(args) + "\n");
		else if (logType == LogType.WARN) ConsoleColors.print(ConsoleColors.YELLOW + "[" + getFormattedTime() + "] [" + threadName + "/WARN]: " + join(args) + "\u001B[0m\n");
		else if (logType == LogType.ERROR) ConsoleColors.print(ConsoleColors.RED + "[" + getFormattedTime() + "] [" + threadName + "/ERROR]: " + join(args) + "\n");
	}

	public static String getFormattedTime() {
		return joinCustomChar(":", "" + (LocalDateTime.now().getHour() < 10 ? "0" : "") + LocalDateTime.now().getHour(),
				"" + (LocalDateTime.now().getMinute() < 10 ? "0" : "") + LocalDateTime.now().getMinute(),
				"" + (LocalDateTime.now().getSecond() < 10 ? "0" : "") + LocalDateTime.now().getSecond());
	}

	public static <T> void print(LogType logType, T... message) {
		print(shards.size() > 0 ? shards.get(0).getSelfUser().getName() : "Discord Bot", logType, message);
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (JDA shard : shards)
					if (status == 0) shard.shutdown();
					else shard.shutdownNow();
			}
		}).start();
		commandsExecutor.shutdown();
		System.exit(status);
	}

	public static Integer[] range(int range) {
		Integer[] array = new Integer[range];
		for (int x = 0; x < array.length; x++)
			array[x] = x;
		return array;
	}

	public static Double[] range(Double range) {
		Double[] array = new Double[range.intValue()];
		for (Double x = 0D; x < array.length; x++)
			array[x.intValue()] = x;
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
			return !input.getMentionedUsers().isEmpty() ? input.getMentionedUsers().get(0) : input.getContent().startsWith(Config.getValue("prefix")) ? input.getGuild().getMembersByName(getUsernameFirstArg(input), true).get(0).getUser() : null;
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

	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event, Method cmd) {
		if (cmd.isAnnotationPresent(Command.class)) {
			Command command = cmd.getAnnotation(Command.class);
			String cmdName = command.name() + (command.arguments() == null || command.arguments().isEmpty() ? "" : " " + command.arguments());
			String cmdHelp = command.help() == null ? "" : command.help().replaceAll("\\[p\\]", getPrefix(event.getGuild()));
			String cmdSubcommands = "";
			if (getSubcommands(cmd).size() != 0) {
				cmdSubcommands = "**Subcommands**\n\t";
				for (Method scommand : getSubcommands(cmd)) {
					Subcommand scmd = scommand.getAnnotation(Subcommand.class);
					if (!event.isOwner() && !event.isCoOwner())
						if (scmd.hidden() ||
								scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) ||
								event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions()))
							continue;
					cmdSubcommands += String.format("**%s**", scmd.name()) + (scmd.help() == null || scmd.help().isEmpty() ? "" : ": " + scmd.help().split("\n")[0]) + "\n\t";
				}
				cmdSubcommands = cmdSubcommands.trim();
			}
			return channel.sendMessage(String.format("**%s**%s%s",
					Config.getValue("prefix") + cmdName,
					cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp,
							cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands)).complete();
		} else if (cmd.isAnnotationPresent(Subcommand.class)) {
			Subcommand command = cmd.getAnnotation(Subcommand.class);
			String cmdName = command.name() + (command.arguments() == null || command.arguments().isEmpty() ? "" : " " + command.arguments());
			String cmdHelp = command.help() == null ? "" : command.help().replaceAll("\\[p\\]", getPrefix(event.getGuild()));
			String cmdSubcommands = "";
			if (getSubcommands(cmd).size() != 0) {
				cmdSubcommands = "**Subcommands**\n\t";
				for (Method scmdMethod : getSubcommands(cmd)) {
					Subcommand scmd = scmdMethod.getAnnotation(Subcommand.class);
					if (!event.isOwner() && !event.isCoOwner())
						if (scmd.hidden() ||
								scmd.ownerCommand() && !event.getAuthor().getId().equals(Main.getOwner().getId()) ||
								event.getMember() != null && !event.getMember().hasPermission(scmd.userPermissions()))
							continue;
					cmdSubcommands += String.format("**%s**", scmd.name()) + (scmd.help() == null || scmd.help().isEmpty() ? "" : ": " + scmd.help().split("\n")[0]) + "\n\t";
				}
				cmdSubcommands = cmdSubcommands.trim();
			}
			try {
				while (getParentCommand(command) != null)
					if (getParentCommand(command).isAnnotationPresent(Command.class)) {
						cmdName = getParentCommand(command).getAnnotation(Command.class).name() + " " + cmdName;
						break;
					} else {
						command = getParentCommand(command).getAnnotation(Subcommand.class);
						cmdName = command.name() + " " + cmdName;
					}
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				return null;
			}
			return channel.sendMessage(String.format("**%s**%s%s",
					Config.getValue("prefix") + cmdName,
					cmdHelp.isEmpty() ? "" : ":\n\n" + cmdHelp,
							cmdSubcommands.isEmpty() ? "" : "\n\n" + cmdSubcommands)).complete();
		}
		return null;
	}

	public static Message sendCommandHelp(CommandEvent event, Method cmd) {
		return sendCommandHelp(event.getChannel(), event, cmd);
	}

	public static Message sendCommandHelp(CommandEvent event) {
		return sendCommandHelp(event.getChannel(), event, event.getCommand());
	}

	public static Message sendCommandHelp(MessageChannel channel, CommandEvent event) {
		return sendCommandHelp(channel, event, event.getCommand());
	}

	@Nullable
	public static Method getParentCommand(Subcommand child) throws ClassNotFoundException, NoSuchMethodException {
		// format should be : package.class.method
		String parent = child.parent();
		String methodName = parent.split("\\.")[parent.split("\\.").length-1];
		// this should make com.ptsmods.impulse.commands.Economy from com.ptsmods.impulse.commands.Economy.bank
		Class clazz = Class.forName(joinCustomChar(".", removeArg(parent.split("\\."), parent.split("\\.").length-1)));
		return clazz.getMethod(methodName, CommandEvent.class);
	}

	public static Role getRoleByName(Guild guild, String name, boolean ignoreCase) {
		List<Role> roles = guild.getRolesByName(name, ignoreCase);
		return roles.size() == 0 ? null : roles.get(0);
	}

	public static void executeCommand(Runnable runnable) {
		commandsExecutor.execute(runnable);
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

	public static boolean devMode() {
		return devMode;
	}

	public static void devMode(boolean bool) {
		devMode = bool;
	}

	public static User getOwner() {
		return owner;
	}

	public static void sendPrivateMessage(User user, String msg) {
		user.openPrivateChannel().complete().sendMessage(msg).queue();
	}

	public static String getCategory(String category) {
		if (!categories.contains(category)) categories.add(category);
		return category;
	}

	public static Object[] removeArg(Object[] args, int arg) {
		List<Object> data = new ArrayList<>();
		for (int x = 0; x < args.length; x++)
			if (x != arg) data.add(args[x]);
		return data.toArray(new Object[0]);
	}

	public static String[] removeArg(String[] args, int arg) {
		return castStringArray(removeArg((Object[]) args, arg));
	}

	public static Object[] removeArgs(Object[] args, Integer... arg) {
		for (int i : arg)
			args = removeArg(args, i);
		return args;
	}

	public static String[] removeArgs(String[] args, Integer... arg) {
		return castStringArray(removeArgs((Object[]) args, arg));
	}

	public static double factorial(Double d) {
		Double[] doubles = range(d);
		doubles = castDoubleArray(removeArg(doubles, 0));
		ArrayUtils.reverse(doubles);
		for (double x : doubles)
			d *= x;
		return d;
	}

	/**
	 * Evaluates a math equation in a String.
	 * It does addition, subtraction, multiplication, division, exponentiation (using the ^ symbol), factorial (! <b>before</b> a number), and a few basic functions like sqrt, cbrt, sin, cos and tan. It supports grouping using (...), and it gets the operator precedence and associativity rules correct.
	 * @param str
	 * @return The answer to the equation.
	 * @author Boann (https://stackoverflow.com/a/26227947)
	 */
	public static double eval(final String str) {
		return new Object() {
			int pos = -1, ch;

			void nextChar() {
				ch = ++pos < str.length() ? str.charAt(pos) : -1;
			}

			boolean eat(int charToEat) {
				while (ch == ' ') nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length()) throw new RuntimeException("Unexpected character: " + (char)ch);
				return x;
			}

			double parseExpression() {
				double x = parseTerm();
				for (;;)
					if      (eat('+')) x += parseTerm(); // addition
					else if (eat('-')) x -= parseTerm(); // subtraction
					else return x;
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;)
					if      (eat('*')) x *= parseFactor(); // multiplication
					else if (eat('/')) x /= parseFactor(); // division
					else return x;
			}

			double parseFactor() {
				if (eat('+')) return parseFactor(); // unary plus
				if (eat('-')) return -parseFactor(); // unary minus

				double x;
				int startPos = pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if (ch >= '0' && ch <= '9' || ch == '.') { // numbers
					while (ch >= '0' && ch <= '9' || ch == '.') nextChar();
					x = Double.parseDouble(str.substring(startPos, pos));
				} else if (ch >= 'a' && ch <= 'z' || ch == '!') { // functions
					while (ch >= 'a' && ch <= 'z' || ch == '!') nextChar();
					String func = str.substring(startPos, pos);
					x = parseFactor();
					if (func.equals("sqrt"))      x = Math.sqrt(x);
					else if (func.equals("cbrt")) x = Math.cbrt(x);
					else if (func.equals("sin"))  x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos"))  x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan"))  x = Math.tan(Math.toRadians(x));
					else if (func.equals("pi"))   x = Math.PI * (x == 0D ? 1D : x);
					else if (func.equals("!"))    x = factorial(x);
					else throw new RuntimeException("Unknown function: " + func);
				} else
					if (ch != -1) throw new RuntimeException("Unexpected character: " + (char)ch);
					else x = 0D;

				if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
				return x;
			}
		}.parse();
	}

	public static String[] castStringArray(Object[] array) {
		return Arrays.copyOf(array, array.length, String[].class);
	}

	public static Integer[] castIntArray(Object[] array) {
		return Arrays.copyOf(array, array.length, Integer[].class);
	}

	public static Double[] castDoubleArray(Object[] array) {
		return Arrays.copyOf(array, array.length, Double[].class);
	}

	public static List<String> getCategories() {
		return categories;
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

	}

	@Nullable
	public static Message waitForInput(Member author, MessageChannel channel, int timeoutMillis, long messageSentTimestamp) {
		long currentMillis = System.currentTimeMillis();
		while (true) {
			if (messages.isEmpty()) continue;
			Message lastMsg = messages.get(messages.size()-1);
			if (lastMsg.getCreationTime().toEpochSecond() > messageSentTimestamp && lastMsg.getAuthor().getIdLong() == author.getUser().getIdLong() && lastMsg.getGuild().getIdLong() == author.getGuild().getIdLong() && lastMsg.getChannel().getIdLong() == channel.getIdLong()) return lastMsg;
			else if (System.currentTimeMillis()-currentMillis >= timeoutMillis) return null;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}
	}

	public static String formatMillis(long millis) {
		String[] output = new String[4];
		String outputString = "";
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		if (days != 0) output[0] = "**" + days + "** day" + (days != 1 ? "s" : "");
		if (hours != 0) output[1] = "**" + hours + "** hour" + (hours != 1 ? "s" : "");
		if (minutes != 0) output[2] = "**" + minutes + "** minute" + (minutes != 1 ? "s" : "");
		if (seconds != 0) output[3] = "**" + seconds + "** second" + (seconds != 1 ? "s" : "");
		while (Lists.newArrayList(output).contains(null))
			for (int x = 0; x < output.length; x++)
				if (output[x] == null) output = removeArg(output, x);
		outputString = joinNiceString(output);
		if (outputString.trim().endsWith(",")) outputString = outputString.trim().substring(0, outputString.trim().length()-1);
		return outputString;
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
		if (messages.size() == Integer.MAX_VALUE) {
			allMessages.add(messages);
			messages.clear();
		}
		return messages.add(message);
	}

	public static List<List<Message>> getReceivedMessages() {
		return allMessages;
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

	public static <T> String joinNiceString(T... array) {
		String output = "";
		for (int x = 0; x < array.length; x++)
			if (x+2 != array.length) output += array[x].toString() + ", ";
			else output += array[x].toString() + " and ";
		return output;
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
	public static void doesJavaHaveALimitOnHowLongTheNamesOfMethodsCanBeIDontThinkSoSoIllJustContinueThisUntillIGetTiredOfItAndWantToStopTheCapitalisationIsStartingToBecomeABitConfusingSoIThinkIllStopRightHere() {
		return;
	}

	public static Integer getIntFromPossibleDouble(Object d) {
		if (d instanceof Double) return ((Double) d).intValue();
		else if (d instanceof Integer) return (Integer) d;
		else
			try {
				return (int) d;
			} catch (Throwable e) {
				return 0;
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

	public static void restart() {
		File currentJar = null;
		try {
			currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		if(!currentJar.getName().endsWith(".jar"))
			return;

		try {
			Runtime.getRuntime().exec("java -cp " + currentJar.getPath() + " com.ptsmods.impulse.Main");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.exit(0);
	}

	/**
	 * Replace characters to their URL encoded counter-part.
	 * E.g. ' becomes %27
	 * @param url The url which contains characters that need encoding.
	 * @return A never-null String encoded for use with URLs (which is probably 3 times as long as the input text).
	 */
	public static String percentEncode(String text) {
		String[] parts = text.split("\n");
		String[] outputParts = new String[parts.length];
		for (int x = 0; x < parts.length; x++) {
			String output = "";
			for (Character ch : parts[x].toCharArray())
				output += "%" + Integer.toHexString(ch);
			outputParts[x] = output;
		}
		return joinCustomChar("%0A", outputParts);
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
		if (parameters == null) parameters = new Class[0];
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
							if (!method1.getParameterTypes()[x].getName().equals(params[x].getName())) return null;
						return method1;
					}
				return null;
			}
		}.getMethod();
	}

	public static Set<Class<? extends Event>> getAllEvents() {
		return events;
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
		case "OFFLINE": {return OnlineStatus.OFFLINE;}
		case "INVISIBLE": {return OnlineStatus.INVISIBLE;}
		case "DND": {return OnlineStatus.DO_NOT_DISTURB;}
		case "DO_NOT_DISTURB": {return OnlineStatus.DO_NOT_DISTURB;}
		case "IDLE": {return OnlineStatus.IDLE;}
		case "ONLINE": {return OnlineStatus.ONLINE;}
		default: {return OnlineStatus.UNKNOWN;}
		}
	}

	public static void setAvatar(Icon avatar) {
		shards.get(0).getSelfUser().getManager().setAvatar(avatar).queue();
	}

	public static String getPrefix(Guild guild) {
		try {
			Map prefixes = DataIO.loadJson("data/mod/serverprefixes.json", Map.class);
			prefixes = prefixes == null ? new HashMap<>() : prefixes;
			String serverPrefix = Config.getValue("prefix");
			try {
				if (prefixes.containsKey(guild.getId())) serverPrefix = (String) ((Map) prefixes.get(guild.getId())).get("serverPrefix");
			} catch (NullPointerException e) { }
			return serverPrefix;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the server prefix file.", e);
		}
	}

	public static SelfUser getSelfUser() {
		return shards.get(0).getSelfUser();
	}

	/**
	 * Basically the same as what gets printed when you do cause.printStackTrace()
	 */
	public static String generateStackTrace(Throwable cause) {
		String stackTrace = String.format("%s: %s\n\t", cause.getClass().toString(), cause.getMessage());
		for (StackTraceElement element : cause.getStackTrace())
			stackTrace += String.format("at %s.%s(%s)\n\t", element.getClassName(), element.getMethodName(), element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source");
		while (cause.getCause() != null) {
			cause = cause.getCause();
			stackTrace = stackTrace.trim();
			stackTrace += String.format("\nCaused by %s: %s\n\t", cause.getClass().getName(), cause.getMessage());
			for (StackTraceElement element : cause.getStackTrace())
				stackTrace += String.format("at %s.%s(%s)\n\t", element.getClassName(), element.getMethodName(), element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source");
		}
		return stackTrace.trim();
	}

	public static String encase(String string) {
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}

	public static String pascalCase(String string) {
		String output = "";
		for (String part : string.split(" "))
			output += encase(part);
		return output;
	}

	public static String camelCase(String string) {
		return string.split(" ")[0].toLowerCase() + pascalCase(join(removeArg(string.split(" "), 0)));
	}

	public static List<Method> getMethods(Class clazz) {
		List<Method> methods = new ArrayList();
		for (Method method : clazz.getMethods())
			methods.add(method);
		for (Method method : clazz.getDeclaredMethods())
			if (!new Object() {
				boolean methodInList() {
					for (Method method1 : methods)
						if (method1.toString().equals(method.toString())) return true;
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
	 * If this throws a {@link java.lang.SecurityException SecurityException} then the command isn't executed and instead the message given with the exception is sent.
	 * @param hook The hook to add.
	 */
	public static void addCommandHook(CommandExecutionHook hook) {
		commandHooks.add(hook);
	}

	public static List<CommandExecutionHook> getCommandHooks() {
		return commandHooks;
	}

	public static List<Method> getSubcommands(Method parent) {
		List<Method> children = new ArrayList();
		for (Method subcommand : subcommands) {
			Method parentCommand;
			try {
				parentCommand = getParentCommand(subcommand.getAnnotation(Subcommand.class));
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				print(LogType.WARN, "The parent command of", subcommand, "could not be found.", subcommand.getAnnotation(Subcommand.class).parent());
				continue;
			}
			if (parentCommand.equals(parent)) children.add(subcommand);
		}
		return children;
	}

	public enum LogType {
		INFO(), ERROR(), WARN();
	}

}
