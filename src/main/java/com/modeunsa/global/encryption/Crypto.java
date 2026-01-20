package com.modeunsa.global.encryption;

public interface Crypto {

  String encrypt(String plainText);

  String decrypt(String cipherText);

  boolean isEnabled();
}
