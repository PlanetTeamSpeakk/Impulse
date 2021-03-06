package com.impulsebot.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.impulsebot.Main.LogType;
import com.impulsebot.commands.Economy;
import com.impulsebot.commands.Marriage;
import com.impulsebot.commands.Moderation;
import com.impulsebot.utils.upnp.UPnP;
import com.impulsebot.utils.upnp.UPnPProtocol;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 *
 * @author PlanetTeamSpeak
 */
public class Dashboard {

	public static final int							port		= 61192;
	private static CLHttpServer						server;
	private static Map<String, Map<String, String>>	dashboardData;
	private static Map<String, List<String>>		enabledModules;
	private static ThreadPoolExecutor				executor	= Main.newTPE();

	private Dashboard() {
	}

	static {
		try {
			dashboardData = DataIO.loadJsonOrDefault("data/dashboard/data.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			enabledModules = DataIO.loadJsonOrDefault("data/dashboard/enabledModules.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO: edit this so it runs on the server and not the clients and actually
	// works.
	public static void initialize() throws IOException {
		Main.print(LogType.INFO, "Starting Dashboard...");
		server = new CLHttpServer(HttpServer.create(new InetSocketAddress(port), 0));
		server.createContext("/", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				String contexts = "";
				for (HttpContext context : server.getContexts()) {
					String url = "http://localhost:" + server.getAddress().getPort() + context.getPath();
					contexts += "<br>&emsp;<a href=\"" + url + "\">" + url + "</a>";
				}
				writeString(he, "<p style=\"font-family:arial;\">Hello, and welcome to the Impulse API. Here you can get data from servers the bot is in, but only if you have a key.<br>The current contexts are:" + contexts + "</p>");
			}
		});
		server.createContext("/getVersion", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				writeString(he, "{\"Impulse\": \"%s\", \"JDA\": \"%s\", \"Java\": \"%s\"}", Main.version, JDAInfo.VERSION, System.getProperty("java.version"));
			}
		});
		server.createContext("/getCommands", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				writeString(he, false, Main.commandsToJson().toString(2));
			}
		});
		server.createContext("/isValidKey", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				Map<String, String> args = parseQuery(he.getRequestURI().getQuery());
				if (args.containsKey("key") && args.get("key") != null) {
					boolean isValid = dashboardData.containsKey(args.get("key")) && Main.getGuildById(dashboardData.get(args.get("key")).get("guild")) != null && Main.getGuildById(dashboardData.get(args.get("key")).get("guild")).getMemberById(dashboardData.get(args.get("key")).get("user")) != null && (Main.getGuildById(dashboardData.get(args.get("key")).get("guild")).getMemberById(dashboardData.get(args.get("key")).get("user")).hasPermission(Permission.ADMINISTRATOR) || dashboardData.get(args.get("key")).get("user").toString().equals(Config.INSTANCE.get("ownerId")));
					writeString(he, "{\"success\": true, \"isValid\": %s}", isValid);
					if (!isValid && dashboardData.containsKey(args.get("key"))) {
						dashboardData.remove(args.get("key"));
						DataIO.saveJson(dashboardData, "data/dashboard/data.json");
					}
				} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' not present.\"}");
			}
		});
		server.createContext("/getRoles", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				Map<String, String> args = parseQuery(he.getRequestURI().getQuery());
				if (args.containsKey("key")) {
					if (args.get("key") != null && dashboardData.containsKey(args.get("key"))) {
						if (Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()) != null) {
							String roles = "";
							for (Role role : Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()).getRoles())
								roles += "\"" + role.getName() + "\", ";
							writeString(he, "{\"success\": true, \"roles\": [%s], \"guild\": \"%s\", \"key\": \"%s\"}", roles.substring(0, roles.length() - 2), dashboardData.get(args.get("key")).get("guild"), args.get("key"));
						} else writeString(he, "{\"success\": false, \"errorMsg\": \"The guild attached to this key could not be found.\"}");
					} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not valid, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
				} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not present, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
			}
		});
		server.createContext("/getChannels", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				Map<String, String> args = parseQuery(he.getRequestURI().getQuery());
				if (args.containsKey("key")) {
					if (args.get("key") != null && dashboardData.containsKey(args.get("key"))) {
						if (Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()) != null) {
							String type = args.containsKey("type") ? args.get("type").equalsIgnoreCase("text") || args.get("type").equalsIgnoreCase("voice") || args.get("type").equalsIgnoreCase("both") ? args.get("type") : "BOTH" : "BOTH";
							String channels = "";
							boolean defaulted = false;
							switch (type.toUpperCase()) {
							default:
								defaulted = true;
							case "TEXT": {
								for (TextChannel channel : Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()).getTextChannels())
									channels += "\"" + channel.getName() + "\", ";
								if (!defaulted) break;
							}
							case "VOICE": {
								for (VoiceChannel channel : Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()).getVoiceChannels())
									channels += "\"" + channel.getName() + "\", ";
							}
							}
							writeString(he, "{\"success\": true, \"channels\": [%s], \"guild\": \"%s\", \"key\": \"%s\"}", channels.substring(0, channels.length() - 2), dashboardData.get(args.get("key")).get("guild"), args.get("key"));
						} else writeString(he, "{\"success\": false, \"errorMsg\": \"The guild attached to this key could not be found.\"}");
					} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not valid, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
				} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not present, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
			}
		});
		server.createContext("/getData", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				Map<String, String> args = parseQuery(he.getRequestURI().getQuery());
				if (args.containsKey("key")) {
					if (args.get("key") != null && dashboardData.containsKey(args.get("key"))) {
						if (Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()) != null)
							writeString(he, "{\"success\": true, \"data\": %s, \"guild\": \"%s\", \"key\": \"%s\"}", new Gson().toJson(getData(Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()))), dashboardData.get(args.get("key")).get("guild"), args.get("key"));
						else writeString(he, "{\"success\": false, \"errorMsg\": \"The guild attached to this key could not be found.\"}");
					} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not valid, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
				} else writeString(he, "{\"success\": false, \"errorMsg\": \"Argument 'key' is not present, this must be a key gotten using %sdashboard getkey.\"}", Config.INSTANCE.get("prefix"));
			}
		});
		server.createContext("/postData", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				if (he.getRequestMethod().equalsIgnoreCase("post")) {
					Map<String, Object> args;
					try {
						args = new Gson().fromJson(URLDecoder.decode(new BufferedReader(new InputStreamReader(he.getRequestBody(), "UTF-8")).readLine(), "UTF-8"), Map.class);
						if (args == null) throw new Exception();
					} catch (Exception e) {
						e.printStackTrace();
						writeString(he, "{\"success\": false, \"errorMsg\": \"An unknown error occurred while parsing the arguments, are you sure it is in JSON format?\"}");
						return;
					}
					Map<String, Object> data = (Map<String, Object>) args.get("data");
					if (args.containsKey("key")) {
						if (dashboardData.containsKey(args.get("key"))) {
							if (args.containsKey("data")) {
								if (Main.containsKeys(data, new String[] {"modules", "modSettings", "economySettings", "marriageSettings"})) {
									if (data.get("modules") instanceof List && data.get("modSettings") instanceof Map && data.get("economySettings") instanceof Map && data.get("marriageSettings") instanceof Map) {
										if (Main.containsKeys((Map) data.get("modSettings"), new String[] {"serverPrefix", "autorole", "autoroleEnabled", "banMentionSpam", "channel", "welcomeChannel", "greeting", "farewell", "dm", "disabled", "givemes", "logChannel", "enableLogging"})) {
											if (Main.containsKeys((Map) data.get("economySettings"), new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"})) {
												if (Main.containsKeys((Map) data.get("marriageSettings"), new String[] {"marryLimit"})) {
													Guild guild = Main.getGuildById(dashboardData.get(args.get("key")).get("guild"));
													try {
														Moderation.putSettings(guild, (Map) data.get("modSettings"));
														Economy.putSettings(guild, (Map) data.get("economySettings"));
														enabledModules.put(guild.getId(), (List) data.get("modules"));
														DataIO.saveJson(enabledModules, "data/dashboard/enabledModules.json");
													} catch (Exception e) {
														e.printStackTrace();
														writeString(he, "{\"success\": false, \"errorMsg\": \"An unknown error occurred while saving the data.\"}");
														return;
													}
													writeString(he, "{\"success\": true}");
													try {
														Main.sendPrivateMessage(Main.getUserById(dashboardData.get(args.get("key")).get("user").toString()), "You have successfully edited the settings of the **%s** server using the dashboard.", Main.getGuildById(dashboardData.get(args.get("key")).get("guild").toString()).getName());
													} catch (Exception e) {
													}
												} else writeString(he, "{\"success\": false, \"errorMsg\": \"The Map 'marriageSettings' should contain the key marryLimit.\"}");
											} else writeString(he, "{\"success\": false, \"errorMsg\": \"The Map 'economySettings' should contain the keys %s.\"}", Main.joinNiceString(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}));
										} else writeString(he, "{\"success\": false, \"errorMsg\": \"The Map 'modSettings' should contain the keys %s.\"}", Main.joinNiceString(new String[] {"serverPrefix", "autorole", "autoroleEnabled", "banMentionSpam", "channel", "welcomeChannel", "greeting", "farewell", "dm", "disabled", "givemes", "logChannel", "enableLogging"}));
									} else writeString(he, "{\"success\": false, \"errorMsg\": \"All arguments except for modules must be an instance of Map, modules must be an instance of List.\"}");
								} else writeString(he, "{\"success\": false, \"errorMsg\": \"The data parameter should contain args with the names 'modules', 'modSettings', 'economySettings', and 'marriageSettings'.\"}");
							} else writeString(he, "{\"success\": false, \"errorMsg\": \"The parameter data was not present.\"}");
						} else writeString(he, "{\"success\": false, \"errorMsg\": \"The given key was not valid.\"}");
					} else writeString(he, "{\"success\": false, \"errorMsg\": \"The parameter key was not present, make sure to put the args in a query string.\"}");
				} else writeString(he, "{\"success\": false, \"errorMsg\": \"The request method was %s while only POST requests are allowed on this URL.\"}", he.getRequestMethod());
			}
		});
		server.createContext("/devMode", new DefaultHttpHandler() {
			@Override
			public void handle0(HttpExchange he) throws IOException {
				writeString(he, "[" + Main.devMode() + "]");
			}
		});
		server.setExecutor(executor);
		server.start();
		Main.print(LogType.INFO, "Successfully started the Dashboard on port", server.getAddress().getPort() + ", you can browse to it by going to http://localhost:" + server.getAddress().getPort() + ".");
		UPnP.portForward(UPnPProtocol.BOTH, port, "Discord Bot API");
	}

	public static void writeString(HttpExchange he, String string, Object... args) throws IOException {
		writeString(he, true, string, args);
	}

	public static void writeString(HttpExchange he, boolean prettyPrintJson, String string, Object... args) throws IOException {
		if (args != null && args.length != 0) string = String.format(string, args);
		boolean isJson = true;
		boolean isHtml = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL).matcher(string).matches();
		Class type = null;
		// checking if the string which has to be written is JSON.
		try {
			new Gson().fromJson(string, ArrayMap.class);
			type = ArrayMap.class;
		} catch (JsonSyntaxException e) {
			try {
				new Gson().fromJson(string, List.class);
				type = List.class;
			} catch (JsonSyntaxException e1) {
				isJson = false;
			}
		}
		if (isJson) {
			he.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			string = gson.toJson(gson.fromJson(string, type)); // pretty printing
		} else if (isHtml)
			he.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		else he.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
		he.sendResponseHeaders(200, string.getBytes("UTF-8").length);
		BufferedOutputStream os = new BufferedOutputStream(he.getResponseBody());
		try {
			os.write(string.getBytes("UTF-8"));
			os.close();
		} catch (IOException ignored) {
			return; // user is probably just refreshing the page very fast.
		}
	}

	public static String createKey(Member member) throws IOException {
		if (!hasKey(member)) {
			if (!enabledModules.containsKey(member.getGuild().getId())) {
				enabledModules.put(member.getGuild().getId(), Lists.newArrayList("economy", "fun", "general", "lewd", "marriage", "miscellaneous", "moderation", "owner"));
				DataIO.saveJson(enabledModules, "data/dashboard/enabledModules.json");
			}
			String key = Random.INSTANCE.genKey(32);
			dashboardData.put(key, Main.newHashMap(new String[] {"guild", "user"}, new String[] {member.getGuild().getId(), member.getUser().getId()}));
			DataIO.saveJson(dashboardData, "data/dashboard/data.json");
			return key;
		} else return getKey(member);
	}

	public static String getKey(Member member) {
		if (hasKey(member)) for (String key : dashboardData.keySet())
			if (key.length() == 32 && dashboardData.get(key).get("guild").equals(member.getGuild().getId()) && dashboardData.get(key).get("user").equals(member.getUser().getId())) return key;
		return null;
	}

	public static boolean hasKey(Member member) {
		for (String key : dashboardData.keySet())
			if (key.length() == 32 && dashboardData.get(key).get("guild").equals(member.getGuild().getId()) && dashboardData.get(key).get("user").equals(member.getUser().getId())) return true;
		return false;
	}

	public static Map getData(Guild guild) {
		Map<String, Object> data = new HashMap();
		Map modSettings = Moderation.getSettings(guild);
		Map modlogSettings = Moderation.getModlogSettings(guild);
		Map<String, Integer> economySettings = Economy.getSettings(guild);
		data.put("modules", enabledModules.get(guild.getId()));
		data.put("modSettings", Main.newHashMap(new String[] {"serverPrefix", "autorole", "autoroleEnabled", "banMentionSpam", "channel", "welcomeChannel", "greeting", "farewell", "dm", "disabled", "givemes", "logChannel", "enableLogging"}, new Object[] {Main.getPrefix(guild) == null ? Config.INSTANCE.get("prefix") : Main.getPrefix(guild), modSettings.get("autorole") == null ? "" : modSettings.get("autorole").toString(), modSettings.get("autoroleEnabled") == null ? false : (boolean) modSettings.get("autoroleEnabled"), modSettings.get("banMentionSpam") == null ? false : (boolean) modSettings.get("banMentionSpam"), modSettings.get("channel") == null ? "" : modSettings.get("channel").toString(), modSettings.get("welcomeChannel") == null ? "" : modSettings.get("welcomeChannel").toString(), modSettings.get("greeting") == null ? "" : modSettings.get("greeting").toString(), modSettings.get("farewell") == null ? "" : modSettings.get("farewell").toString(), modSettings.get("dm") == null ? false : (boolean) modSettings.get("dm"), modSettings.get("welcomeChannel") == null || modSettings.get("welcomeChannel").toString().isEmpty(), new ArrayList(Moderation.getGivemeSettings(guild).keySet()), modlogSettings.get("channel") == null ? "" : modlogSettings.get("channel").toString(), modlogSettings.get("enabled") == null ? false : (boolean) modlogSettings.get("enabled")}
		// that's just Eclipse being weird, alright.
		));
		data.put("economySettings", economySettings);
		data.put("marriageSettings", Main.newHashMap(new String[] {"marryLimit"}, new Integer[] {Marriage.getMarryLimit(guild)}));
		return data;
	}

	public static List<String> getEnabledModules(Guild guild) {
		return enabledModules.get(guild.getId()) == null ? Lists.newArrayList("economy", "fun", "general", "lewd", "marriage", "miscellaneous", "moderation", "owner") : enabledModules.get(guild.getId());
	}

	public static Map<String, String> parseQuery(String queryArgs) {
		Map<String, String> args = new HashMap();
		if (queryArgs == null || queryArgs.isEmpty()) return args;
		try {
			queryArgs = URLDecoder.decode(queryArgs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		for (String arg : queryArgs.split("&"))
			if (arg.split("=").length > 1)
				args.put(arg.split("=")[0], arg.split("=")[1]);
			else args.put(arg, null);
		return args;
	}

	/**
	 * Supports JavaScript CORS requests by default, logs any traffic gotten, pretty
	 * prints JSON, and sends any errors to the owner.
	 *
	 * @author PlanetTeamSpeak
	 */
	public static abstract class DefaultHttpHandler implements HttpHandler {

		@Override
		public final void handle(HttpExchange he) throws IOException {
			he.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // support for JavaScript CORS requests, if you'd remove or comment out this
																				// line the entire dashboard wouldn't work anymore.
			try (PrintWriter writer = new PrintWriter(new FileWriter("webserver.log", true))) {
				writer.println(String.format("[%s %s] [INFO] Request gotten on %s from %s.", Main.getFormattedDate(), Main.getFormattedTime(), he.getRequestURI().getPath(), he.getRemoteAddress().getHostName().equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : he.getRemoteAddress().getHostName()));
				handle0(he);
			} catch (Throwable t) {
				t.printStackTrace();
				for (StackTraceElement element : t.getStackTrace())
					if (element.getClassName().startsWith("com.ptsmods.impulse")) {
						Main.sendPrivateMessage(Main.getOwner(), "A `%s` exception was thrown on line %s in %s while parsing a %s request on %s. ```java\n%s```", t.getClass().getName(), element.getLineNumber(), element.getFileName(), he.getRequestMethod(), he.getRequestURI().getPath(), Main.generateStackTrace(t));
						break;
					}
			}
		}

		public abstract void handle0(HttpExchange he) throws IOException;

	}

	private static class CLHttpServer extends HttpServer {

		private final HttpServer		server;
		private final List<HttpContext>	contexts	= new ArrayList();

		public CLHttpServer(HttpServer server) {
			this.server = server;
		}

		@Override
		public void bind(InetSocketAddress arg0, int arg1) throws IOException {
			server.bind(arg0, arg1);
		}

		@Override
		public HttpContext createContext(String arg0) {
			HttpContext context = server.createContext(arg0);
			contexts.add(context);
			return context;
		}

		@Override
		public HttpContext createContext(String arg0, HttpHandler arg1) {
			HttpContext context = server.createContext(arg0, arg1);
			contexts.add(context);
			return context;
		}

		@Override
		public InetSocketAddress getAddress() {
			return server.getAddress();
		}

		@Override
		public Executor getExecutor() {
			return server.getExecutor();
		}

		@Override
		public void removeContext(String arg0) throws IllegalArgumentException {
			for (HttpContext context : contexts)
				if (context.getPath().equals(arg0)) {
					contexts.remove(context);
					break;
				}
			server.removeContext(arg0);
		}

		@Override
		public void removeContext(HttpContext arg0) {
			contexts.remove(arg0);
			server.removeContext(arg0);
		}

		@Override
		public void setExecutor(Executor arg0) {
			server.setExecutor(arg0);
		}

		@Override
		public void start() {
			server.start();
		}

		@Override
		public void stop(int arg0) {
			server.stop(arg0);
		}

		public List<HttpContext> getContexts() {
			return Collections.unmodifiableList(contexts);
		}

	}

}
