package com.impulsebot.net.server;

import com.impulsebot.miscellaneous.Main;

public class ServerUpdatePacket extends ServerPacket {

	private static final long	serialVersionUID	= 3056231353785056035L;
	private final int			guilds;
	private final int			users;

	public ServerUpdatePacket() {
		guilds = Main.getTotalGuildCount();
		users = Main.getTotalUserCount();
	}

	@Override
	public void runOnClient() {
		Main.addGuilds(guilds - Main.getTotalGuildCount());
		Main.addUsers(users - Main.getTotalUserCount());
	}

}
