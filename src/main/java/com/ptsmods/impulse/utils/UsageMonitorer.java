package com.ptsmods.impulse.utils;

public class UsageMonitorer {

	private UsageMonitorer() {}
	public static long getRamUsage() {
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
	}

	public static long getRamMax() {
		return Runtime.getRuntime().maxMemory();
	}

}
