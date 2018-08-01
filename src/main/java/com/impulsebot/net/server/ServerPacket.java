package com.impulsebot.net.server;

import java.io.Serializable;

public abstract class ServerPacket implements Serializable {

	private static final long serialVersionUID = 7282527600480060484L;

	public abstract void runOnClient();

}
