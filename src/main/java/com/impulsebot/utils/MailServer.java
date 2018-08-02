package com.impulsebot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class MailServer {

	public MailServer() {
	}

	public static boolean createMailAddress(String name, String password, boolean changePassIfExists) throws IOException {
		if (isEnabled()) {
			StringBuilder result = new StringBuilder();
			URL URL = new URL(Config.INSTANCE.get("miabBaseUrl") + "/admin/mail/users/add");
			HttpURLConnection connection = getConnection(URL);
			OutputStream writer = connection.getOutputStream();
			writer.write(String.format("email=%s@%s&password=%s", name, Config.INSTANCE.get("mailBaseUrl"), password).getBytes("UTF-8"));
			writer.flush();
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (IOException e) {
				if (changePassIfExists) {
					// account already exists, changing its password.
					URL = new URL(Config.INSTANCE.get("miabBaseUrl") + "/admin/mail/users/password");
					connection = getConnection(URL);
					writer = connection.getOutputStream();
					writer.write(String.format("email=%s@%s&password=%s", name, Config.INSTANCE.get("mailBaseUrl"), password).getBytes("UTF-8"));
					writer.flush();
					rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				} else throw e;
			}
			String line;
			while ((line = rd.readLine()) != null)
				result.append(line);
			rd.close();
			writer.close();
			return true;
		} else return false;
	}

	// @formatter:off
	public static boolean isEnabled() {
		return  Config.INSTANCE.get("miabBaseUrl") 				!= null && !Config.INSTANCE.get("miabBaseUrl").isEmpty() 			&&
				Config.INSTANCE.get("mailBaseUrl") 				!= null && !Config.INSTANCE.get("mailBaseUrl").isEmpty() 			&&
				Config.INSTANCE.get("miabUsername") 			!= null && !Config.INSTANCE.get("miabUsername").isEmpty() 			&&
				Config.INSTANCE.get("miabPassword") 			!= null && !Config.INSTANCE.get("miabPassword").isEmpty() 			&&
				Config.INSTANCE.get("miabIMAP") 				!= null && !Config.INSTANCE.get("miabIMAP").isEmpty() 				&&
				Config.INSTANCE.get("miabServer") 				!= null && !Config.INSTANCE.get("miabServer").isEmpty() 			&&
				Config.INSTANCE.get("miabIncomingPort") 		!= null && !Config.INSTANCE.get("miabIncomingPort").isEmpty() 		&&
				Config.INSTANCE.get("miabOutgoingPort") 		!= null && !Config.INSTANCE.get("miabOutgoingPort").isEmpty() 		&&
				Config.INSTANCE.get("miabIncomingSecurity") 	!= null && !Config.INSTANCE.get("miabOutgoingSecurity").isEmpty()	&&
				Config.INSTANCE.get("miabSmtpAlwaysVerify") 	!= null && !Config.INSTANCE.get("miabSmtpAlwaysVerify").isEmpty();
	}
	// @formatter:on

	private static final HttpURLConnection getConnection(URL URL) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Username", Config.INSTANCE.get("miabUsername"));
		connection.setRequestProperty("Password", Config.INSTANCE.get("miabPassword"));
		connection.setRequestProperty("Authorization", "Basic " + new String(new Base64().encode((Config.INSTANCE.get("miabUsername") + ":" + Config.INSTANCE.get("miabPassword")).getBytes())));
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		connection.setDoOutput(true);
		return connection;
	}

}
