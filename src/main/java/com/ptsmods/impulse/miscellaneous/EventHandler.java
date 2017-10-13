package com.ptsmods.impulse.miscellaneous;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

	private static Map<Long, List<String>> pastNames;
	private static Map<Long, List<String>> pastNicks;

	public EventHandler() throws IOException {
		pastNames = DataIO.loadJson("data/mod/pastNames.json", Map.class);
		pastNicks = DataIO.loadJson("data/mod/pastNicks.json", Map.class);
		if (pastNames == null) pastNames = new HashMap<>();
		if (pastNicks == null) pastNicks = new HashMap<>();
	}

	@Override
	public void onReady(ReadyEvent event) {
		Main.print(LogType.INFO, "Shard " + event.getJDA().getShardInfo().getShardId() + " ready!");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Main.addReceivedMessage(event.getMessage());
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		if (!pastNicks.containsKey(event.getUser().getIdLong())) pastNicks.put(event.getUser().getIdLong(), Lists.newArrayList(event.getPrevNick()));
		else pastNicks.get(event.getUser().getIdLong()).add(event.getPrevNick());
		try {
			DataIO.saveJson(pastNicks, "data/mod/pastNicks.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUserNameUpdate(UserNameUpdateEvent event) {
		if (!pastNames.containsKey(event.getUser().getIdLong())) pastNicks.put(event.getUser().getIdLong(), Lists.newArrayList(event.getOldName()));
		else pastNames.get(event.getUser().getIdLong()).add(event.getOldName());
		try {
			DataIO.saveJson(pastNames, "data/mod/pastNames.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
