package com.ptsmods.impulse.miscellaneous;

public class CommandPermissionException extends Exception {
	private static final long serialVersionUID = -3255903699103800237L;

	public CommandPermissionException(String message) {
		super(message);
	}

	public CommandPermissionException(Throwable cause) {
		super(cause);
	}

	public CommandPermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandPermissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
