package com.search.service.security.encrypt;

import java.util.Base64;

public class PBEStorage {
    private byte[] iv;
    private byte[] ciphertext;
    public static String separator = " *** ";

    public PBEStorage(byte[] iv, byte[] ciphertext) {
        this.iv = iv;
        this.ciphertext = ciphertext;
    }

    public PBEStorage(String base64) {
        this(base64, separator);
    }

    public PBEStorage(String base64, String separator) {
        int loc = base64.indexOf(separator);
        iv = Base64.getDecoder().decode(base64.substring(0, loc));
        ciphertext = Base64.getDecoder().decode(base64.substring(loc + separator.length()));
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public byte[] getIv() {
        return iv;
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(iv) + separator + Base64.getEncoder().encodeToString(ciphertext);
    }

}
