package com.search.service.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class AESDecrypt {
    private static final String characterEncoding = "UTF-8";
    private static final String cipherTransformation = "AES/CBC/PKCS5Padding";
    private static final String aesEncryptionAlgorithm = "AES";

    public static byte[] decryptBase64EncodedWithManagedIV(String encryptedText, String key) throws Exception {
        byte[] cipherText = Base64.decodeBase64(encryptedText.getBytes());
        byte[] keyBytes = Base64.decodeBase64(key.getBytes());
        return decryptWithManagedIV(cipherText, keyBytes);
    }

    public static byte[] decryptWithManagedIV(byte[] cipherText, byte[] key) throws Exception {
        byte[] initialVector = Arrays.copyOfRange(cipherText, 0, 16);
        byte[] trimmedCipherText = Arrays.copyOfRange(cipherText, 16, cipherText.length);
        return decrypt(trimmedCipherText, key, initialVector);
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] initialVector) throws Exception {
        Cipher cipher = Cipher.getInstance(cipherTransformation);
        SecretKeySpec secretKeySpecy = new SecretKeySpec(key, aesEncryptionAlgorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
        cipherText = cipher.doFinal(cipherText);
        return cipherText;
    }

    public static void main(String args[]) throws Exception {
        byte[] clearText = decryptBase64EncodedWithManagedIV("CERcUfcNbCAkVxklXVpMqko2FqhE12iU6eldQ9jpFPUl+uVQXKDCXxtfPQ1hwt9A5fIbt60kdVgyFhb2V40z7w==", "mRMjHmlC1C+1L/Dkz8EJuw==");
        System.out.println("ClearText:" + new String(clearText, characterEncoding));
    }

}
