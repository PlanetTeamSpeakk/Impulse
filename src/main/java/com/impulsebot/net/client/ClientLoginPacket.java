package com.impulsebot.net.client;

import com.impulsebot.Main;
import com.impulsebot.Main.LogType;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.server.ServerClientLoginPacket;
import com.impulsebot.net.server.ServerCustomPacket;
import com.impulsebot.net.server.ServerUpdatePacket;

import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;

public class ClientLoginPacket extends ClientPacket {

	private static final long	serialVersionUID	= 2922293453465127614L;
	private final boolean		successful;
	private final boolean		ownerFound;
	private final Throwable		cause;
	private final int			guilds;
	private final int			users;
	private final SelfUser		self;
	private final User			owner;

	public ClientLoginPacket(boolean successful, boolean ownerFound, Throwable cause, SelfUser self, User owner) {
		super();
		this.successful = successful;
		this.ownerFound = ownerFound;
		this.cause = cause;
		guilds = Main.getGuildCount();
		users = Main.getUserCount();
		this.self = Main.getShardCount() - 1 == Main.getShardId() ? self : null;
		this.owner = owner;
	}

	public boolean wasSuccessful() {
		return successful;
	}

	public boolean ownerFound() {
		return ownerFound;
	}

	public Throwable getCause() {
		return cause;
	}

	public User getOwner() {
		return owner;
	}

	@Override
	public void runOnServer() {
		if (self != null) Main.setSelfUser(self);
		if (owner != null) Main.setOwner(owner);
		if (getShardId() + 1 != Main.getShardCount()) {
			Main.addGuilds(guilds);
			Main.addUsers(users);
			ServerHandler.INSTANCE.sendPacket(new ServerClientLoginPacket(), getShardId() + 1);
			Main.print(LogType.DEBUG, "Sending login packet to next shard.");
		} else {
			Main.print(LogType.SUCCESSFUL, "Successfully logged in all shards, finishing up...");
			for (String id : Main.getIdentifications().keySet()) {
				ServerHandler.INSTANCE.sendPacket(new ServerUpdatePacket(), id);
				ServerHandler.INSTANCE.sendPacket(new ServerCustomPacket() {

					private static final long serialVersionUID = 7138332955180536112L;

					@Override
					public void runOnClient() {
						Main.finish();
					}
				}, id);
			}
			Main.print(LogType.SUCCESSFUL, String.format("Succesfully logged in as %s, took %s milliseconds. Owner = %s, prefix = %s.", Main.str(Main.getSelfUser()), System.currentTimeMillis() - Main.started.getTime(), "", Main.getPrefix(null)));
		}
	}

}
