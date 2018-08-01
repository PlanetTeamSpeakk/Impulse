package com.impulsebot.net.client;

import java.util.Map.Entry;
import java.util.UUID;

import com.impulsebot.Main;
import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.server.ServerDataRequestPacket;

public class ClientDataRequestPacket extends ClientPacket {

	public final UUID			uniqueId			= UUID.randomUUID();
	private static final long	serialVersionUID	= 3922659449338882168L;
	private final Class			clazz;
	private final String		id;
	public volatile Object		data;
	public volatile boolean		received			= false;

	public ClientDataRequestPacket(Class clazz, String id) {
		this.clazz = clazz;
		this.id = id;
		ClientHandler.INSTANCE.registerCDRPacket(this);
	}

	@Override
	public void runOnServer() {
		for (Entry<String, Integer> entry : Main.getIdentifications().entrySet())
			if (!entry.getValue().equals(getShardId())) ServerHandler.INSTANCE.sendPacket(new ServerDataRequestPacket(clazz, id, this), entry.getKey());
	}

}
