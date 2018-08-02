package com.impulsebot.utils.commands;

public interface Extension {

	public String getName();

	public String getVersion();

	public String getDescription();

	public String getCommandsPackage();

	public void onEnable();

	public void onDisable();

}
