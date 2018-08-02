package com.impulsebot.net.server;

import com.impulsebot.Main;
import com.impulsebot.Main.LogType;

public class ServerConnectionRefusedPacket extends ServerPacket {

	private static final long serialVersionUID = 2982745106375651005L;

	public ServerConnectionRefusedPacket() {

	}

	@Override
	public void runOnClient() {
		Main.print(LogType.INFO, "The connection was refused by the server, this is probably due to the fact that there is another instance running with the same shard id as this instance, exiting...");
		System.exit(0);
	}

}
