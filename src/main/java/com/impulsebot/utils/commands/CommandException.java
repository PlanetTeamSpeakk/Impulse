package com.impulsebot.utils.commands;

public class CommandException extends Exception {

	private static final long serialVersionUID = 633902191760429602L;

	public CommandException() {
		super();
	}

	public CommandException(Throwable cause) {
		super(cause);
	}

	public CommandException(String message) {
		super(message);
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

}
