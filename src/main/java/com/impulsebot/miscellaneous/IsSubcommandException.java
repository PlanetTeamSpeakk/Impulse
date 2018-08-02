package com.impulsebot.miscellaneous;

public class IsSubcommandException extends Throwable {

	private static final long serialVersionUID = 1629898846588427417L;

	public IsSubcommandException() {
		super();
	}

	public IsSubcommandException(String message) {
		super(message);
	}

	public IsSubcommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public IsSubcommandException(Throwable cause) {
		super(cause);
	}

	public IsSubcommandException(String message, Throwable cause, boolean enableSuppression, boolean writeableStacktrace) {
		super(message, cause, enableSuppression, writeableStacktrace);
	}

}
