package com.impulsebot.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;
import com.impulsebot.utils.commands.CommandException;

public class Cleverbot {

	private static Map<String, Cleverbot>	bots	= new HashMap();
	private final String					key;
	private final ChatterBot				bot;
	private final ChatterBotSession			session;

	private Cleverbot(String key) {
		this.key = key;
		try {
			bot = new ChatterBotFactory().create(ChatterBotType.PANDORABOTS, "c6d6ba49eaadc69d0b0c116f33dfe07c");
			session = bot.createSession(Locale.ENGLISH);
		} catch (Exception e) {
			Main.throwCheckedExceptionWithoutDeclaration(e);
			throw new RuntimeException(e);
		}
	}

	public static Cleverbot newBot() {
		return getBot(Random.INSTANCE.genKey(128));
	}

	public static Cleverbot getBot(String key) {
		Cleverbot bot = bots.getOrDefault(key, new Cleverbot(key));
		bots.put(key, bot);
		return bot;
	}

	public String askQuestion(String question) throws CommandException {
		try {
			return session.think(question);
		} catch (Exception e) {
			throw new CommandException(e);
		}
	}

	public String getKey() {
		return key;
	}

}
