package github.mess;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import github.mess.utils.HttpUtils;
import io.activej.bytebuf.ByteBuf;
import io.activej.common.collection.Try;
import io.activej.common.function.SupplierEx;
import io.activej.common.tuple.Tuple2;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.promise.Promise;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TransacaoHandler {
  private static final String TRANSACAO_SQL = "SELECT * FROM transacao(?, ?, ?, ?)";

  private final PreparedStatement stmt;
  private Transacao transacao = new Transacao(0, "", "");
  private final JsonReader<Object> jsonReader;

  public TransacaoHandler(final DslJson<Object> dslJson, final Connection connection)
      throws SQLException {
    this.jsonReader = dslJson.newReader();
    this.stmt =
        connection.prepareStatement(
            TRANSACAO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    return Try.wrap(
            (SupplierEx<Integer>) () -> Integer.parseInt(request.getPathParameter(HttpUtils.ID)))
        .reduce(id -> handlePayload(request, id), e -> HttpUtils.handle404());
  }

  public Promise<HttpResponse> handlePayload(final HttpRequest request, final int id) {
    return request
        .loadBody()
        .map(this::desserializeTransacao)
        .map(transacao -> handleTransacao(transacao, id))
        .then(this::handleResponse);
  }

  private Optional<Transacao> desserializeTransacao(final ByteBuf body) {
    try {
      transacao =
          jsonReader
              .process(body.getArray(), body.getArray().length)
              .next(TransacaoConverter.JSON_BINDER, transacao);

      if (TransacaoValidator.isValid(transacao)) {
        return Optional.of(transacao);
      }

      return Optional.empty();
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Tuple2<Integer, String>> handleTransacao(
      final Optional<Transacao> transacao, final int id) {
    try {
      if (transacao.isEmpty()) return Optional.empty();

      stmt.setInt(1, id);
      stmt.setInt(2, transacao.get().valor);
      stmt.setString(3, transacao.get().tipo);
      stmt.setString(4, transacao.get().descricao);

      ResultSet rs = stmt.executeQuery();
      rs.next();

      return Optional.of(new Tuple2<>(rs.getInt(1), rs.getString(2)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Promise<HttpResponse> handleResponse(final Optional<Tuple2<Integer, String>> result) {
    if (result.isEmpty()) return HttpUtils.handle422();

    return switch (result.get().value1()) {
      case 1 -> HttpUtils.handle404();
      case 2 -> HttpUtils.handle422();
      default -> HttpResponse.ok200().withBody(result.get().value2()).toPromise();
    };
  }
  ;
}
