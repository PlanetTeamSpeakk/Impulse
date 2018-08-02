package com.impulsebot.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.impulsebot.Main.LogType;
import com.impulsebot.commands.Main;
import com.impulsebot.net.client.ClientPacket;
import com.impulsebot.net.server.ServerDataRequestPacket;
import com.impulsebot.net.server.ServerPacket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

	public static final ServerHandler			INSTANCE	= new ServerHandler();
	private Map<UUID, ServerDataRequestPacket>	SDRs		= new HashMap();
	private int									biggestSent	= 0;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
		if (in instanceof DataPacket) {
			ClientPacket packet = ((DataPacket<ClientPacket>) in).toObject();
			in = packet;
		}
		if (in instanceof ClientPacket)
			((ClientPacket) in).runOnServer();
		else Main.print(LogType.WARN, "An unknown object was received over the netty connection, please make sure the client and the server are of the same major and preferably also the same minor versions.", in.getClass().getName());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	public void sendPacket(ServerPacket packet, int shardId) {
		for (Entry<String, Integer> entry : Main.getIdentifications().entrySet())
			if (entry.getValue().equals(shardId)) sendPacket(packet, entry.getKey());
	}

	public void sendPacket(ServerPacket packet, String channelId) {
		if (Main.getPipelines().get(channelId) != null) {
			DataPacket dPacket = new DataPacket(packet);
			Main.getPipelines().get(channelId).writeAndFlush(dPacket);
			int length = Main.serializeObject(dPacket).length;
			if (length > biggestSent) {
				biggestSent = length;
				Main.print(LogType.DEBUG, "Last packet sent exceeded last biggest packet in length, new record =", biggestSent);
			}
		}
	}

	public void registerSDRPacket(ServerDataRequestPacket SDR) {
		SDRs.put(SDR.uniqueId, SDR);
	}

	public void setData(UUID id, Object data) {
		ServerDataRequestPacket SDR = SDRs.get(id);
		if (SDR.data == null) SDR.data = data;
		SDR.received = true;
	}

}
