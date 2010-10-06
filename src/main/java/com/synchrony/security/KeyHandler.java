/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 *
 * @author blip
 */
public class KeyHandler {

    private static SecretKey generateAESKey() throws NoSuchProviderException, Exception {
        SecretKey sk = null;
        SecureRandom sr = null;
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
            kg.init(256, sr);
            sk = kg.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new Exception("Error while generating the secret key");
        }
        return sk;
    }
    private SecretKeyFactory keyFac;
    private KeySpec pbeKeySpec;

    private void pwbEnc() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Create PBE parameter set
        //pbeParamSpec = new PBEParameterSpec();

        // Prompt user for encryption password.
        // Collect user password as char array (using the
        // "readPasswd" method from above), and convert
        // it into a SecretKey object, using a PBE key
        // factory.
        System.out.print("Enter encryption password:  ");
        System.out.flush();
        pbeKeySpec = new PBEKeySpec("Password".toCharArray());
        keyFac = SecretKeyFactory.getInstance("PBEWith");
        SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

        // Create PBE Cipher
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");

        // Initialize PBE Cipher with key and parameters
        pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey);

        // Our cleartext
        byte[] cleartext = "This is another example".getBytes();

        // Encrypt the cleartext
        byte[] ciphertext = pbeCipher.doFinal(cleartext);
    }
}
