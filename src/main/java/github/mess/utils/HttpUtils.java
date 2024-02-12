package github.mess.utils;

import io.activej.common.exception.MalformedDataException;
import io.activej.http.HttpResponse;
import io.activej.promise.Promise;
import java.io.IOException;

public class HttpUtils {

  public static final String ID = "id";

  public static Promise<HttpResponse> handleError(Throwable e) {
    return switch (e) {
      case MalformedDataException $ -> HttpUtils.handle422();
      case IOException $ -> HttpUtils.handle422();
      default -> HttpResponse.ofCode(500).withBody(e.getMessage()).toPromise();
    };
  }

  public static Promise<HttpResponse> handle422() {
    return HttpResponse.ofCode(422).toPromise();
  }

  public static Promise<HttpResponse> handle404() {
    return HttpResponse.ofCode(404).toPromise();
  }
}
