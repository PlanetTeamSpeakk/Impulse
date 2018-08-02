package com.impulsebot.net.server;

import java.util.UUID;

import com.impulsebot.net.ClientHandler;
import com.impulsebot.net.ServerHandler;
import com.impulsebot.net.client.ClientDataRequestPacket;
import com.impulsebot.net.client.ClientDeliveryPacket;
import com.impulsebot.utils.Main;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class ServerDataRequestPacket extends ServerPacket {

	public final UUID			uniqueId			= UUID.randomUUID();
	private static final long	serialVersionUID	= 7185104736847131024L;
	private final Class			clazz;
	private final String		id;
	public final UUID			CDRID;
	public final int			CDRSID;
	public Object				data;
	public boolean				received			= false;

	public ServerDataRequestPacket(Class clazz, String id, ClientDataRequestPacket CDR) {
		this.clazz = clazz;
		this.id = id;
		CDRID = CDR.uniqueId;
		CDRSID = CDR.getShardId();
		ServerHandler.INSTANCE.registerSDRPacket(this);
	}

	@Override
	public void runOnClient() {
		if (clazz == User.class) {
			for (User user : Main.getUsers())
				if (user.getId().equals(id)) data = user;
		} else if (clazz == Guild.class) {
			for (Guild guild : Main.getGuilds())
				if (guild.getId().equals(id)) data = guild;
		} else if (clazz == TextChannel.class) {
			for (TextChannel channel : Main.getTextChannels())
				if (channel.getId().equals(id)) data = channel;
		} else if (clazz == VoiceChannel.class) for (VoiceChannel channel : Main.getVoiceChannels())
			if (channel.getId().equals(id)) data = channel;
		ClientHandler.INSTANCE.sendPacket(new ClientDeliveryPacket(this));
	}

}
