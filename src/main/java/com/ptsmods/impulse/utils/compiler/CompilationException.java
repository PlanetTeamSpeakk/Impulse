package com.ptsmods.impulse.utils.compiler;

import java.io.PrintStream;
import java.io.PrintWriter;

import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.commands.Main;

public class CompilationException extends Exception {

	private static final long	serialVersionUID	= -4878734843431493652L;
	private final String		line;
	private final int			character;
	private final String		error;

	public CompilationException(String line, int character, String error) {
		super();
		this.line = line;
		this.character = character;
		this.error = error;
	}

	public CompilationException(String error) {
		this(null, -1, error);
	}

	public String getLine() {
		return line;
	}

	public int getCharacter() {
		return character;
	}

	public String getError() {
		return error;
	}

	@Override
	public String toString() {
		return getLine() == null ? getError() : getError() + "\n" + getLine() + "\n" + Main.multiplyString(" ", character == 0 ? 0 : character - 1) + '^';
	}

	@Override
	public void printStackTrace(PrintStream stream) {
		Main.print(LogType.DEBUG, toString());
	}

	@Override
	public void printStackTrace(PrintWriter writer) {
		Main.print(LogType.DEBUG, toString());
	}

}
