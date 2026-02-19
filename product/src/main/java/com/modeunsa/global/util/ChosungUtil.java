package com.modeunsa.global.util;

public class ChosungUtil {
  private static final char[] CHOSUNG = {
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
  };

  public static String extract(String text) {
    if (text == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();

    for (char ch : text.toCharArray()) {
      if (ch >= 0xAC00 && ch <= 0xD7A3) {
        int index = (ch - 0xAC00) / 588;
        sb.append(CHOSUNG[index]);
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }
}
