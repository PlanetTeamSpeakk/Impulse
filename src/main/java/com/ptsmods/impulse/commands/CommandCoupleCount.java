package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.entities.Role;

public class CommandCoupleCount extends Command {

	public CommandCoupleCount() {
		name = "couplecount";
		help = "Counts all the married couples in this server.";
		arguments = "";
		guildOnly = true;
		ownerCommand = false;
		category = Main.getCategory("Marriages");
	}

	@Override
	protected void execute(CommandEvent event) {
		List<Role> marriageRoles = new ArrayList<>();
		for (Role role : event.getGuild().getRoles())
			if (role.getName().contains("❤") && role.getColor().equals(new Color(Integer.parseInt("FF00EE", 16)))) marriageRoles.add(role);
		event.reply("There are currently " + marriageRoles.size() + " married couples in this server.");
	}

}
