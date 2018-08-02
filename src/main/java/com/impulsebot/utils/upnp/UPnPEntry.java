package com.impulsebot.utils.upnp;

import java.net.InetAddress;

public class UPnPEntry {

	private final UPnPProtocol	protocol;
	private final int			port;
	private final String		description;
	private final InetAddress	ip;
	private final boolean		enabled;

	UPnPEntry(UPnPProtocol protocol, int port, String description, InetAddress ip, boolean enabled) {
		this.protocol = protocol;
		this.port = port;
		this.description = description;
		this.ip = ip;
		this.enabled = enabled;
	}

	public UPnPProtocol getProtocol() {
		return protocol;
	}

	public int getPort() {
		return port;
	}

	public String getDescription() {
		return description;
	}

	public InetAddress getIp() {
		return ip;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
