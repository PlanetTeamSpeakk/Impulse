package com.impulsebot.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.util.Base64;

/**
 * Encrypts and decrypts strings.<br>
 * The name of this class comes from the ancient Greek word κρυπτω which means
 * 'I hide' and is pronounced as either krypto or krupto.
 * 
 * @author PlanetTeamSpeak
 */
public class Krypto {

	private Krypto() {
	}

	public static String encrypt(String key, String value) throws KryptoException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"), new IvParameterSpec(key.getBytes("UTF-8")));
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.encodeBase64String(encrypted);
		} catch (Exception e) {
			throw new KryptoException(e);
		}
	}

	public static String decrypt(String key, String encrypted) throws KryptoException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"), new IvParameterSpec(key.getBytes("UTF-8")));
			byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
			return new String(original);
		} catch (Exception e) {
			throw new KryptoException(e);
		}
	}

	public static final class KryptoException extends RuntimeException {
		private static final long serialVersionUID = -6226775520760901357L;

		public KryptoException() {
			super();
		}

		public KryptoException(String message) {
			super(message);
		}

		public KryptoException(Throwable cause) {
			super(cause /* cause comes from the Latin word causa, btw, which also means cause. */);
		}

		public KryptoException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
