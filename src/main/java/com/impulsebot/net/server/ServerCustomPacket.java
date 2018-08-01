package com.impulsebot.net.server;

public abstract class ServerCustomPacket extends ServerPacket {

	private static final long serialVersionUID = 1077166710718143912L;

	public ServerCustomPacket() {
	}

	@Override
	public abstract void runOnClient();

}
