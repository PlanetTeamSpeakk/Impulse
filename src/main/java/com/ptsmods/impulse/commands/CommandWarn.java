package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class CommandWarn extends Command {

	private Map settings;

	public CommandWarn() {
		name = "warn";
		help = "Warns people.";
		arguments = "<user>";
		botPermissions = new Permission[] {Permission.BAN_MEMBERS, Permission.KICK_MEMBERS, Permission.MANAGE_ROLES};
		userPermissions = new Permission[] {Permission.KICK_MEMBERS};
		guildOnly = true;
		category = Main.getCategory("Moderation");
		Main.createDirectoryIfNotExisting("data/warner/");
		try {
			Main.createFileIfNotExisting("data/warner/settings.json");
		} catch (IOException e) {
			throw new RuntimeException("Could not create settings file.", e);
		}
		try {
			settings = DataIO.loadJson("data/warner/settings.json", Map.class);
		} catch (IOException e) {
			throw new RuntimeException("There was an error while loading the file.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			try {
				settings = DataIO.loadJson("data/warner/settings.json", Map.class);
			} catch (IOException e) {
				throw new RuntimeException("There was an error while loading the file.", e);
			}
			String guildId = event.getGuild().getId();
			String userId = Main.getUserFromInput(event.getMessage()).getId();
			Member member = event.getGuild().getMemberById(userId);
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
			if (!settings.containsKey(guildId)) settings.put(guildId, new HashMap<>());
			if (!event.getGuild().getSelfMember().canInteract(member))
				event.reply("I cannot mute, kick or ban that user as they're higher in the hierarchy than I am.");
			else if (!((Map) settings.get(guildId)).containsKey(userId)) {
				((Map) settings.get(guildId)).put(userId, 1);
				event.reply(member.getAsMention() + " has 1 warning, but nothing happens yet. Next up: 5 minute mute.");
				try {
					DataIO.saveJson(settings, "data/warner/settings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the file.");
					e.printStackTrace();
					return;
				}
			} else {
				Integer warnings = 1;
				try {
					warnings = (Integer) ((Map) settings.get(guildId)).get(userId) + 1;
				} catch (ClassCastException e) {
					warnings = ((Double) ((Map) settings.get(guildId)).get(userId)).intValue() + 1;
				}
				((Map) settings.get(guildId)).put(userId, warnings);
				String output = "";
				switch (warnings) {
				case 0: {output = "%s has **0 warnings**, dafuq? :thinking:"; break;}
				case 1: {output = "%s has **1 warning**, but nothing happens yet. Next up: **5 minute mute**."; break;}
				case 2: {output = "%s has **2 warnings**, and has been muted for 5 minutes. Next up: **30 minute mute**."; mute(member, 5); break;}
				case 3: {output = "%s has **3 warnings**, and has been muted for 30 minutes. Next up: **kick**."; mute(member, 30); break;}
				case 4: {output = "%s has **4 warnings**, and has been kicked. Next up: **ban**."; event.getGuild().getController().kick(member).complete(); break;}
				case 5: {output = "%s has **5 warnings**, and has been banned."; new CommandResetWarns().execute(event); event.getGuild().getController().ban(member, 1).complete(); break;}
				default: {output = "%s has **an unknown** amount of warnings. :thinking:"; break;}
				}
				try {
					DataIO.saveJson(settings, "data/warner/settings.json");
				} catch (IOException e) {
					event.reply("There was an error while saving the file.");
					e.printStackTrace();
					return;
				}
				event.reply(String.format(output, member.getAsMention()));

			}
		} else Main.sendCommandHelp(event, this);
	}

	private static void mute(Member member, int minutes) {
		Role role = Main.getRoleByName(member.getGuild(), "Impulse Muted", true);
		if (role == null)
			role = member.getGuild().getController().createRole().setName("Impulse Muted").setPermissions().complete();
		List<Role> roles = member.getRoles();
		List<Role> rolesToAdd = new ArrayList<>();
		rolesToAdd.add(role);
		List<Role> rolesToRemove = new ArrayList<>();
		for (Role r : roles) {
			if (r.isManaged() || !PermissionUtil.canInteract(member.getGuild().getSelfMember(), r))
				continue;
			if (r.equals(role))
				continue;
			rolesToRemove.add(r);
		}
		member.getGuild().getController().modifyMemberRoles(member, rolesToAdd, rolesToRemove).complete();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(60000*minutes);
				} catch (InterruptedException e) {}
				member.getGuild().getController().modifyMemberRoles(member, rolesToRemove, rolesToAdd).complete();
			}
		}).start();
	}

}
