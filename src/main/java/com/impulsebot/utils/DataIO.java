package com.impulsebot.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.impulsebot.Main;
import com.impulsebot.Main.LogType;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Insert;
import com.rethinkdb.gen.exc.ReqlOpFailedError;
import com.rethinkdb.model.MapObject;

import sun.reflect.Reflection;

public class DataIO {

	private static final RethinkDB rdb = RethinkDB.r;

	private DataIO() {
	}

	static {
		Main.getRDBConnection().use(Config.INSTANCE.get("database"));
	}

	public static void saveJson(Collection collection, String path) throws IOException {
		int count = 0;
		Throwable t = new Exception();
		while (t != null && count < 16)
			try {
				saveJson0(new ArrayList<>(collection), path);
				t = null;
			} catch (ConcurrentModificationException e) {
				t = e;
				count += 1;
				Main.sleep(50);
			}
		if (t != null) throw new IOException(t);
	}

	public static void saveJson(Map map, String path) throws IOException {
		int count = 0;
		Throwable t = new Exception();
		while (t != null && count < 16)
			try {
				saveJson0(new HashMap<>(map), path);
				t = null;
			} catch (ConcurrentModificationException e) {
				t = e;
				count += 1;
				Main.sleep(50);
			}
		if (t != null) throw new IOException(t);
	}

	@SuppressWarnings("deprecation")
	private static void saveJson0(Object obj, String path) throws IOException {
		if (path == null || path.isEmpty()) throw new IOException("The path cannot be empty.");
		if (obj == null) throw new NullPointerException("The object cannot be null.");
		if (!(obj instanceof Map) && !(obj instanceof List) && !(obj instanceof Set)) Main.print(LogType.WARN, "The given object was not a Map, a List or a Set, this might throw an unexpected StackOverflowError.");
		String className = "";
		for (int i : Main.range(999))
			if (!Reflection.getCallerClass(i).getSimpleName().equals(DataIO.class.getSimpleName()) && i > 1) {
				className = Reflection.getCallerClass(i).getSimpleName();
				break;
			}
		String tableName = Config.INSTANCE.getOrDefault("tablePrefix", "ip_") + className + "_" + path.split("/")[path.split("/").length - 1].split("\\.")[0];
		if (!rdb.tableList().contains(tableName).<Boolean>run(Main.getRDBConnection())) rdb.tableCreate(tableName).run(Main.getRDBConnection());
		try {
			rdb.table(tableName).get(0).delete().run(Main.getRDBConnection());
		} catch (ReqlOpFailedError e) {
		}
		Insert job = null;
		if (obj instanceof Map)
			job = rdb.table(tableName).insert(mapToMapObject((Map) obj));
		else if (obj instanceof List || obj instanceof Set)
			job = rdb.table(tableName).insert(listOrSetToMapObject(obj));
		else throw new IllegalArgumentException("DataIO can only save and load Maps, Lists and Sets, the given object, instance of " + obj.getClass().getName() + ", is none of either.");
		try {
			job.run(Main.getRDBConnection());
		} catch (ReqlOpFailedError e) {
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
	@SuppressWarnings("deprecation")
	public static <T> T loadJson(String path, Class<T> clazz) {
		String className = "";
		for (int i : Main.range(999))
			if (!Reflection.getCallerClass(i).getSimpleName().equals(DataIO.class.getSimpleName()) && i > 1) {
				className = Reflection.getCallerClass(i).getSimpleName();
				break;
			}
		String tableName = className + "_" + path.split("/")[path.split("/").length - 1].split("\\.")[0];
		if (!rdb.tableList().contains(tableName).<Boolean>run(Main.getRDBConnection())) return null;
		return (T) ((Map) rdb.table(tableName).get(0).run(Main.getRDBConnection())).get("content");
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

	private static MapObject mapToMapObject(Map map) {
		MapObject mapObj = rdb.hashMap("id", 0);
		MapObject mapObj2 = rdb.hashMap();
		for (Entry entry : (Set<Entry>) map.entrySet())
			mapObj2.with(entry.getKey(), entry.getValue());
		mapObj.with("content", mapObj2);
		return mapObj;
	}

	private static MapObject listOrSetToMapObject(Object obj) {
		List list = null;
		if (obj instanceof List)
			list = (List) obj;
		else if (obj instanceof Set) list = new ArrayList((Set) obj);
		return rdb.hashMap("id", 0).with("content", list);
	}

}
