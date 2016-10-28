package com.universal.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class DESUtils {

    public static String decrypt(final String ciphertext, final String key) throws Exception {

        final SecretKey secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "DESede");
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] bytes = Hex.decodeHex(ciphertext.toCharArray());

        return new String(cipher.doFinal(bytes), "UTF-8");
    }

    public static String encrypt(final String plaintext, final String key) throws Exception {

        final SecretKey secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "DESede");
        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] bytes = cipher.doFinal(plaintext.getBytes("UTF-8"));

        return Hex.encodeHexString(bytes);
    }
}
