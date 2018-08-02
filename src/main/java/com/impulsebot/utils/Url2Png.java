package com.impulsebot.utils;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Url2Png {
	private Url2Png() {
	}

	public static File capture(String url) throws IOException {
		String name = Main.percentEncode(url);
		File imgFile = new File("data/url2png/" + name + ".png");
		return Downloader.downloadFile(getApiUrl(url), imgFile.getPath()).getFile();
	}

	public static Image captureToImage(String url) throws IOException {
		return ImageIO.read(new URL(url));
	}

	private static String getApiUrl(String url) {
		return "http://capturethat.website/api.php?url=" + url + "&width=1920"; // uwu my own api.
	}

}
