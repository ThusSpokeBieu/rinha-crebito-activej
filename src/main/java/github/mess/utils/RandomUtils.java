package github.mess.utils;

import github.mess.App;
import io.activej.http.HttpMethod;
import io.activej.http.HttpRequest;
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

  public static HttpRequest generateRandomRequest() {
    int valor = generateRandomPositiveInt(100000);
    char tipo = generateRandomChar();
    String descricao = generateRandomString(10);

    String json =
        String.format(
            "{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", valor, tipo, descricao);
    return createRequest(json.getBytes());
  }

  public static HttpRequest GenerateNotRandomReqD() {
    String json =
        String.format(
            "{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", 1, 'd', "validacao");
    return createRequest(json.getBytes());
  }

  public static HttpRequest generateNotRandomReqC() {
    String json =
        String.format(
            "{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", 1, 'c', "validacao");

    return createRequest(json.getBytes());
  }

  public static HttpRequest generateOtherNotRandomReqC() {
    String json =
        String.format("{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", 1, 'c', "toma");
    return createRequest(json.getBytes());
  }

  public static HttpRequest generateOtherNotRandomReqD() {
    String json =
        String.format(
            "{\"valor\": %d, \"tipo\": \"%c\", \"descricao\": \"%s\"}", 1, 'd', "devolve");
    return createRequest(json.getBytes());
  }

  public static HttpRequest createRequest(byte[] payloadByte) {
    return HttpRequest.builder(HttpMethod.POST, "http://localhost:8080" + App.PATH_TRANSACAO)
        .withBody(payloadByte)
        .build();
  }
}
