package com.ptsmods.impulse.utils;

/**
 * Actually random, unlike ThreadLocalRandom, smh. Oracle, pls.
 * @author PlanetTeamSpeak
 */
public class Random {

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
		double rng = (int) (Math.random() * max + min) * (min < 0D ? (int) (Math.random() * 10) >= 5 ? 1 : -1 : 1);
		while (rng < min) rng += 1D;
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

}
