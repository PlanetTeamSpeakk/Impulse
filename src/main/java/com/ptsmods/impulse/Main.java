package com.ptsmods.impulse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import com.ptsmods.impulse.utils.Config;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class Main {

	private static List<JDA> shards = new ArrayList<>();

	public static void main(String[] args) {
		long millis = System.currentTimeMillis();
		try {
			if (Config.getKey("token") == null) {
				Config.addComment("Config file created by Impulse Discord Bot written by PlanetTeamSpeak.");
				Config.addComment("The bot token used to log in, example: MzIzMTc2ODk2NTAwMzM0NTk0.DLfRfw.zZ7V1ljky12M5sKEVUZBAzwYUbo.");
				Config.addValuePair("token", "");
				Config.addComment("The ID of the owner, this should be your id as anyone who has this can do anything with your bot in any server.");
				Config.addValuePair("ownerId", "");
				print(LogType.WARN, "The config hasn't been changed yet, please open config.cfg and change the variables.");
				System.exit(0);
			} else if (Config.getKey("token").equals("")) {
				print(LogType.WARN, "The config hasn't been changed yet, please open config.cfg and change the variables.");
				System.exit(0);
			}
			print(LogType.INFO, "Logging in...");
			for (int i : new int[2]) {
				JDA shard = new JDABuilder(AccountType.BOT).setToken(Config.getKey("token")).useSharding(shards.size(), shards.size() + 1).buildBlocking();
				shards.add(shard);
			}
		} catch (LoginException e) {
			print(LogType.ERROR, "The bot could not log in with the given token, please change the token in config.cfg.");
			System.exit(0);
		} catch (RateLimitedException e) {
			print(LogType.ERROR, "The bot has been rate limited, please try restarting.");
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
		print(LogType.INFO, "Succesfully logged in, took " + (System.currentTimeMillis() - millis) + " milliseconds.");
	}

	public static <T> void print(String threadName, LogType logType, T... message) {
		String[] args = new String[message.length];
		for (int x = 0; x < message.length; x++)
			if (message[x] == null) args[x] = "null";
			else args[x] = message[x].toString();
		if (logType == LogType.INFO) System.out.print("[" + getFormattedTime() + "] [" + threadName + "/INFO]: " + join(args) + "\n");
		else if (logType == LogType.WARN) System.out.print("\u001B[33m[" + getFormattedTime() + "] [" + threadName + "/WARN]: " + join(args) + "\u001B[0m\n");
		else if (logType == LogType.ERROR) System.err.print("[" + getFormattedTime() + "] [" + threadName + "/ERROR]: " + join(args) + "\n");
	}

	public static String getFormattedTime() {
		return joinCustomChar(":", "" + (LocalDateTime.now().getHour() < 10 ? "0" : "") + LocalDateTime.now().getHour(),
				"" + (LocalDateTime.now().getMinute() < 10 ? "0" : "") + LocalDateTime.now().getMinute(),
				"" + (LocalDateTime.now().getSecond() < 10 ? "0" : "") + LocalDateTime.now().getSecond());
	}

	public static <T> void print(LogType logType, T... message) {
		print(shards.size() > 0 ? shards.get(0).getSelfUser().getName() : "Impulse", logType, message);
	}

	public static String join(String... stringArray) {
		return joinCustomChar(" ", stringArray);
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

	public enum LogType {
		INFO(), ERROR(), WARN();
	}

}
