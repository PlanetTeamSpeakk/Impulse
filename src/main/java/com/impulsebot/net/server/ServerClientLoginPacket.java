package com.impulsebot.net.server;

import com.impulsebot.miscellaneous.Main;
import com.impulsebot.utils.Config;

public class ServerClientLoginPacket extends ServerPacket {

	private static final long	serialVersionUID	= -4789332418235182512L;
	private final Config		config;

	public ServerClientLoginPacket() {
		config = Config.INSTANCE;
	}

	@Override
	public void runOnClient() {
		Config.INSTANCE = config;
		Main.login();
	}

}
