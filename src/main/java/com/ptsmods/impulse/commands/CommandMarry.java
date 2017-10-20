package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

public class CommandMarry extends Command {

	public static final String heart = "❤";
	private static Map settings;

	public CommandMarry() {
		name = "marry";
		help = "Marry your crush.";
		arguments = "<user>";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
		botPermissions = new Permission[] {Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL};
		try {
			settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occured while loading the data file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			try {
				settings = DataIO.loadJson("data/marriage/settings.json", Map.class);
				settings = settings == null ? new HashMap() : settings;
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occured while loading the data file.", e);
			}
			Member member = Main.getMemberFromInput(event.getMessage());
			if (member == null) event.reply("The given user could not be found.");
			else {
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
				int marryLimit = -1;
				try {
					marryLimit = (Integer) ((Map) settings.get(event.getGuild().getId())).get("marryLimit") + 1;
				} catch (ClassCastException e) {
					marryLimit = ((Double) ((Map) settings.get(event.getGuild().getId())).get("marryLimit")).intValue() + 1;
				}
				List<Role> marriageRolesOfAuthor = new ArrayList<>();
				List<Role> marriageRolesOfCrush = new ArrayList<>();
				for (Role role : event.getMember().getRoles())
					if (role.getName().contains(heart) && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRolesOfAuthor.add(role);
				for (Role role : member.getRoles())
					if (role.getName().contains(heart) && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRolesOfCrush.add(role);
				MessageChannel marriageChannel = null;
				boolean isMarriedToMember = false;
				for (Role role : marriageRolesOfAuthor)
					if (role.getName().contains(member.getUser().getName())) isMarriedToMember = true;
				for (Role role : marriageRolesOfCrush)
					if (role.getName().contains(event.getAuthor().getName())) isMarriedToMember = true;
				if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());
				if (marriageChannel == null) marriageChannel = event.getGuild().getTextChannelsByName("marriage", true).get(0);
				if (event.getMember().equals(member)) event.reply("You can't marry yourself, that would be weird wouldn't it?");
				else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled")) event.reply("Marriages are currently disabled in this server.");
				else if (marryLimit > 0 && marriageRolesOfAuthor.size() >= marryLimit) event.reply("You have reached this server's marry limit. (" + marryLimit + ")");
				else if (marryLimit > 0 && marriageRolesOfCrush.size() >= marryLimit) event.reply("The user you're trying to marry has reached their marry limit. (" + marryLimit + ")");
				else if (isMarriedToMember) event.reply("You're already married to that person.");
				else if (member.equals(event.getSelfMember()) && !event.getMember().getUser().equals(Main.getOwner())) event.reply("I'd only marry my owner.");
				else {
					event.replyFormatted("%s, do you take %s as your husband/wife? (yes/no)", member.getAsMention(), event.getAuthor().getAsMention());
					Message response = Main.waitForInput(member, event.getChannel(), 60000, event.getMessage().getCreationTime().toEpochSecond());
					if (response == null) event.replyFormatted("%s, the user you tried to marry did not respond, I'm sorry.", event.getAuthor().getAsMention());
					else if (!response.getContent().startsWith("ye")) event.replyFormatted("%s, the user you tried to marry did not say yes, I'm sorry.", event.getAuthor().getAsMention());
					else {
						Role role = event.getGuild().getController().createRole().setName(String.format("%s " + heart + " %s", event.getAuthor().getName(), member.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions(Permission.ALL_TEXT_PERMISSIONS).complete();
						event.getGuild().getController().addSingleRoleToMember(event.getMember(), role).queue();
						event.getGuild().getController().addSingleRoleToMember(member, role).queue();
						marriageChannel.sendMessageFormat("%s married %s, congratulations!", event.getAuthor().getAsMention(), member.getAsMention()).queue();
						try {
							Main.sendPrivateMessage(event.getAuthor(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
									member.getUser().getName(), member.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getClient().getPrefix(), role.getId(), event.getGuild().getName()));
						} catch (Throwable e) {
							event.reply("I wasn't able to send a direct message to " + event.getAuthor().getName() + ".");
						}
						try {
							Main.sendPrivateMessage(member.getUser(), String.format("You married **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
									event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getClient().getPrefix(), role.getId(), event.getGuild().getName()));
						} catch (Throwable e) {
							event.reply("I wasn't able to send a direct message to " + member.getUser().getName() + ".");
						}
					}
				}
			}
		} else Main.sendCommandHelp(event, this);
	}

}
