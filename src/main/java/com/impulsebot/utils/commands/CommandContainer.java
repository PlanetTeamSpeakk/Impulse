package com.impulsebot.utils.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.impulsebot.Main;

public class CommandContainer {

	private final Method				command;
	private final ExtensionContainer	extension;
	private final boolean				isSubcommand;
	private static List<Method>			commandsWithContainers	= new ArrayList();

	CommandContainer(Method command, ExtensionContainer extension) {
		if (!command.isAnnotationPresent(Command.class) && !command.isAnnotationPresent(Subcommand.class)) throw new IllegalArgumentException("The given command is not annotated with Command nor Subcommand.");
		if (commandsWithContainers.contains(command))
			Main.throwCheckedExceptionWithoutDeclaration(new IllegalAccessException("The given command," + command + ", already has a container."));
		else commandsWithContainers.add(command);
		this.command = command;
		this.extension = extension;
		isSubcommand = command.isAnnotationPresent(Subcommand.class);
	}

	public Method toMethod() {
		return command;
	}

	public Command toCommand() {
		if (isSubcommand) {
			Main.throwCheckedExceptionWithoutDeclaration(new IllegalAccessException("The command in this container is a Subcommand and not a Command."));
			return null;
		} else return command.getAnnotation(Command.class);
	}

	public Subcommand toSubcommand() {
		if (!isSubcommand) {
			Main.throwCheckedExceptionWithoutDeclaration(new IllegalAccessException("The command in this container is a Command and not a Subcommand."));
			return null;
		} else return command.getAnnotation(Subcommand.class);
	}

	public boolean isSubcommand() {
		return isSubcommand;
	}

	public ExtensionContainer getExtension() {
		return extension;
	}

}
