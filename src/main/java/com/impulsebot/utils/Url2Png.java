package com.impulsebot.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.impulsebot.Main.LogType;

public class Url2Png {
	private Url2Png() {
	}

	static {
		if (!getWkHTMLtoImageBin().exists()) {
			Main.print(LogType.DEBUG, "Downloading tool.");
			String os = Main.isWindows() ? "win" : Main.isUnix() ? "linux" : Main.isMac() ? "mac" : "";
			int arch = System.getProperty("os.arch").contains("64") ? 64 : 32;
			try {
				Downloader.downloadFile("https://rebrand.ly/WkHTMLtoImage_" + os + "-" + arch, getWkHTMLtoImageBin().toString());
			} catch (IOException e) {
				Main.throwCheckedExceptionWithoutDeclaration(e);
			}
		}
	}

	public static File capture(String url) throws IOException {
		String name = Main.percentEncode(url);
		File imgFile = new File("data/url2png/" + name + ".png");
		try {
			Runtime.getRuntime().exec("\"" + getWkHTMLtoImageBin().getAbsolutePath() + "\" --format png --width 1920 --height 1080 " + url + " " + imgFile).waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return imgFile;
	}

	public static Image captureToImage(String url) throws IOException {
		File file = capture(url);
		Image image = ImageIO.read(file);
		file.delete();
		return image;
	}

	public static File getWkHTMLtoImageBin() {
		String os = Main.isWindows() ? "win" : Main.isUnix() ? "linux" : Main.isMac() ? "mac" : "";
		int arch = System.getProperty("os.arch").contains("64") ? 64 : 32;
		return new File("libs/WkHTMLtoImage_" + os + "-" + arch + (Main.isWindows() ? ".exe" : ""));
	}

}
