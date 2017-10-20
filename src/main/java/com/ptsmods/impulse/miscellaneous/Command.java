package com.ptsmods.impulse.miscellaneous;

import java.util.function.BiConsumer;

import net.dv8tion.jda.core.Permission;

public abstract class Command extends com.jagrosh.jdautilities.commandclient.Command {

	protected boolean isSubcommand = false;

	@Override
	public com.jagrosh.jdautilities.commandclient.Command[] getChildren() {
		return children;
	}

	public boolean isSubcommand() {
		return isSubcommand;
	}

	public Command setAliases(String... aliases) {
		this.aliases = aliases;
		return this;
	}

	public Command setArguments(String arguments) {
		this.arguments = arguments;
		return this;
	}

	public Command setBotPermissions(Permission... permissions) {
		botPermissions = permissions;
		return this;
	}

	public Command setCategory(Category category) {
		this.category = category;
		return this;
	}

	public Command setChildren(Command... children) {
		this.children = children;
		return this;
	}

	public Command setCooldown(int cooldown) {
		this.cooldown = cooldown;
		return this;
	}

	public Command setCooldownScope(CooldownScope cooldownScope) {
		this.cooldownScope = cooldownScope;
		return this;

	}

	public Command setGuildOnly(boolean guildOnly) {
		this.guildOnly = guildOnly;
		return this;
	}

	public Command setHelp(String help) {
		this.help = help;
		return this;
	}

	public Command setHelpBiConsumer(BiConsumer helpBiConsumer) {
		this.helpBiConsumer = helpBiConsumer;
		return this;
	}

	public Command setSubCommand(boolean isSubcommand) {
		this.isSubcommand = isSubcommand;
		return this;
	}

	public Command setName(String name) {
		this.name = name;
		return this;
	}

	public Command setOwnerCommand(boolean ownerCommand) {
		this.ownerCommand = ownerCommand;
		return this;
	}

	public Command setRequiredRole(String requiredRole) {
		this.requiredRole = requiredRole;
		return this;
	}

	public Command setUserPermissions(Permission[] permissions) {
		userPermissions = permissions;
		return this;
	}

	public Command setUsesTopicTags(boolean usesTopicTags) {
		this.usesTopicTags = usesTopicTags;
		return this;
	}

}
