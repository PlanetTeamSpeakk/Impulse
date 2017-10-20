package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

public class Config {

	public static final File configFile = new File("config.cfg");
	private static Map<String, String> keys = new HashMap<>();

	private Config() { }

	static {
		try {
			if (!configFile.exists()) configFile.createNewFile();
			for (String line : Files.readAllLines(configFile.toPath())) {
				if (line.equals("") || line.split("//")[0].isEmpty()) continue;
				if (line.split("=").length > 0) {
					String value = line.substring(line.indexOf('=') + 1, line.length());
					String key = line.substring(0, line.indexOf('='));
					keys.put(key, value);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException("An error occured while reading the config file.");
		}
	}

	@Nullable
	public static String getValue(String key) {
		return keys.get(key);
	}

	public static void addValuePair(String key, String value) {
		if (!keys.containsKey(key)) {
			addLine(key + "=" + value);
			keys.put(key, value);
		}
	}

	public static void addComment(String comment) {
		addLine("// " + comment);
	}

	private static void addLine(String line) {
		if (new File("config.cfg").isDirectory()) new File("config.cfg").delete();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("config.cfg", true));
		} catch (Throwable e) {
			throw new RuntimeException("Could not open config file.");
		}
		try {
			writer.println(line);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

}
