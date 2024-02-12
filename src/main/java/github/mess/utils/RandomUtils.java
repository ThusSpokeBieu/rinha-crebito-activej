package github.mess.utils;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtils {

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final Random RANDOM = new SecureRandom();

  public static String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = RANDOM.nextInt(CHARACTERS.length());
      sb.append(CHARACTERS.charAt(randomIndex));
    }
    return sb.toString();
  }

  public static int generateRandomPositiveInt(int max) {
    return RANDOM.nextInt(max) + 1;
  }

  public static char generateRandomChar() {
    return RANDOM.nextBoolean() ? 'd' : 'c';
  }

  public static byte[] generateRandomJsonBytes() {
    int valor = generateRandomPositiveInt(100000);
    char tipo = generateRandomChar();
    String descricao = generateRandomString(10);

    String json =
        String.format(
            "{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", valor, tipo, descricao);
    return json.getBytes();
  }
}
