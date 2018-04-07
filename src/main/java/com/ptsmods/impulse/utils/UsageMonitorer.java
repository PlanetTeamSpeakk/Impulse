package com.ptsmods.impulse.utils;

import java.io.File;
import java.lang.management.ManagementFactory;

import com.ptsmods.impulse.utils.MathHelper.Percentage;
import com.sun.management.OperatingSystemMXBean;

public class UsageMonitorer {

	private UsageMonitorer() {
	}

	public static int getProcessorCount() {
		return getOSMXB().getAvailableProcessors();
	}

	public static Percentage getSystemCpuLoad() {
		try {
			return new Percentage(getOSMXB().getSystemCpuLoad() * 100);
		} catch (Exception e) {
			return new Percentage(0);
		}
	}

	public static Percentage getAverageSystemCpuLoad() {
		try {
			return new Percentage(getOSMXB().getSystemLoadAverage() * 100);
		} catch (Exception e) {
			return new Percentage(0);
		}
	}

	public static Percentage getProcessCpuLoad() {
		try {
			return new Percentage(getOSMXB().getProcessCpuLoad() * 100);
		} catch (Exception e) {
			return new Percentage(0);
		}
	}

	public static long getRamUsage() {
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
	}

	public static long getRamMax() {
		return Runtime.getRuntime().maxMemory();
	}

	public static long getSystemRamUsage() {
		return getSystemRamMax() - getOSMXB().getFreePhysicalMemorySize();
	}

	public static long getSystemRamMax() {
		return getOSMXB().getTotalPhysicalMemorySize();
	}

	public static long getSystemSwapUsage() {
		return getSystemSwapMax() - getOSMXB().getFreeSwapSpaceSize();
	}

	public static long getSystemSwapMax() {
		return getOSMXB().getTotalSwapSpaceSize();
	}

	public static long getTotalSystemRamUsage() {
		return getSystemRamUsage() + getSystemSwapUsage();
	}

	public static long getTotalSystemRamMax() {
		return getSystemRamMax() + getSystemSwapMax();
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

	public static long getCurrentLoadedClassCount() {
		return VMManagement.getVMM().getLoadedClassCount();
	}

	public static long getTotalLoadedClassCount() {
		return VMManagement.getVMM().getTotalClassCount();
	}

	private static final OperatingSystemMXBean getOSMXB() {
		return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

}
