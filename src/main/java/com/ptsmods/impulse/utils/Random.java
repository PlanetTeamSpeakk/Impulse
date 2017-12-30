package com.ptsmods.impulse.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actually random, unlike ThreadLocalRandom, smh. Oracle, pls.
 * @author PlanetTeamSpeak
 */
public class Random {

	private static final Character[] characters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static Map<String, Map<String, Double>> seeds;
	private static boolean shouldSeed = false;
	private static boolean shouldMakeNewSeed = false;
	private static String seedKey = "";
	private static Class callerClass = null;
	private static long lastCalledMillis = 0;

	private Random() { }

	static {
		try {
			seeds = DataIO.loadJsonOrDefault("data/random/seeds.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static int randInt() {
		return randInt(0, Integer.MAX_VALUE);
	}

	public static int randInt(int max) {
		return randInt(0, max);
	}

	public static int randInt(int min, int max) {
		return (int) randDouble(min, max);
	}

	public static long randLong() {
		return randLong(0, Long.MAX_VALUE);
	}

	public static long randLong(long max) {
		return randLong(0, max);
	}

	public static long randLong(long min, long max) {
		return (long) randDouble(min, max);
	}

	public static short randShort() {
		return randShort((short) 0, Short.MAX_VALUE);
	}

	public static short randShort(short max) {
		return randShort((short) 0, max);
	}

	public static short randShort(short min, short max) {
		return (short) randLong(min, max);
	}

	public static double randDouble() {
		return randDouble(0D, Double.MAX_VALUE);
	}

	public static double randDouble(double max) {
		return randDouble(0D, max);
	}

	public static double randDouble(double min, double max) {
		return randDouble(min, max, true);
	}

	public static double randDouble(double min, double max, boolean useSeeding) {
		double rng = (Math.random() * max + min) * (min < 0D ? (int) (Math.random() * 10) >= 5 ? 1 : -1 : 1);
		if (shouldSeed && useSeeding && System.currentTimeMillis()-lastCalledMillis <= 100)
			if (shouldMakeNewSeed) {
				Map callerClassSeeds = seeds.get(callerClass.getName());
				callerClassSeeds = callerClassSeeds == null ? new HashMap<>() : callerClassSeeds;
				callerClassSeeds.put(seedKey, rng);
				seeds.put(callerClass.getName(), callerClassSeeds);
				try {
					DataIO.saveJson(seeds, "data/random/seeds.json");
				} catch (IOException e) {
					throw new RuntimeException("There was an error while saving the seeding file.", e);
				}
			} else
				rng = (Double) ((Map) seeds.get(callerClass.getName())).get(seedKey);
		if (useSeeding) { // just making sure the values are always reset when seeding is turned on.
			shouldSeed = false;
			shouldMakeNewSeed = false;
			seedKey = "";
			callerClass = null;
			lastCalledMillis = 0;
		}
		if (rng > max) rng = max;
		if (rng < min) rng = min;
		return rng;
	}

	public static float randFloat() {
		return randFloat(0F, Float.MAX_VALUE);
	}

	public static float randFloat(float max) {
		return randFloat(0F, max);
	}

	public static float randFloat(float min, float max) {
		return (float) randDouble(min, max);
	}

	public static <T> T choice(T... choices) {
		return choices[randInt(choices.length)];
	}

	public static <T> T choice(List<T> choices) {
		return choices.get(randInt(choices.size()));
	}

	public static <T> void scramble(List<T> list) {
		List<T> listCopy = new ArrayList(list);
		list.clear();
		List<T> passed = new ArrayList();
		for (int x = 0; x < listCopy.size(); x++) {
			T chosenOne = choice(listCopy);
			while (passed.contains(chosenOne)) chosenOne = choice(listCopy);
			list.add(chosenOne);
			passed.add(chosenOne);
		}
	}

	public static String genKey(int length) {
		String key = "";
		for (int i : Main.range(length))
			key += choice(characters);
		return key;
	}

	/**
	 * Seeds a key so the next time a method is called it returns the same value as previous time.
	 * A method of this class which returns a random value should be called within 100 milliseconds to return a seeded value, otherwise it'll return a random value.
	 * @param key
	 */
	@SuppressWarnings("deprecation")
	public static void seed(String key) {
		callerClass = sun.reflect.Reflection.getCallerClass(2); // 0 is java.lang.Thread, 1 is this class and 2 is the actual caller class.
		if (!seeds.getOrDefault(callerClass.getName(), new HashMap<>()).containsKey(key)) {
			shouldMakeNewSeed = true;
			Map callerClassSeeds = seeds.getOrDefault(callerClass.getName(), new HashMap<>());
			callerClassSeeds.put(key, 0D);
			seeds.put(callerClass.getName(), callerClassSeeds);
			try {
				DataIO.saveJson(seeds, "data/random/seeds.json");
			} catch (IOException e) {
				throw new RuntimeException("There was an error while saving the seeding file.", e);
			}
		}
		shouldSeed = true;
		seedKey = key;
		lastCalledMillis = new Long(System.currentTimeMillis());
	}

}
