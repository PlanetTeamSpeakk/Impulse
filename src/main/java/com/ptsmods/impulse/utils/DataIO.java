package com.ptsmods.impulse.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;

public class DataIO {

	private DataIO() {
	}

	// for sets and lists.
	public static void saveJson(Collection collection, String path) throws IOException {
		saveJson(collection, path, true);
	}

	public static void saveJson(Collection collection, String path, boolean encrypt) throws IOException {
		int count = 0;
		Throwable t = new Exception();
		while (t != null && count < 16)
			try {
				saveJson0(new ArrayList<>(collection), path, encrypt);
				t = null;
			} catch (ConcurrentModificationException e) {
				t = e;
				count += 1;
				Main.sleep(50);
			}
		if (t != null) throw new IOException(t);
	}

	// for maps.
	public static void saveJson(Map map, String path) throws IOException {
		saveJson(map, path, true);
	}

	public static void saveJson(Map map, String path, boolean encrypt) throws IOException {
		int count = 0;
		Throwable t = new Exception();
		while (t != null && count < 16)
			try {
				saveJson0(new HashMap<>(map), path, encrypt);
				t = null;
			} catch (ConcurrentModificationException e) {
				t = e;
				count += 1;
				Main.sleep(50);
			}
		if (t != null) throw new IOException(t);
	}

	private static void saveJson0(Object obj, String path, boolean encrypt) throws IOException {
		if (path == null || path.isEmpty()) throw new IOException("The path cannot be empty.");
		if (obj == null) throw new NullPointerException("The object cannot be null.");
		if (!(obj instanceof Map) && !(obj instanceof List) && !(obj instanceof Set)) Main.print(LogType.WARN, "The given object was not a Map, a List or a Set, this might throw an unexpected StackOverflowError.");
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length - 1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		String tmpPath = "data/tmp/" + Random.randDouble(1000, 9999, false) + ".tmp";
		directories = tmpPath.split("/");
		directories = Main.removeArg(directories, directories.length - 1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().setDateFormat("EEEE d MMM y HH:mm:ss").create();
		String string = encrypt ? "iEncrypt" + Krypto.encrypt(Config.get("kryptoKey"), gson.toJson(gson.toJsonTree(obj))) : gson.toJson(gson.toJsonTree(obj));
		FileWriter writer = new FileWriter(tmpPath);
		try {
			writer.write(string);
		} finally {
			IOUtils.closeQuietly(writer);
		}
		try {
			loadJson(tmpPath, obj.getClass(), encrypt);
		} catch (Throwable e) {
			throw new IOException("The saved data file was corrupt and could not be read, the actual file has not been changed.", e);
		} finally {
			new File(tmpPath).delete();
		}
		FileWriter writer1 = new FileWriter(path);
		try {
			writer1.write(string);
		} finally {
			IOUtils.closeQuietly(writer1);
		}
	}

	/**
	 * @param path
	 *            The path to the JSON file.
	 * @param clazz
	 *            The class which should be returned.
	 * @param isEncrypted
	 *            If the JSON file is encrypted using AES and Base64 using the key
	 *            given in the config as kryptoKey.
	 * @return Null if the file is empty, an instance of the given class otherwise.
	 * @throws IOException
	 */
	@Nullable
	public static <T> T loadJson(String path, Class<T> clazz, boolean isEncrypted) throws IOException {
		String[] directories = path.split("/");
		directories = Main.removeArg(directories, directories.length - 1);
		new File(Main.joinCustomChar("/", directories)).mkdirs();
		if (!new File(path).exists()) new File(path).createNewFile();
		if (Files.readAllLines(new File(path).toPath()).isEmpty()) return null;
		String content = Main.join(Files.readAllLines(new File(path).toPath()));
		return new Gson().fromJson(isEncrypted && content.startsWith("iEncrypt") ? Krypto.decrypt(Config.get("kryptoKey"), content.substring(8)) : content, new TypeToken<T>() {
		}.getType());
	}

	@Nullable
	public static <T> T loadJson(String path, Class<T> clazz) throws IOException {
		return loadJson(path, clazz, true);
	}

	public static <T, U extends T> T loadJsonOrDefault(String path, Class<T> clazz, U fallback) throws IOException {
		return loadJson(path, clazz) == null ? fallback : loadJson(path, clazz);
	}

	public static <T> T loadJsonQuietly(String path, Class<T> clazz) {
		try {
			return loadJson(path, clazz);
		} catch (Throwable e) {
			return null;
		}
	}

	public static <T, U extends T> T loadJsonOrDefaultQuietly(String path, Class<T> clazz, U fallback) {
		T value = null;
		try {
			return (value = loadJsonOrDefault(path, clazz, fallback)) == null ? fallback : value;
		} catch (Throwable e) {
			return fallback;
		}
	}

}
