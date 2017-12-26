package com.ptsmods.impulse.miscellaneous;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.Config;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.Requester;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventHandler extends ListenerAdapter {

	private static Map<String, Map<String, Long>> cooldowns = new HashMap();

	@Override
	public void onGenericEvent(Event event) {
		List<Class> passedClasses = new ArrayList();
		for (Method cmd : Main.getCommands()) {
			if (passedClasses.contains(cmd.getDeclaringClass())) continue;
			passedClasses.add(cmd.getDeclaringClass());
			Class eventClass = event.getClass();
			Method method = Main.getMethod(cmd.getDeclaringClass(), "on" + eventClass.getSimpleName().replaceAll("Event", ""), eventClass);
			while (method == null && eventClass.getSuperclass() != null) {
				eventClass = eventClass.getSuperclass();
				method = Main.getMethod(cmd.getDeclaringClass(), "on" + eventClass.getSimpleName().replaceAll("Event", ""), eventClass);
			}
			if (method != null && method.isAnnotationPresent(SubscribeEvent.class)) {
				method.setAccessible(true);
				final Method method1 = method;
				Main.runAsynchronously(() -> {
					Object obj = null;
					try {
						obj = cmd.getDeclaringClass().newInstance();
					} catch (InstantiationException | IllegalAccessException e1) {}
					try {
						method1.invoke(obj, event);
					} catch (InvocationTargetException e) {
						e.getCause().printStackTrace();
					} catch (IllegalAccessException | IllegalArgumentException e) {
						e.printStackTrace();
					}
				});
			}
		}
	}

	@Override
	public void onReady(ReadyEvent event) {
		Main.print(LogType.INFO, "Shard " + event.getJDA().getShardInfo().getShardId() + " ready!");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Main.addReceivedMessage(event.getMessage());
		Main.runAsynchronously(new Runnable() {@Override public void run() {System.gc();}}); // it's not necessary, but it is said JDA is very resource demanding.
		if (event.getAuthor().isBot() || !Main.done()) return;
		String prefix = Main.getPrefix(event.getGuild());
		if (event.getMessage().getContent().startsWith(prefix) && Main.getCommandNames().contains(event.getMessage().getContent().split(" ")[0].substring(prefix.length())))
			Main.print(LogType.INFO, String.format("%s#%s used the '%s' command in %s.",
					event.getAuthor().getName(),
					event.getAuthor().getDiscriminator(),
					event.getMessage().getContent().substring(prefix.length()).replaceAll("\n", " "),
					event.getGuild() == null ? "private messages" : event.getGuild().getName()));
		Main.executeCommand(() -> {
			try {
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
							boolean ownerCommand = false;
							boolean sendTyping = true;
							double cooldown = 1D;
							if (command.isAnnotationPresent(Command.class)) {
								Command annotation = command.getAnnotation(Command.class);
								annotation.name();
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								ownerCommand = annotation.ownerCommand();
								cooldown = annotation.cooldown();
								sendTyping = annotation.sendTyping();
							} else if (command.isAnnotationPresent(Subcommand.class)) {
								Subcommand annotation = command.getAnnotation(Subcommand.class);
								annotation.name();
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								ownerCommand = annotation.ownerCommand();
								cooldown = annotation.cooldown();
								sendTyping = annotation.sendTyping();
							}
							if (cooldowns.getOrDefault(event.getAuthor().getId(), new HashMap()).containsKey(command.toString()) && System.currentTimeMillis()-cooldowns.get(event.getAuthor().getId()).get(command.toString()) < cooldown*1000)
								event.getChannel().sendMessage("You're still on cooldown, please try again in " + Main.formatMillis((long) (cooldown * 1000 - (System.currentTimeMillis()-cooldowns.get(event.getAuthor().getId()).get(command.toString()))), true, true, true, true, true, false) + ".").queue();
							else if (event.getGuild() == null && guildOnly)
								event.getChannel().sendMessage("That command cannot be used in direct messages.").queue();
							else if (ownerCommand && !event.getAuthor().getId().equals(Main.getOwner().getId()))
								event.getChannel().sendMessage("That command can only be used by my owner.").queue();
							else if (event.getMember() != null && !event.getMember().hasPermission(permissions)) {
								List<String> nonPresentPerms = new ArrayList();
								for (Permission perm : permissions)
									if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
								event.getChannel().sendMessage("You need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to use that.").queue();
							} else if (event.getGuild() != null && !event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(botPermissions)) {
								List<String> nonPresentPerms = new ArrayList();
								for (Permission perm : permissions)
									if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
								event.getChannel().sendMessage("I need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to do that.").queue();
							} else {
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
									new EventHandler().sendStackTrace(e.getCause(), event);
								}
							}
						}
					}
			} catch (Throwable e) {
				sendStackTrace(e, event);
			}
		});
	}

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		updateStats((JDAImpl) event.getJDA());
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		updateStats((JDAImpl) event.getJDA());
	}

	private static void updateStats(JDAImpl jda) {
		OkHttpClient client = jda.getHttpClientBuilder().build();
		if (Config.get("carbonitexKey") != null) {
			FormBody.Builder bodyBuilder = new FormBody.Builder()
					.add("key", Config.get("carbonitexKey"))
					.add("servercount", Integer.toString(jda.getGuilds().size()));

			if (jda.getShardInfo() != null)
				bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
				.add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));

			Request.Builder builder = new Request.Builder()
					.post(bodyBuilder.build())
					.url("https://www.carbonitex.net/discord/data/botdata.php");

			client.newCall(builder.build()).enqueue(new Callback() {
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					Main.print(LogType.INFO, "Joined a server and successfully updated the stats on carbonitex.net");
					response.close();
				}

				@Override
				public void onFailure(Call call, IOException e) {
					Main.print(LogType.WARN, "Joined a server, but could not update the stats on carbonitex.net");
					e.printStackTrace();
				}
			});
		}

		if (Config.get("discordBotListKey") != null) {
			JSONObject body = new JSONObject()
					.put("server_count", jda.getGuilds().size())
					.put("shard_id", jda.getShardInfo().getShardId())
					.put("shard_count", jda.getShardInfo().getShardTotal());

			Request.Builder builder = new Request.Builder()
					.post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
					.url("https://discordbots.org/api/bots/" + jda.getSelfUser().getId() + "/stats")
					.header("Authorization", Config.get("discordBotListKey"))
					.header("Content-Type", "application/json");

			client.newCall(builder.build()).enqueue(new Callback() {
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					Main.print(LogType.INFO, "Joined a server and successfully updated the stats on discordbots.org");
					response.close();
				}

				@Override
				public void onFailure(Call call, IOException e) {
					Main.print(LogType.WARN, "Joined a server, but could not update the stats on discordbots.org");
					e.printStackTrace();
				}
			});
		}

		if (Config.get("discordBotsKey") != null) {
			JSONObject body = new JSONObject()
					.put("server_count", jda.getGuilds().size())
					.put("shard_id", jda.getShardInfo().getShardId())
					.put("shard_count", jda.getShardInfo().getShardTotal());

			Request.Builder builder = new Request.Builder()
					.post(RequestBody.create(Requester.MEDIA_TYPE_JSON, body.toString()))
					.url("https://bots.discord.pw/api/bots/" + jda.getSelfUser().getId() + "/stats")
					.header("Authorization", Config.get("discordBotsKey"))
					.header("Content-Type", "application/json");

			client.newCall(builder.build()).enqueue(new Callback() {
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					Main.print(LogType.INFO, "Joined a server and successfully updated the stats on bots.discord.pw");
					response.close();
				}

				@Override
				public void onFailure(Call call, IOException e) {
					Main.print(LogType.WARN, "Joined a server, but could not update the stats on bots.discord.pw");
					e.printStackTrace();
				}
			});
		}
	}

	private void sendStackTrace(Throwable e, MessageReceivedEvent event) {
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

}
