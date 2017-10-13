package com.ptsmods.impulse.commands;

import java.awt.Color;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.EmbedBuilder;

public class CommandInfo extends Command {

	public CommandInfo() {
		name = "info";
		help = "Some information about the bot.";
		aliases = new String[] {"about"};
		guildOnly = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(event.getJDA().getSelfUser().getName());
		embed.setColor(new Color(Random.randInt(256*256*256)));
		embed.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl() == null ? event.getJDA().getSelfUser().getDefaultAvatarUrl() : event.getJDA().getSelfUser().getAvatarUrl());
		embed.setDescription("This bot is an instance of Impulse, a Discord Bot written by PlanetTeamSpeak using JDA and JDA Utilities. "
				+ "If you want your own bot with all these commands, make sure to check out [the GitHub page](https://github.com/PlanetTeamSpeakk/Impulse \"Yes, it's open source.\") or [the Discord Server](https://discord.gg/tzsmCyk \"Yes, I like advertising.\").");
		event.reply(embed.build());
	}

}
