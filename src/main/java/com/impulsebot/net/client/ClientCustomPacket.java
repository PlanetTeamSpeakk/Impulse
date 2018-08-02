package com.impulsebot.net.client;

public abstract class ClientCustomPacket extends ClientPacket {

	private static final long serialVersionUID = 3245641205108796845L;

	public ClientCustomPacket() {
	}

	@Override
	public abstract void runOnServer();

}
