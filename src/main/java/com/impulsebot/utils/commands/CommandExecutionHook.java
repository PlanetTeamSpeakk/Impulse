package com.impulsebot.utils.commands;

public interface CommandExecutionHook {

	public void run(CommandEvent event) throws CommandPermissionException;

}
