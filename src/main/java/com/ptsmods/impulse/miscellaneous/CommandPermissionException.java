package com.ptsmods.impulse.miscellaneous;

public class CommandPermissionException extends Exception {
	private static final long serialVersionUID = -3255903699103800237L;

	public CommandPermissionException() {
		super();
	}

	public CommandPermissionException(String message, Object... args) {
		super(String.format(message, args));
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
