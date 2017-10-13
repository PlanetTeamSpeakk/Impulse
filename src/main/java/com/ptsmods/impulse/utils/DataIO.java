package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ptsmods.impulse.Main;

public class DataIO {

	private DataIO() { }

	public static void saveJson(Object settings, String path) throws IOException {
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		String tmpPath = "data/tmp/" + Random.randInt(10000) + ".tmp";
		directories = tmpPath.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(tmpPath);
		try {
			writer.write(gson.toJson(gson.toJsonTree(settings)));
		} finally {
			IOUtils.closeQuietly(writer);
		}
		try {
			loadJson(tmpPath, settings.getClass());
		} catch (Throwable e) {
			throw new RuntimeException("The saved data file was corrupt and could not be read, the actual file has not been changed.", e);
		} finally {
			new File(tmpPath).delete();
		}
		FileWriter writer1 = new FileWriter(path);
		try {
			writer1.write(gson.toJson(gson.toJsonTree(settings)));
		} finally {
			IOUtils.closeQuietly(writer1);
		}
	}


	public static <T> T loadJson(String path, Class<T> clazz) throws IOException {
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		return new Gson().fromJson(Main.join(Files.readAllLines(new File(path).toPath()).toArray(new String[0])), clazz);
	}

}
