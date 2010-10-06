/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.synchrony.security;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.Key;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class SecuritySample {

    /**
     * @param args the command line arguments
     */


    private static final byte[] SHARED_KEY = hexstringToByteArray("BAC464B197083EE626273DC4C9EBD8AE82E0897E3D8388EE06CB3EF7BCFFF458");

    private static SecretKey secretKey;

   public static void main(String[] args) {

		try {

			String input = "hello";

			byte[] toEncrypt = padWithZeros(input.getBytes("UTF8"));

			Cipher aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
			byte[] iv = new byte[16];
			System.arraycopy(SHARED_KEY, 0, iv, 0, 16);
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
			String myAES = getSecretKey().toString();
                        System.out.println(myAES);
                        aesCipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), paramSpec);

			byte[] encrypted = aesCipher.doFinal(toEncrypt);
			System.out.println("encrypted: \t" + byteArrayToHexString(encrypted));

			MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

			byte[] hash = messageDigest.digest((encrypted));

			System.out.println("hash: \t\t" + byteArrayToHexString(hash));

			hash = padWithZeros(hash);
			byte[] encryptedSignature = aesCipher.doFinal(hash);

			System.out.println("signature: \t" + byteArrayToHexString(encryptedSignature));

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


   private static synchronized SecretKey getSecretKey()
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			UnsupportedEncodingException {

		if (secretKey == null) {
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
			KeySpec keySpec = new SecretKeySpec(SHARED_KEY, "AES/CBC/NoPadding");
			secretKey = keyFactory.generateSecret(keySpec);
		}
		return secretKey;
	}




	private static byte[] padWithZeros(byte[] input) {

		int rest = input.length % 16;
		if (rest > 0) {
			byte[] result = new byte[input.length + (16 - rest)];
			System.arraycopy(input, 0, result, 0, input.length);
			return result;
		}
		return input;
	}

	private static String byteArrayToHexString(byte[] block) {
		StringBuffer buf = new StringBuffer();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		int len = block.length;
		int high = 0;
		int low = 0;
		for (int i = 0; i < len; i++) {
			high = ((block[i] & 0xf0) >> 4);
			low = (block[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		return buf.toString();
	}

	private static byte[] hexstringToByteArray(String hex) {

		if (hex == null)
			return null;

		byte[] result = new byte[hex.length() / 2];

		int i = 0;
		for (int pos = 0; pos < hex.length(); pos += 2, i++) {
			byte b = 0;
			try {
				b = (byte) Integer.parseInt(hex.substring(pos, pos + 2), 16);

			} catch (NumberFormatException ex) {
				ex.printStackTrace();

			}
			result[i] = b;
		}

		return result;
	}

}