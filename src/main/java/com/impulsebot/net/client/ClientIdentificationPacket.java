package com.impulsebot.net.client;

import java.util.Map.Entry;

import com.impulsebot.Main.LogType;
import com.impulsebot.commands.Main;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.server.ServerClientLoginPacket;
import com.impulsebot.net.server.ServerConnectionRefusedPacket;

public class ClientIdentificationPacket extends ClientPacket {

	private static final long	serialVersionUID	= -8936013710516429369L;
	private final int			shardId;

	public ClientIdentificationPacket() {
		super();
		shardId = Main.getShardId();
	}

	@Override
	public int getShardId() {
		return shardId;
	}

	@Override
	public void runOnServer() {
		Main.print(LogType.DEBUG, "Client identified");
		if (!Main.identifyClient(this))
			ServerHandler.INSTANCE.sendPacket(new ServerConnectionRefusedPacket(), getIdentifier());
		else if (Main.getIdentifications().size() == Main.getShardCount()) {
			Main.print(LogType.DEBUG, "All shards are online, logging in first shard.");
			for (Entry<String, Integer> identification : Main.getIdentifications().entrySet())
				if (identification.getValue() == 0) {
					ServerHandler.INSTANCE.sendPacket(new ServerClientLoginPacket(), identification.getKey());
					break;
				}
		}
		Main.print(LogType.DEBUG, Main.getIdentifications(), Main.getShardCount());
	}

}
