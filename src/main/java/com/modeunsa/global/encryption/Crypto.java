package com.modeunsa.global.encryption;

public interface Crypto {

  void init();

  String encrypt(String plainText);

  String decrypt(String cipherText);
}
