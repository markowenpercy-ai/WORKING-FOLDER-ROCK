package com.go2super.socket.util;

import org.jasypt.util.text.StrongTextEncryptor;

public class Crypto {

    private static final String SECRET = System.getenv().getOrDefault("CRYPTO_SECRET", "6c4c0cc399f655b313b1719287b3fde1");

    private static final StrongTextEncryptor encryptor = new StrongTextEncryptor();

    static {

        encryptor.setPassword(SECRET);


    }

    public static String encrypt(String text) {

        return encryptor.encrypt(text);
    }

    public static String decrypt(String text) {

        return encryptor.decrypt(text);
    }

}
