package com.impulsebot.net.server;

import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.client.ClientDeliveryPacket;

public class ServerDeliveryPacket extends ServerPacket {

	private static final long			serialVersionUID	= 8900648225086259149L;
	private final ClientDeliveryPacket	delivery;

	public ServerDeliveryPacket(ClientDeliveryPacket delivery) {
		this.delivery = delivery;
	}

	@Override
	public void runOnClient() {
		ClientHandler.INSTANCE.setData(delivery.request.CDRID, delivery.request.data);
	}

}
