package com.impulsebot.utils;

import java.io.File;
import java.io.IOException;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public final class LaughingMao {

	private static final String[] LMAO = {
			"\uD83C\uDDF1", 		 // regional_indicator_l
			"\uD83C\uDDF2",			// regional_indicator_m
			"\uD83C\uDDE6",		   // regional_indicator_a
			"\uD83C\uDDF4"		  // regional_indicator_o
	};

	public static Message sendLMao(MessageChannel channel, Message content) {
		if (!new File("data/fun/lmao.png").exists())
			try {
				Downloader.downloadFile("http://i.imgur.com/yfkKXGQ.png", "data/fun/lmao.png");
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		return channel.sendFile(new File("data/fun/lmao.png"), content).complete();
	}

	public static void addLmao(Message message) {
		for (String ch : LMAO)
			try {
				message.addReaction(ch).queue();
			} catch (InsufficientPermissionException ignored) {}
	}

}
