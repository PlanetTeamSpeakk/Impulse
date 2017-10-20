package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;

public class CommandMassDivorce extends Command {

	public CommandMassDivorce() {
		name = "massdivorce";
		help = "Divorces everyone in this server.";
		arguments = "";
		guildOnly = true;
		ownerCommand = false;
		userPermissions = new Permission[] {Permission.ADMINISTRATOR};
		botPermissions = new Permission[] {Permission.MANAGE_ROLES};
		category = Main.getCategory("Marriages");
	}

	@Override
	protected void execute(CommandEvent event) {
		List<Role> marriageRoles = new ArrayList<>();
		for (Role role : event.getGuild().getRoles())
			if (role.getName().contains("❤") && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRoles.add(role);
		for (Role role : marriageRoles)
			role.delete().queue();
		event.reply("Successfully deleted " + marriageRoles.size() + " marriage roles in this server.");
	}

}
