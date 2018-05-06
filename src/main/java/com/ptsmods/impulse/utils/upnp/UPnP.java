package com.ptsmods.impulse.utils.upnp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.miscellaneous.Main;
import com.ptsmods.impulse.utils.ArraySet;
import com.ptsmods.impulse.utils.Downloader;

public class UPnP {

	private UPnP() {
	}

	public static void portForward(UPnPProtocol protocol, int port, String description) throws IOException {
		getPortmapper();
		String cmd = "java -jar libs/portmapper.jar -add -internalPort " + port + " -externalPort " + port + " -description \"" + description + "\" ";
		if (Main.isWindows()) cmd = "cmd /c " + cmd;
		switch (protocol) {
		case TCP:
			Runtime.getRuntime().exec(cmd + "-protocol TCP");
			break;
		case UDP:
			Runtime.getRuntime().exec(cmd + "-protocol UDP");
			break;
		case BOTH:
			Runtime.getRuntime().exec(cmd + "-protocol TCP");
			Runtime.getRuntime().exec(cmd + "-protocol UDP");
			break;
		}
		Main.print(LogType.INFO, "Successfully portforwarded port", port + ".");
	}

	public static boolean checkPortmapper() {
		return new File("libs/portmapper.jar").exists();
	}

	public static void getPortmapper() {
		if (!checkPortmapper()) try {
			Main.print(LogType.DEBUG, "Could not find portmapper, downloading it now...");
			if (!Downloader.downloadFile("https://kent.dl.sourceforge.net/project/upnp-portmapper/v2.1.1/portmapper-2.1.1.jar", "libs/portmapper.jar").succeeded()) throw new IOException("Could not download portmapper.jar.");
		} catch (IOException e) {
			Main.throwCheckedExceptionWithoutDeclaration(e);
		}
	}

	public static List<UPnPEntry> getUPnPEntries() throws IOException {
		getPortmapper();
		List<UPnPEntry> entries = new ArraySet();
		Process process = Runtime.getRuntime().exec((Main.isWindows() ? "cmd /c " : " ") + "java -jar libs/portmapper.jar -list");
		Main.sleep(7500);
		List<String> lines = new ArrayList();
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = input.readLine()) != null)
			lines.add(line);
		for (String line0 : lines)
			if (line0.startsWith("TCP") || line0.startsWith("UDP")) entries.add(new UPnPEntry(line0.startsWith("TCP") ? UPnPProtocol.TCP : UPnPProtocol.UDP, Integer.parseInt(line0.substring(5).split(" ")[0]), Main.join(Main.removeArgs(line0.split(" "), 0, 0, 0, 0, 0)), InetAddress.getByName(line0.split(" ")[3].split(":")[0]), line0.split(" ")[4].equals("enabled")));
		return entries;
	}

}
