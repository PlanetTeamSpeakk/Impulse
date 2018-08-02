package com.impulsebot.net.client;

import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.server.ServerDataRequestPacket;
import com.impulsebot.net.server.ServerDeliveryPacket;

public class ClientDeliveryPacket extends ClientPacket {

	private static final long				serialVersionUID	= 8997539602916686744L;
	public final ServerDataRequestPacket	request;

	public ClientDeliveryPacket(ServerDataRequestPacket request) {
		this.request = request;
	}

	@Override
	public void runOnServer() {
		ServerHandler.INSTANCE.sendPacket(new ServerDeliveryPacket(this), request.CDRSID);
	}

}
