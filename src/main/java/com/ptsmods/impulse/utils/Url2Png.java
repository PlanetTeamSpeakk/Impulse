package com.ptsmods.impulse.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.net.util.Base64;

import com.google.gson.Gson;

public class Url2Png {
	private Url2Png() {
	}

	public static File capture(String url) throws IOException {
		String name = "";
		if (url.startsWith("http://"))
			name = url.substring(7);
		else if (url.startsWith("https://"))
			name = url.substring(8);
		else name = new String(url);
		name = name.replaceAll("\\/", "-");
		Main.createDirectoryIfNotExisting("data/url2png/");
		File imgFile = new File("data/url2png/" + name + ".png");
		ImageIO.write((BufferedImage) captureToImage(url), "png", imgFile);
		return imgFile;
	}

	public static Image captureToImage(String url) throws IOException {
		Map data = new Gson().fromJson(Main.getHTML("https://api.letsvalidate.com/v1/thumbs/?output=json&full=true&width=1920&height=1080&format=png&url=" + url), Map.class);
		byte[] bytes = Base64.decodeBase64(data.get("base64").toString());
		return ImageIO.read(new ByteArrayInputStream(bytes));
	}

}
