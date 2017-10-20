package com.ptsmods.impulse.utils;

import org.fusesource.jansi.AnsiConsole;

public class ConsoleColors {

	public static final String BLACK = "\u001b[0;30m";
	public static final String RED = "\u001b[0;31m";
	public static final String GREEN = "\u001b[0;32m";
	public static final String YELLOW = "\u001b[0;33m";
	public static final String BLUE = "\u001b[0;34m";
	public static final String PURPLE = "\u001b[0;35m";
	public static final String CYAN = "\u001b[0;36m";
	public static final String WHITE = "\u001b[0;37m";

	public static final String BOLD_BLACK = "\u001b[1;30m";
	public static final String BOLD_RED = "\u001b[1;31m";
	public static final String BOLD_GREEN = "\u001b[1;32m";
	public static final String BOLD_YELLOW = "\u001b[1;33m";
	public static final String BOLD_BLUE = "\u001b[1;34m";
	public static final String BOLD_PURPLE = "\u001b[1;35m";
	public static final String BOLD_CYAN = "\u001b[1;36m";
	public static final String BOLD_WHITE = "\u001b[1;37m";

	public static final String UNDERLINE_BLACK = "\u001b[4;30m";
	public static final String UNDERLINE_RED = "\u001b[4;31m";
	public static final String UNDERLINE_GREEN = "\u001b[4;32m";
	public static final String UNDERLINE_YELLOW = "\u001b[4;33m";
	public static final String UNDERLINE_BLUE = "\u001b[4;34m";
	public static final String UNDERLINE_PURPLE = "\u001b[4;35m";
	public static final String UNDERLINE_CYAN = "\u001b[4;36m";
	public static final String UNDERLINE_WHITE = "\u001b[4;37m";

	public static final String BACKGROUND_BLACK = "\u001b[40m";
	public static final String BACKGROUND_RED = "\u001b[41m";
	public static final String BACKGROUND_GREEN = "\u001b[42m";
	public static final String BACKGROUND_YELLOW = "\u001b[43m";
	public static final String BACKGROUND_BLUE = "\u001b[44m";
	public static final String BACKGROUND_PURPLE = "\u001b[45m";
	public static final String BACKGROUND_CYAN = "\u001b[46m";
	public static final String BACKGROUND_WHITE = "\u001b[47m";

	public static final String HIGHINTENSTY_BLACK = "\u001b[0;90m";
	public static final String HIGHINTENSTY_RED = "\u001b[0;90m";
	public static final String HIGHINTENSTY_GREEN = "\u001b[0;92m";
	public static final String HIGHINTENSTY_YELLOW = "\u001b[0;93m";
	public static final String HIGHINTENSTY_BLUE = "\u001b[0;94m";
	public static final String HIGHINTENSTY_PURPLE = "\u001b[0;95m";
	public static final String HIGHINTENSTY_CYAN = "\u001b[0;96m";
	public static final String HIGHINTENSTY_WHITE = "\u001b[0;97m";

	public static final String BOLD_HIGHINTENSTY_BLACK = "\u001b[1;90m";
	public static final String BOLD_HIGHINTENSTY_RED = "\u001b[1;91m";
	public static final String BOLD_HIGHINTENSTY_GREEN = "\u001b[1;92m";
	public static final String BOLD_HIGHINTENSTY_YELLOW = "\u001b[1;93m";
	public static final String BOLD_HIGHINTENSTY_BLUE = "\u001b[1;94m";
	public static final String BOLD_HIGHINTENSTY_PURPLE = "\u001b[1;95m";
	public static final String BOLD_HIGHINTENSTY_CYAN = "\u001b[1;96m";
	public static final String BOLD_HIGHINTENSTY_WHITE = "\u001b[1;97m";

	public static final String BACKGROUND_HIGHINTENSTY_BLACK = "\u001b[0;100m";
	public static final String BACKGROUND_HIGHINTENSTY_RED = "\u001b[0;101m";
	public static final String BACKGROUND_HIGHINTENSTY_GREEN = "\u001b[0;102m";
	public static final String BACKGROUND_HIGHINTENSTY_YELLOW = "\u001b[0;103m";
	public static final String BACKGROUND_HIGHINTENSTY_BLUE = "\u001b[0;104m";
	public static final String BACKGROUND_HIGHINTENSTY_PURPLE = "\u001b[0;105m";
	public static final String BACKGROUND_HIGHINTENSTY_CYAN = "\u001b[0;106m";
	public static final String BACKGROUND_HIGHINTENSTY_WHITE = "\u001b[0;107m";

	public static final String RESET = "\u001b[0m";

	public static void println() {
		AnsiConsole.out().println();
	}

	public static void println(String s) {
		if (s == null) println();
		else AnsiConsole.out().println(s);
	}

	public static void print(String s) {
		AnsiConsole.out().print(s);
	}

}
