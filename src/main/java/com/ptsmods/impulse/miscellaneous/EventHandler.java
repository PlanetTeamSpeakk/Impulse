package com.ptsmods.impulse.miscellaneous;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

import org.json.JSONObject;

import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.ConsoleColors;
import com.ptsmods.impulse.utils.EventListenerManager;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
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

	@Override
	public void onGenericEvent(Event event) {
		EventListenerManager.postEvent(event);
	}

	@Override
	public void onReady(ReadyEvent event) {
		Main.print(LogType.INFO, "Shard " + event.getJDA().getShardInfo().getShardId() + " ready!");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Main.addReceivedMessage(event.getMessage());
		if (event.getAuthor().isBot() || !Main.done()) return;
		String prefix = Main.getPrefix(event.getGuild());
		if (event.getMessage().getRawContent().startsWith(prefix) && Main.getCommandNames().contains(event.getMessage().getRawContent().split(" ")[0].substring(prefix.length()))) {
			Main.print(LogType.INFO, String.format("%s#%s used the '%s' command in %s.",
					event.getAuthor().getName(),
					event.getAuthor().getDiscriminator(),
					event.getMessage().getContent().substring(prefix.length()).replaceAll("\n", " "),
					event.getGuild() == null ? "private messages" : event.getGuild().getName()));
			Main.executeCommand(event);
		}
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

	@Override
	public void onShutdown(ShutdownEvent event) {
		Main.print(LogType.DEBUG, "Cleaning log...");
		List<String> lines;
		try {
			lines = Files.readAllLines(new File("bot.log").toPath());
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		try (PrintWriter writer = new PrintWriter(new FileWriter("bot.log", false))) {
			for (String line : lines)
				writer.println(ConsoleColors.getCleanString(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Main.print(LogType.DEBUG, "Log cleaned.");
	}

}
