package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.IOException;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public final class LaughingMao {

	public static final Message sendLMao(MessageChannel channel, Message content) {
		if (!new File("data/fun/lmao.png").exists())
			try {
				Downloader.downloadFile("http://i.imgur.com/yfkKXGQ.png", "data/fun/lmao.png");
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		return channel.sendFile(new File("data/fun/lmao.png"), content).complete();
	}

}
