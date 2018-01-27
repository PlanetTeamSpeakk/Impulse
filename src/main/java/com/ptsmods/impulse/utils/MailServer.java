package com.ptsmods.impulse.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class MailServer {

	public MailServer() {}

	public static boolean createMailAddress(String name, String password) throws IOException {
		if (isEnabled()) {
			StringBuilder result = new StringBuilder();
			URL URL = new URL(Config.get("miabBaseUrl") + "/admin/mail/users/add");
			HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Username", Config.get("miabUsername"));
			connection.setRequestProperty("Password", Config.get("miabPassword"));
			String userpass = Config.get("miabUsername") + ":" + Config.get("miabPassword");
			String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
			connection.setRequestProperty("Authorization", basicAuth);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
			connection.setDoOutput(true);
			OutputStream writer = connection.getOutputStream();
			writer.write(String.format("email=%s@%s&password=%s", name, Config.get("mailBaseUrl"), password).getBytes("UTF-8"));
			writer.flush();
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null)
				result.append(line);
			rd.close();
			writer.close();
			return true;
		} else return false;
	}

	public static boolean isEnabled() {
		return  Config.get("miabBaseUrl") != null && !Config.get("miabBaseUrl").isEmpty() &&
				Config.get("mailBaseUrl") != null && !Config.get("mailBaseUrl").isEmpty() &&
				Config.get("miabUsername") != null && !Config.get("miabUsername").isEmpty() &&
				Config.get("miabPassword") != null && !Config.get("miabPassword").isEmpty() &&
				Config.get("miabIMAP") != null && !Config.get("miabIMAP").isEmpty() &&
				Config.get("miabServer") != null && !Config.get("miabServer").isEmpty() &&
				Config.get("miabIncomingPort") != null && !Config.get("miabIncomingPort").isEmpty() &&
				Config.get("miabOutgoingPort") != null && !Config.get("miabOutgoingPort").isEmpty() &&
				Config.get("miabIncomingSecurity") != null && !Config.get("miabOutgoingSecurity").isEmpty() &&
				Config.get("miabSmtpAlwaysVerify") != null && !Config.get("miabSmtpAlwaysVerify").isEmpty();
	}

}