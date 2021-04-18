package com.search.service.security.encrypt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

/**
 * The type Pbe.
 */
public class PBE {
    /**
     * bytes used to salt the key (set before making an instance)
     */
    public static byte[] salt = {
            (byte) 0xc8, (byte) 0x73, (byte) 0x41, (byte) 0x8c,
            (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89
    };

    /**
     * number of times the password & salt are hashed during key creation (set before making an instance)
     */
    public static int numIterations = 1024;

    private SecretKey secretKey = null;

    private Cipher cipher = null;

    /**
     * Instantiates a new Pbe.
     * @param password the password
     * @throws InvalidKeySpecException  the invalid key spec exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws NoSuchPaddingException   the no such padding exception
     */
    public PBE(char[] password) throws InvalidKeySpecException,
            NoSuchAlgorithmException,
            NoSuchPaddingException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, 1024, 256);
        secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * encrypt a byte array
     * @param cleartext the cleartext
     * @return the pbe storage
     * @throws IllegalBlockSizeException     the illegal block size exception
     * @throws BadPaddingException           the bad padding exception
     * @throws InvalidKeyException           the invalid key exception
     * @throws InvalidParameterSpecException the invalid parameter spec exception
     */
    public PBEStorage encrypt(byte[] cleartext) throws IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return new PBEStorage(
                cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV()
                , cipher.doFinal(cleartext)
        );
    }

    /**
     * decrypt a PBEStorage object (IV and ciphertext)
     * @param storage the storage
     * @return the byte [ ]
     * @throws BadPaddingException                the bad padding exception
     * @throws IllegalBlockSizeException          the illegal block size exception
     * @throws InvalidAlgorithmParameterException the invalid algorithm parameter exception
     * @throws InvalidKeyException                the invalid key exception
     */
    public byte[] decrypt(PBEStorage storage) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(storage.getIv()));
        return cipher.doFinal(storage.getCiphertext());
    }

}
