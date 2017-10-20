package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;

public class DataIO {

	private DataIO() { }

	public static void saveJson(Object obj, String path) throws IOException {
		if (path == null || path.isEmpty()) throw new IOException("The path cannot be empty.");
		if (obj == null) throw new NullPointerException("The object cannot be null.");
		if (!(obj instanceof Map) && !(obj instanceof List) && !(obj instanceof Set)) Main.print(LogType.WARN, "The given object was not a Map, a List or a Set, this might throw an unexpected StackOverflowError.");
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		String tmpPath = "data/tmp/" + Random.randDouble(0, 10000, false) + ".tmp";
		directories = tmpPath.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(tmpPath);
		try {
			writer.write(gson.toJson(gson.toJsonTree(obj)));
		} finally {
			IOUtils.closeQuietly(writer);
		}
		try {
			loadJson(tmpPath, obj.getClass());
		} catch (Throwable e) {
			throw new RuntimeException("The saved data file was corrupt and could not be read, the actual file has not been changed.", e);
		} finally {
			new File(tmpPath).delete();
		}
		FileWriter writer1 = new FileWriter(path);
		try {
			writer1.write(gson.toJson(gson.toJsonTree(obj)));
		} finally {
			IOUtils.closeQuietly(writer1);
		}
	}


	/**
	 *
	 * @param path The path to the JSON file.
	 * @param clazz The class which should be returned.
	 * @return Null if the file is empty, an instance of the given class otherwise.
	 * @throws IOException
	 */
	@Nullable
	public static <T> T loadJson(String path, Class<T> clazz) throws IOException {
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length-1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		if (Files.readAllLines(new File(path).toPath()).isEmpty())
			return null;
		return new Gson().fromJson(Main.join(Files.readAllLines(new File(path).toPath()).toArray(new String[0])), clazz);
	}

}
