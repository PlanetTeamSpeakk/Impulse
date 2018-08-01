package com.impulsebot.net;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.impulsebot.Main.LogType;
import com.impulsebot.net.client.ClientDataRequestPacket;
import com.impulsebot.net.client.ClientPacket;
import com.impulsebot.net.server.ServerPacket;
import com.impulsebot.utils.Main;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

	public static final ClientHandler			INSTANCE	= new ClientHandler();
	private Map<UUID, ClientDataRequestPacket>	CDRs		= new HashMap();

	@Override
	public void channelRead(ChannelHandlerContext channelHandlerContext, Object in) {
		if (in instanceof DataPacket) {
			ServerPacket packet = ((DataPacket<ServerPacket>) in).toObject();
			in = packet;
		}
		if (in instanceof ServerPacket)
			((ServerPacket) in).runOnClient();
		else Main.print(LogType.WARN, "An unknown object was received over the netty connection, please make sure the client and the server are of the same major and preferably also the same minor versions.", in.getClass().getName());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
		cause.printStackTrace();
		channelHandlerContext.close();
	}

	public void sendPacket(ClientPacket packet) {
		if (Main.getPipeline() != null) Main.getPipeline().writeAndFlush(new DataPacket(packet));
	}

	public void registerCDRPacket(ClientDataRequestPacket CDR) {
		CDRs.put(CDR.uniqueId, CDR);
	}

	public void setData(UUID id, Object data) {
		ClientDataRequestPacket CDR = CDRs.get(id);
		if (data != null && data.getClass().getName().startsWith("net.dv8tion.jda.core.entities")) Main.fixJDAEntity(data);
		if (CDR.data == null) CDR.data = data;
		CDR.received = Main.getShardId() + 1 == Main.getShardCount() ? true : CDR.data != null;
	}

}
