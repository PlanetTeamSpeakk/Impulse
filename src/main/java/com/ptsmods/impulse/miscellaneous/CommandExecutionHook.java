package com.ptsmods.impulse.miscellaneous;

public interface CommandExecutionHook {

	public void run(CommandEvent event) throws CommandPermissionException;

}
