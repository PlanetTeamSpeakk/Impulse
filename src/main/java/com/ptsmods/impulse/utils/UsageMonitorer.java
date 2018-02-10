package com.ptsmods.impulse.utils;

import java.io.File;
import java.lang.management.ManagementFactory;

import com.ptsmods.impulse.utils.MathHelper.Percentage;
import com.sun.management.OperatingSystemMXBean;

public class UsageMonitorer {

	private UsageMonitorer() {}

	public static int getProcessorCount() {
		return getOSMXB().getAvailableProcessors();
	}

	public static Percentage getSystemCpuLoad() {
		return new Percentage(getOSMXB().getSystemCpuLoad() * 100);
	}

	public static Percentage getAverageSystemCpuLoad() {
		return new Percentage(getOSMXB().getSystemLoadAverage() * 100);
	}

	public static Percentage getProcessCpuLoad() {
		return new Percentage(getOSMXB().getProcessCpuLoad() * 100);
	}

	public static long getRamUsage() {
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
	}

	public static long getRamMax() {
		return Runtime.getRuntime().maxMemory();
	}

	public static long getTotalSpace() {
		long totalSpace = 0;
		for (File root : File.listRoots())
			totalSpace += root.getTotalSpace();
		return totalSpace;
	}

	public static long getFreeSpace() {
		long freeSpace = 0;
		for (File root : File.listRoots())
			freeSpace += root.getFreeSpace();
		return freeSpace;
	}

	public static long getUsedSpace() {
		return getTotalSpace() - getFreeSpace();
	}

	public static long getUsableSpace() {
		long usableSpace = 0;
		for (File root : File.listRoots())
			usableSpace += root.getUsableSpace();
		return usableSpace;
	}

	private static final OperatingSystemMXBean getOSMXB() {
		return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

}
