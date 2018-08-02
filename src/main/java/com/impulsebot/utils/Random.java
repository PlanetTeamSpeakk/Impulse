package com.impulsebot.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Actually random, unlike ThreadLocalRandom, smh. Oracle, pls.
 *
 * @author PlanetTeamSpeak
 */
public class Random {

	public static final Random			INSTANCE		= new Random();
	private static final Character[]	characters		= {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	private boolean						shouldResetSeed	= true;
	private java.util.Random			random			= null;

	public Random() {
		random = new java.util.Random();
	}

	public Random(long seed) {
		random = new java.util.Random(seed);
		shouldResetSeed = false;
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
		if (shouldResetSeed()) random = new java.util.Random();
		return (random.nextDouble() * max + min) * (min < 0D ? (int) (random.nextDouble() * 10) >= 5 ? 1 : -1 : 1);
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
	 *
	 * @param key
	 */
	public void seed(String key) {
		random = new java.util.Random(Main.isLong(key) ? Long.parseLong(key) : key.hashCode());
		shouldResetSeed(false);
	}

	public boolean shouldResetSeed() {
		return shouldResetSeed;
	}

	public void shouldResetSeed(boolean bool) {
		shouldResetSeed = bool;
	}

}
