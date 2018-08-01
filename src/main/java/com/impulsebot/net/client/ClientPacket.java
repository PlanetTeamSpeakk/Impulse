package com.impulsebot.net.client;

import java.io.Serializable;

import com.impulsebot.Main;

public abstract class ClientPacket implements Serializable {

	private static final long	serialVersionUID	= 6336733427034073958L;
	private final String		identifier;
	private final int			shardId;

	protected ClientPacket() {
		identifier = Main.getIdentifier();
		shardId = Main.getShardId();
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getShardId() {
		return shardId;
	}

	public abstract void runOnServer();

}
