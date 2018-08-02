package com.impulsebot.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

public class Config implements Serializable {

	private static final long		serialVersionUID	= 8033140627248984300L;
	public static Config			INSTANCE			= new Config();
	public final File				configFile			= new File("config.cfg");
	private Map<String, String>		keys				= new HashMap<>();
	private Map<Integer, String>	lines				= new HashMap();

	private Config() {
		try {
			if (!configFile.exists()) configFile.createNewFile();
			for (String line : Files.readAllLines(configFile.toPath())) {
				lines.put(lines.size() + 1, line);
				if (line.equals("") || line.split("//")[0].isEmpty()) continue;
				if (line.split("=").length > 0) {
					String value = line.substring(line.indexOf('=') + 1, line.length());
					String key = line.substring(0, line.indexOf('='));
					keys.put(key, value);
				}
			}
		} catch (Throwable e) {
			configFile.delete(); // most likely due to corrupt file.
			throw new RuntimeException("An error occured while reading the config file.", e);
		}
	}

	public List<String> getKeys() {
		return new ArrayList(keys.keySet());
	}

	public List<String> getValues() {
		return new ArrayList(keys.values());
	}

	public String getOrDefault(String key, String fallback) {
		return get(key) == null ? fallback : get(key);
	}

	@Nullable
	public String get(String key) {
		return keys.get(key);
	}

	public void put(String key, String value) {
		if (!keys.containsKey(key)) {
			lines.put(lines.size() + 1, key + "=" + value);
			write();
			keys.put(key, value);
		} else for (Integer i : lines.keySet())
			if (lines.get(i).equals(key + "=" + keys.get(key))) {
				lines.put(i, key + "=" + value);
				write();
				keys.put(key, value);
			}
	}

	public void addComment(String comment) {
		addComment(lines.size() + 1, comment);
	}

	private void addComment(int line, String comment) {
		if (line > lines.size() + 1) throw new StringIndexOutOfBoundsException("The given integer line was bigger than it's current maximum (" + (lines.size() + 1) + ").");
		for (String part : comment.split("\n"))
			lines.put(lines.size() + 1, "// " + part);
		write();
	}

	/**
	 * Writes all the lines from the private lines field to the config.cfg
	 * file.
	 */
	private final void write() {
		if (lines == null) throw new NullPointerException("The given argument 'lines' cannot be null.");
		if (new File("config.cfg").isDirectory()) new File("config.cfg").delete();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter("config.cfg", false));
		} catch (Throwable e) {
			throw new RuntimeException("Could not open config file.");
		}
		try {
			for (int i : Main.range(lines.size()))
				writer.println(lines.get(i + 1));
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public static class ConfigException extends RuntimeException {

		private static final long serialVersionUID = 6886096769970779989L;

		public ConfigException() {
			super();
		}

		public ConfigException(String message) {
			super(message);
		}

		public ConfigException(Throwable cause) {
			super(cause);
		}

		public ConfigException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
