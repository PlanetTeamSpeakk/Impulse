package com.impulsebot.net.server;

import com.impulsebot.Main.LogType;
import com.impulsebot.commands.Main;
import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.client.ClientIdentificationPacket;

import io.netty.channel.ChannelId;

public class ServerClientIdentifierPacket extends ServerPacket {

	private static final long	serialVersionUID	= 2926034108311396059L;
	private final String		id;
	private final int			shardCount;

	public ServerClientIdentifierPacket(ChannelId id) {
		this.id = id.asLongText();
		shardCount = Main.getShardCount();
	}

	public String getIdentifier() {
		return id;
	}

	@Override
	public void runOnClient() {
		Main.print(LogType.DEBUG, "Identifier =", id);
		Main.setShardCount(shardCount);
		Main.setIdentifier(this);
		ClientHandler.INSTANCE.sendPacket(new ClientIdentificationPacket());
	}

}
