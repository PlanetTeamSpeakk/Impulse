package com.ptsmods.impulse.utils;

import java.awt.Color;

import org.fusesource.jansi.AnsiConsole;

public enum ConsoleColours {

	BLACK("\u001b[0;30m"),
	RED("\u001b[0;31m"),
	GREEN("\u001b[0;32m"),
	YELLOW("\u001b[0;33m"),
	BLUE("\u001b[0;34m"),
	PURPLE("\u001b[0;35m"),
	CYAN("\u001b[0;36m"),
	WHITE("\u001b[0;37m"),

	BOLD_BLACK("\u001b[1;30m"),
	BOLD_RED("\u001b[1;31m"),
	BOLD_GREEN("\u001b[1;32m"),
	BOLD_YELLOW("\u001b[1;33m"),
	BOLD_BLUE("\u001b[1;34m"),
	BOLD_PURPLE("\u001b[1;35m"),
	BOLD_CYAN("\u001b[1;36m"),
	BOLD_WHITE("\u001b[1;37m"),

	UNDERLINED_BLACK("\u001b[4;30m"),
	UNDERLINED_RED("\u001b[4;31m"),
	UNDERLINED_GREEN("\u001b[4;32m"),
	UNDERLINED_YELLOW("\u001b[4;33m"),
	UNDERLINED_BLUE("\u001b[4;34m"),
	UNDERLINED_PURPLE("\u001b[4;35m"),
	UNDERLINED_CYAN("\u001b[4;36m"),
	UNDERLINED_WHITE("\u001b[4;37m"),

	BACKGROUND_BLACK("\u001b[40m"),
	BACKGROUND_RED("\u001b[41m"),
	BACKGROUND_GREEN("\u001b[42m"),
	BACKGROUND_YELLOW("\u001b[43m"),
	BACKGROUND_BLUE("\u001b[44m"),
	BACKGROUND_PURPLE("\u001b[45m"),
	BACKGROUND_CYAN("\u001b[46m"),
	BACKGROUND_WHITE("\u001b[47m"),

	HIGHINTENSITY_BLACK("\u001b[0;90m"),
	HIGHINTENSITY_RED("\u001b[0;90m"),
	HIGHINTENSITY_GREEN("\u001b[0;92m"),
	HIGHINTENSITY_YELLOW("\u001b[0;93m"),
	HIGHINTENSITY_BLUE("\u001b[0;94m"),
	HIGHINTENSITY_PURPLE("\u001b[0;95m"),
	HIGHINTENSITY_CYAN("\u001b[0;96m"),
	HIGHINTENSITY_WHITE("\u001b[0;97m"),

	BOLD_HIGHINTENSITY_BLACK("\u001b[1;90m"),
	BOLD_HIGHINTENSITY_RED("\u001b[1;91m"),
	BOLD_HIGHINTENSITY_GREEN("\u001b[1;92m"),
	BOLD_HIGHINTENSITY_YELLOW("\u001b[1;93m"),
	BOLD_HIGHINTENSITY_BLUE("\u001b[1;94m"),
	BOLD_HIGHINTENSITY_PURPLE("\u001b[1;95m"),
	BOLD_HIGHINTENSITY_CYAN("\u001b[1;96m"),
	BOLD_HIGHINTENSITY_WHITE("\u001b[1;97m"),

	BACKGROUND_HIGHINTENSITY_BLACK("\u001b[0;100m"),
	BACKGROUND_HIGHINTENSITY_RED("\u001b[0;101m"),
	BACKGROUND_HIGHINTENSITY_GREEN("\u001b[0;102m"),
	BACKGROUND_HIGHINTENSITY_YELLOW("\u001b[0;103m"),
	BACKGROUND_HIGHINTENSITY_BLUE("\u001b[0;104m"),
	BACKGROUND_HIGHINTENSITY_PURPLE("\u001b[0;105m"),
	BACKGROUND_HIGHINTENSITY_CYAN("\u001b[0;106m"),
	BACKGROUND_HIGHINTENSITY_WHITE("\u001b[0;107m"),

	RESET("\u001b[0m"),
	UNKNOWN("");

	private final String ansiColourCode;
	private final String name;
	private final Color colour;

	private ConsoleColours(String ansiColourCode) {
		this.ansiColourCode = ansiColourCode;
		String name = "";
		String id = ansiColourCode.isEmpty() ? "" : ansiColourCode.substring(2).split(";")[0];
		String code = ansiColourCode.isEmpty() ? "" : ansiColourCode.substring(2).split(";").length > 1 ? ansiColourCode.substring(2).split(";")[1] : "";
		int code1 = ansiColourCode.isEmpty() ? -1 : code.isEmpty() ? Integer.parseInt(id.substring(0, id.length()-1)) : Integer.parseInt(code.substring(0, code.length()-1));
		int id1 = id.isEmpty() ? -2 : Main.isInteger(id) ? Integer.parseInt(id) : -1;
		if (id.equals("0m")) name = "reset";
		else {
			switch (id1) {
			case -1:
				name = "background ";
			case 0:
				if (code1 >= 100) name = "background highintensity ";
				else if (code1 >= 90) name = "highintensity ";
				break;
			case 1:
				if (code1 >= 90) name = "bold highintensity ";
				else if (code1 >= 30) name = "bold ";
				break;
			case 4:
				name = "underlined ";
				break;
			default:
				name = "unknown";
				break;
			}
			switch (code1%10) {
			case 0: {name += "black"; break;}
			case 1: {name += "red"; break;}
			case 2: {name += "green"; break;}
			case 3: {name += "yellow"; break;}
			case 4: {name += "blue"; break;}
			case 5: {name += "purple"; break;}
			case 6: {name += "cyan"; break;}
			case 7: {name += "white"; break;}
			default: break;
			}
		}
		this.name = Main.encase(name);
		if 		(name.contains("highintensity black")) 	colour = Color.getHSBColor(0.000f, 0.000f, 0.502f);
		else if (name.contains("highintensity red")) 	colour = Color.getHSBColor(0.000f, 1.000f, 1.000f);
		else if (name.contains("highintensity blue")) 	colour = Color.getHSBColor(0.667f, 1.000f, 1.000f);
		else if (name.contains("highintensity purple")) colour = Color.getHSBColor(0.833f, 1.000f, 1.000f);
		else if (name.contains("highintensity green")) 	colour = Color.getHSBColor(0.333f, 1.000f, 1.000f);
		else if (name.contains("highintensity yellow")) colour = Color.getHSBColor(0.167f, 1.000f, 1.000f);
		else if (name.contains("highintensity cyan")) 	colour = Color.getHSBColor(0.500f, 1.000f, 1.000f);
		else if (name.contains("highintensity white")) 	colour = Color.getHSBColor(0.000f, 0.000f, 1.000f);
		else if (name.contains("reset"))			 	colour = Color.getHSBColor(0.000f, 0.000f, 1.000f);
		else if	(name.contains("black")) 				colour = Color.getHSBColor(0.000f, 0.000f, 0.000f);
		else if (name.contains("black")) 				colour = Color.getHSBColor(0.000f, 0.000f, 0.000f);
		else if (name.contains("red")) 					colour = Color.getHSBColor(0.000f, 1.000f, 0.502f);
		else if (name.contains("blue")) 				colour = Color.getHSBColor(0.667f, 1.000f, 0.502f);
		else if (name.contains("purple")) 				colour = Color.getHSBColor(0.833f, 1.000f, 0.502f);
		else if (name.contains("green")) 				colour = Color.getHSBColor(0.333f, 1.000f, 0.502f);
		else if (name.contains("yellow")) 				colour = Color.getHSBColor(0.167f, 1.000f, 0.502f);
		else if (name.contains("cyan")) 				colour = Color.getHSBColor(0.500f, 1.000f, 0.502f);
		else if (name.contains("white")) 				colour = Color.getHSBColor(0.000f, 0.000f, 0.753f);
		else colour = null;
	}

	public String getName() {
		return name;
	}

	public String getAnsiColourCode() {
		return new String(ansiColourCode.toCharArray());
	}

	@Override
	public String toString() {
		return getAnsiColourCode();
	}

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

	public static boolean isANSI(String arg) {
		for (ConsoleColours colour : ConsoleColours.values())
			if (colour.getAnsiColourCode().equals(arg)) return true;
		return false;
	}

	public static String getCleanString(String string) {
		if (string == null) return null;
		for (ConsoleColours colour : ConsoleColours.values())
			string = string.replaceAll(colour.getAnsiColourCode().replaceAll("\\[", "\\\\["), "");
		return string;
	}

	public static ConsoleColours getFirstColour(String string) {
		if (string == null) return null;
		for (ConsoleColours colour : ConsoleColours.values())
			if (!string.equals(string.replaceAll(colour.getAnsiColourCode().replaceAll("\\[", "\\\\["), "")) && colour != UNKNOWN && colour != RESET) return colour;
		return BLACK;
	}

	public Color toColour() {
		return colour;
	}

}
