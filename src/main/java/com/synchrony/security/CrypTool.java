/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CrypTool {

    /**
     * @param args the command line arguments
     */
    private static String password = "synchrony";
    private static String encryptionMode = "AES/ECB/PKCS5Padding";
    private static int BLOCK_SIZE = 1024;
    
    public static int PASSWORD_SIZE = 32;
    
    public static void main(String[] args) {

        try {

            String random = CrypToolUtil.generateRandomString(32);
            System.out.println("random (" + random.length() + "): " + random);


            String text = "Ich bin ja so geheim!";

            System.out.println(CrypToolUtil.byteArrayToHexString(getSessionKeyString(random.getBytes(), CrypToolUtil.fitToPasswordLength(password).getBytes())));

            System.out.println("Text: " + text);
            System.out.println("Text: " + CrypToolUtil.byteArrayToHexString((text.getBytes())));

            //byte[] encrypted = initCipher(password, Cipher.ENCRYPT_MODE).doFinal((text.getBytes()));
            byte[] encrypted = encrypt(password.getBytes(), text.getBytes());
            System.out.println("encrypted: \t" + CrypToolUtil.byteArrayToHexString(encrypted));

            // aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
            // byte[] decrypted = aesCipher.doFinal((encrypted));
            byte[] decrypted = decrypt(password.getBytes(), encrypted);
            System.out.println("decrypted: \t" + CrypToolUtil.byteArrayToHexString(decrypted));
            System.out.println("decrypted: \t" + new String(decrypted));

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public static String getRandomString(int length) {
        return CrypToolUtil.generateRandomString(length);
    }

    public static byte[] getSessionKeyString(byte[] randomString, byte[] password) throws Exception {
        return CrypToolUtil.xorByteArray(randomString, password);
    }

    public static byte[] encrypt(byte[] password, byte[] plainText) throws Exception {
        Cipher cipher = initCipher(password, Cipher.ENCRYPT_MODE);

        byte[] cipherText = cipher.doFinal(plainText);

        return cipherText;

    }

    public static byte[] decrypt(byte[] password, byte[] cipherText) throws Exception {
        Cipher cipher = initCipher(password, Cipher.DECRYPT_MODE);

        byte[] plainText = cipher.doFinal(cipherText);

        return plainText;

    }

    public static CipherOutputStream encryptStream(ByteArrayInputStream plainData, byte[] password) throws Exception {
        Cipher cipher = initCipher(password, Cipher.ENCRYPT_MODE);

        byte[] byteBuffer = new byte[BLOCK_SIZE];

        CipherOutputStream encryptedData = new CipherOutputStream(new ByteArrayOutputStream(byteBuffer.length), cipher);

        for (int n; (n = plainData.read(byteBuffer)) != -1; encryptedData.write(byteBuffer, 0, n));

        return encryptedData;
    }

    public static ByteArrayOutputStream decryptStream(ByteArrayInputStream encryptedData, byte[] password) throws Exception {
        Cipher cipher = initCipher(password, Cipher.DECRYPT_MODE);

        byte[] byteBuffer = new byte[BLOCK_SIZE];

        CipherInputStream in = new CipherInputStream(encryptedData, cipher);
        ByteArrayOutputStream plainData = new ByteArrayOutputStream(byteBuffer.length);

        for (int n; (n = encryptedData.read(byteBuffer)) != -1; plainData.write(byteBuffer, 0, n));

        return plainData;
    }

    public static Cipher initCipher(byte[] password, int mode) throws Exception {

        Cipher cipher = Cipher.getInstance(encryptionMode);

        SecretKey aesKey = getSecretKey(password);

        //System.out.println(aesKey.toString());

        cipher.init(mode, aesKey);

        return cipher;

    }


    /**
     * Generates a secretKey from a byte[]
     * @param password the password, must be 32 bytes long!
     * Herefore use CrypToolUitl.fitToPasswordSize() first
     * @return
     * @throws Exception
     */
    public static SecretKey getSecretKey(byte[] password) throws Exception {

        SecretKey secretKey = null;

        if (password.length == 32) {
            secretKey = new SecretKeySpec(password, "AES");
        } else {
            throw new RuntimeException("Password size was not correct!");
        }
        return secretKey;
    }

    //    /**
//     * Generates a secretKey from a String
//     * @param password the password's size doesn't matter, it will be fitted properly
//     * @return
//     * @throws Exception
//     */
//    public static SecretKey getSecretKey(String password) throws Exception {
//
//        SecretKey secretKey = null;
//
//        if (!password.isEmpty()) {
//            password = CrypToolUtil.fitToPasswordLength(password);
//            byte[] keyData = password.getBytes();
//            secretKey = new SecretKeySpec(keyData, "AES");
//
//        } else {
//            throw new RuntimeException("Password was empty!");
//        }
//        return secretKey;
//    }
}
