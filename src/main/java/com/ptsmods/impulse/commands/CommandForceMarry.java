package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

public class CommandForceMarry extends Command {

	private static Map settings;

	public CommandForceMarry() {
		name = "forcemarry";
		help = "Forcibly marries 2 members, you pervert.";
		arguments = "<user1> <user2>";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
		botPermissions = new Permission[] {Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL};
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
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
			if (event.getMessage().getMentionedUsers().size() < 2) event.reply("Please mention the 2 users you'd like to forcibly marry each other.");
			else {
				Member mem1 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
				Member mem2 = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(1));
				if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"marryLimit", "disabled"}, new Object[] {-1, false}));
				MessageChannel marriageChannel = null;
				boolean isMarriedToMember = false;
				if (event.getGuild().getTextChannelsByName("marriage", true).isEmpty()) marriageChannel = event.getGuild().getTextChannelById(event.getGuild().getController().createTextChannel("marriage").complete().getId());
				if (marriageChannel == null) marriageChannel = event.getGuild().getTextChannelsByName("marriage", true).get(0);
				if (mem1.equals(mem2)) event.reply("People can't marry themselves, that would be weird wouldn't it?");
				else if ((boolean) ((Map) settings.get(event.getGuild().getId())).get("disabled")) event.reply("Marriages are currently disabled on this server.");
				else if (isMarriedToMember) event.reply("They're already married.");
				else if (mem2.equals(event.getSelfMember()) && !mem1.getUser().equals(Main.getOwner()) || mem1.equals(event.getSelfMember()) && !mem2.getUser().equals(Main.getOwner())) event.reply("I'd only marry my owner.");
				else {
					Role role = event.getGuild().getController().createRole().setName(String.format("%s " + CommandMarry.heart + " %s", mem1.getUser().getName(), mem2.getUser().getName())).setColor(new Color(Integer.parseInt("FF00EE", 16))).setPermissions(Permission.ALL_TEXT_PERMISSIONS).complete();
					event.getGuild().getController().addSingleRoleToMember(mem1, role).queue();
					event.getGuild().getController().addSingleRoleToMember(mem2, role).queue();
					marriageChannel.sendMessageFormat("%s was forced to marry %s.", mem2.getAsMention(), mem1.getAsMention()).queue();
					try {
						Main.sendPrivateMessage(mem1.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
								event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem2.getUser().getName(), mem2.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getClient().getPrefix(), role.getId(), event.getGuild().getName()));
					} catch (Throwable e) {
						event.reply("I wasn't able to send a direct message to " + mem1.getUser().getName() + ".");
					}
					try {
						Main.sendPrivateMessage(mem2.getUser(), String.format("**%s#%s** forced you to marry **%s#%s** in **%s**.\nYour divorce id is `%s`.\nTo divorce type `%sdivorce %s` in %s.",
								event.getAuthor().getName(), event.getAuthor().getDiscriminator(), mem1.getUser().getName(), mem1.getUser().getDiscriminator(), event.getGuild().getName(), role.getId(), Main.getClient().getPrefix(), role.getId(), event.getGuild().getName()));
					} catch (Throwable e) {
						event.reply("I wasn't able to send a direct message to " + mem2.getUser().getName() + ".");
					}
					event.replyFormatted("Successfully forced %s to marry %s.", mem2.getUser().getName(), mem1.getUser().getName());
				}
			}
		} else Main.sendCommandHelp(event, this);
	}

}
