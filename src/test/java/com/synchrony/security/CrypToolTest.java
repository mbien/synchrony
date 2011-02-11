/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.security;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author blip
 */
public class CrypToolTest {

    public static String password = CrypToolUtil.fitToPasswordLength("synchrony");

    @Before
    public void setUp() {
        System.out.println("---------------------");

    }

    @After
    public void tearDown() {
    }

    /**
     * A quite small plaintext, i.e. a randomly generated string of 32 bytes
     * which can be used to create a session key, should be encrypted and then
     * decrypted again properly.
     */
    @Test
    public void testEncryptionAndDecrytion() {
        try {
            String randomString = CrypTool.getRandomString(CrypTool.PASSWORD_SIZE);
            System.out.println("randomString (" + randomString.length() + "): " + randomString);

            byte[] cipherText = CrypTool.encrypt(password.getBytes(), randomString.getBytes());
            System.out.println("cipherText (" + cipherText.length + "): " + CrypToolUtil.byteArrayToHexString(cipherText));

            byte[] plainText = CrypTool.decrypt(password.getBytes(), cipherText);
            System.out.println("plainText (" + plainText.length + "): " + new String(plainText));

            assertEquals(randomString, new String(plainText));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * A large plaintext, i.e. a file of some MBs, should be encrypted
     * and then decrypted again using Streams.
     */
    @Test
    public void testStreamEncryption() throws FileNotFoundException {
        try {
            // FileInputStream fis = new FileInputStream("/home/blip/Downloads/netbeans-6.9.1-ml-java-linux.sh");
            String testfile = "/home/blip/Downloads/1_Einfuhrung.pdf";
            FileInputStream fis = new FileInputStream(testfile);
            //ByteArrayInputStream bis = CrypToolUtil.InputStreamToByteArrayInputStream(fis);
            FileOutputStream fos = new FileOutputStream("/tmp/encfile");
            CrypTool.encryptStream(fis, password.getBytes(), fos);
            fis.close();
            fos.close();
            System.out.println("File sucessfully encrypted (File: " + testfile + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * A large plaintext, i.e. a file of some MBs, should be encrypted
     * and then decrypted again using Streams.
     */
    @Test
    public void testStreamDecryption() throws FileNotFoundException {
        try {
            // FileInputStream fis = new FileInputStream("/home/blip/Downloads/netbeans-6.9.1-ml-java-linux.sh");
            String cipherfile = "/tmp/encfile";
            FileInputStream fis = new FileInputStream(cipherfile);
            FileOutputStream fos = new FileOutputStream("/tmp/plaintext.pdf");
            CrypTool.decryptStream(fis, password.getBytes(), fos);
            fis.close();
            fos.close();
            System.out.println("File sucessfully decrypted (File: " + cipherfile + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * A master key and a randomly generated string combined
     * generate a session key. Plaintext should be encrypted with this
     * session key. Then, this session key should be created once again
     * and the ciphertext should be decryptable.
     */
    @Test
    public void testSessionKeyUsage() {

        try {
            byte[] randomString = CrypTool.getRandomString(CrypTool.PASSWORD_SIZE).getBytes();
            System.out.println("randomString (" + randomString.length + "): " + new String(randomString));

            byte[] sessionKeyString = CrypTool.getSessionKeyString(randomString, CrypToolUtil.fitToPasswordLength(password).getBytes());
            System.out.println("sessionKeyString (" + sessionKeyString.length + "): " + CrypToolUtil.byteArrayToHexString(sessionKeyString));

            byte[] cipherText = CrypTool.encrypt(sessionKeyString, randomString);
            System.out.println("cipherText (" + cipherText.length + "): " + CrypToolUtil.byteArrayToHexString(cipherText));

            byte[] plainText = CrypTool.decrypt(sessionKeyString, cipherText);
            System.out.println("plainText (" + plainText.length + "): " + new String(plainText));

            assertEquals(new String(randomString), new String(plainText));

        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * Tests the random string generation. For example, this tests let
     * generate about 1000000 random strings with a length of CrypTool.PASSWORD_SIZE bytes.
     * There should be no identical strings.
     */
    @Test
    public void testRandomStringGeneration() throws Exception {

        String z = CrypToolUtil.generateRandomString(CrypTool.PASSWORD_SIZE);
        String key = CrypToolUtil.generateRandomString(CrypTool.PASSWORD_SIZE);

        System.out.println("z: \t" + CrypToolUtil.byteArrayToHexString(z.getBytes()));
        System.out.println("key: \t" + CrypToolUtil.byteArrayToHexString(key.getBytes()));

        byte[] xorByteArray = CrypToolUtil.xorByteArray(z.getBytes(), key.getBytes());
        System.out.println("Xor: \t" + CrypToolUtil.byteArrayToHexString(xorByteArray));

        xorByteArray = CrypToolUtil.xorByteArray(key.getBytes(), xorByteArray);
        System.out.println("Xor² (z again): \t" + CrypToolUtil.byteArrayToHexString(xorByteArray));


        System.out.println(CrypToolUtil.byteArrayToHexString(CrypToolUtil.xorByteArray(z.getBytes(), key.getBytes())));
        System.out.println(CrypToolUtil.byteArrayToHexString(CrypToolUtil.xorByteArray(key.getBytes(), z.getBytes())));



    }
}
