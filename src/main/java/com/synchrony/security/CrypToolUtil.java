/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synchrony.security;

/**
 *
 * @author blip
 */
public class CrypToolUtil {

    public static byte[] padWithZeros(byte[] input) {

        int rest = input.length % 16;
        if (rest > 0) {
            byte[] result = new byte[input.length + (16 - rest)];
            System.arraycopy(input, 0, result, 0, input.length);
            return result;
        }
        return input;
    }

    public static byte[] hexStringToByteArray(String hex) {

        if (hex == null) {
            return null;
        }

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

    /**
     * Converts a byte[] to a hex string.
     * @param cipherText byte[]
     * @return String Every byte results in two hex digits.
     */
    public static String byteArrayToHexString(byte[] cipherText) {
        String hexStr = "";
        for (int y = 0; y < cipherText.length; y++) {
            hexStr += Integer.toString((cipherText[y] & 0xff) + 0x100, 16).substring(1);
        }
        return hexStr;
    }

    public static String fitToPasswordLength(String string) {
        int stringLength = string.length();
        int passwordLength = CrypTool.PASSWORD_SIZE;
        
        if (stringLength > passwordLength) {
            string = string.substring(0, passwordLength);
        } else if (stringLength < passwordLength) {
            while (string.length() < passwordLength) {
                string += string;
            }
            string = fitToPasswordLength(string);
        }
        return string;
    }


    /** The random number generator. */
    protected static java.util.Random r = new java.util.Random();

    /*
     * Set of characters that is valid. Must be printable, memorable, and "won't
     * break HTML" (i.e., not ' <', '>', '&', '=', ...). or break shell commands
     * (i.e., not ' <', '>', '$', '!', ...). I, L and O are good to leave out,
     * as are numeric zero and one.
     */
    protected static char[] goodChar = {'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
        'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
        'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    /* Generate a Password object with a random password. */
    public static String generateRandomString(int length) {


        StringBuffer sb = new StringBuffer();
        //length = r.nextInt(length);

        for (int i = 0; i < length; i++) {
            sb.append(goodChar[r.nextInt(goodChar.length)]);
        }
        return sb.toString();
    }

    public static byte[] xorByteArray(byte[] a, byte[] b) throws Exception {
        if (b.length < a.length) {
            System.out.println("a length:" + a.length);
            System.out.println("b length:" + b.length);
            throw new IllegalArgumentException("length of byte [] b must be >= byte [] a");
        }
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }
}
