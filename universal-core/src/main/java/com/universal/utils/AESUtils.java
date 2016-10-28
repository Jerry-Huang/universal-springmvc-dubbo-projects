package com.universal.utils;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AESUtils {

    private final static Logger logger = LoggerFactory.getLogger(AESUtils.class);

    private final static String DEFAULT_KEY = "dongbinhusername";
    private final static byte[] DEFAULT_IV = { 0x12, 0x34, 0x56, 0x78, -0x70, -0x55, -0x33, -0x11, 0x12, 0x34, 0x56, 0x78, -0x70, -0x55, -0x33, -0x11 };

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static String encrypt(final String plaintext) {

        return encrypt(plaintext, DEFAULT_KEY);
    }

    public static String encrypt(final String plaintext, final String key) {

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(DEFAULT_IV));

            byte[] output = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeBase64String(output);

        } catch (Exception e) {

            logger.error("AES encrypt failed: AES/CBC/PKCS7Padding, " + plaintext, e);
        }

        return StringUtils.EMPTY;
    }

    public static String decrypt(final String ciphertext) {

        return decrypt(ciphertext, DEFAULT_KEY);
    }

    public static String decrypt(final String ciphertext, final String key) {

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(DEFAULT_IV));

            byte[] output = cipher.doFinal(Base64.decodeBase64(ciphertext));
            return new String(output, "UTF-8");

        } catch (Exception e) {

            logger.error("AES decrypt failed: AES/CBC/PKCS7Padding, " + ciphertext, e);
        }

        return StringUtils.EMPTY;
    }
}
