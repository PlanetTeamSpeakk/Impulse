package com.ptsmods.impulse.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Actually random, unlike ThreadLocalRandom, smh. Oracle, pls.
 *
 * @author PlanetTeamSpeak
 */
public class Random {

	public static final Random						INSTANCE			= new Random();
	private final Character[]						characters			= {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private static Map<String, Map<String, Double>>	seeds;
	private boolean									shouldSeed			= false;
	private boolean									shouldMakeNewSeed	= false;
	private String									seedKey				= "";
	private Class									callerClass			= null;
	private long									lastCalledMillis	= 0;

	public Random() {
	}

	static {
		try {
			seeds = DataIO.loadJsonOrDefault("data/random/seeds.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int randInt() {
		return randInt(0, Integer.MAX_VALUE);
	}

	public int randInt(int max) {
		return randInt(0, max);
	}

	public int randInt(int min, int max) {
		return (int) randDouble(min, max);
	}

	public long randLong() {
		return randLong(0, Long.MAX_VALUE);
	}

	public long randLong(long max) {
		return randLong(0, max);
	}

	public long randLong(long min, long max) {
		return (long) randDouble(min, max);
	}

	public short randShort() {
		return randShort((short) 0, Short.MAX_VALUE);
	}

	public short randShort(short max) {
		return randShort((short) 0, max);
	}

	public short randShort(short min, short max) {
		return (short) randLong(min, max);
	}

	public double randDouble() {
		return randDouble(0D, Double.MAX_VALUE);
	}

	public double randDouble(double max) {
		return randDouble(0D, max);
	}

	public double randDouble(double min, double max) {
		return randDouble(min, max, true);
	}

	public double randDouble(double min, double max, boolean useSeeding) {
		double rng = (Math.random() * max + min) * (min < 0D ? (int) (Math.random() * 10) >= 5 ? 1 : -1 : 1);
		if (shouldSeed && useSeeding && System.currentTimeMillis() - lastCalledMillis <= 100) if (shouldMakeNewSeed) {
			Map callerClassSeeds = seeds.get(callerClass.getName());
			callerClassSeeds = callerClassSeeds == null ? new HashMap<>() : callerClassSeeds;
			callerClassSeeds.put(seedKey, rng);
			seeds.put(callerClass.getName(), callerClassSeeds);
			try {
				DataIO.saveJson(seeds, "data/random/seeds.json");
			} catch (IOException e) {
				throw new RuntimeException("There was an error while saving the seeding file.", e);
			}
		} else rng = (Double) ((Map) seeds.get(callerClass.getName())).get(seedKey);
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

	public float randFloat() {
		return randFloat(0F, Float.MAX_VALUE);
	}

	public float randFloat(float max) {
		return randFloat(0F, max);
	}

	public float randFloat(float min, float max) {
		return (float) randDouble(min, max);
	}

	public <T> T choice(T... choices) {
		return choices[randInt(choices.length)];
	}

	public <T> T choice(List<T> choices) {
		return choices.get(randInt(choices.size()));
	}

	public <T> void scramble(List<T> list) {
		List<T> listCopy = new ArrayList(list);
		list.clear();
		List<T> passed = new ArrayList();
		for (int x = 0; x < listCopy.size(); x++) {
			T chosenOne = choice(listCopy);
			while (passed.contains(chosenOne))
				chosenOne = choice(listCopy);
			list.add(chosenOne);
			passed.add(chosenOne);
		}
	}

	public String genKey(int length) {
		return genKey(length, true);
	}

	public String genKey(int length, boolean alphanumeric) {
		String key = "";
		for (int i : Main.range(length))
			key += alphanumeric ? choice(characters) : Main.fromUnicode(genKey(4, true));
		return key;
	}

	/**
	 * Seeds a key so the next time a method is called it returns the same value as
	 * previous time.
	 * A method of this class which returns a random value should be called within
	 * 100 milliseconds to return a seeded value, otherwise it'll return a random
	 * value.
	 *
	 * @param key
	 */
	public void seed(String key) {
		callerClass = Main.getCallerClass();
		if (!seeds.getOrDefault(callerClass.getName(), new HashMap<>()).containsKey(key) || seeds.getOrDefault(callerClass.getName(), new HashMap<>()).get(key) == null) {
			shouldMakeNewSeed = true;
			Map callerClassSeeds = seeds.getOrDefault(callerClass.getName(), new HashMap<>());
			callerClassSeeds.put(key, 0D);
			seeds.put(callerClass.getName(), callerClassSeeds);
			try {
				DataIO.saveJson(seeds, "data/random/seeds.json");
			} catch (IOException e) {
				Main.throwCheckedExceptionWithoutDeclaration(new IOException("There was an error while saving the seeding file.", e));
			}
		}
		shouldSeed = true;
		seedKey = key;
		lastCalledMillis = System.currentTimeMillis();
	}

}
