package github.mess.handlers;

import github.mess.Transacao;
import github.mess.codecs.TransacaoCodec;
import github.mess.utils.HttpUtils;
import io.activej.bytebuf.ByteBuf;
import io.activej.common.collection.Try;
import io.activej.common.function.SupplierEx;
import io.activej.common.tuple.Tuple2;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.json.JsonUtils;
import io.activej.promise.Promise;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransacaoHandler {
  private static final String TRANSACAO_SQL = "SELECT * FROM transacao(?, ?, ?, ?)";

  private final PreparedStatement stmt;

  public TransacaoHandler(final Connection connection) throws SQLException {
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

  private Transacao desserializeTransacao(final ByteBuf body) {
    try {
      Transacao transacao = JsonUtils.fromJsonBytes(TransacaoCodec.CODEC, body.getArray());
      if (transacao.isValid()) return transacao;

      return null;
    } catch (Exception e) {
      return null;
    }
  }

  public Tuple2<Integer, String> handleTransacao(final Transacao transacao, final int id) {
    try {
      if (transacao == null) return null;

      stmt.setInt(1, id);
      stmt.setInt(2, transacao.valor());
      stmt.setString(3, transacao.tipo());
      stmt.setString(4, transacao.descricao());

      ResultSet rs = stmt.executeQuery();
      rs.next();

      return new Tuple2<>(rs.getInt(1), rs.getString(2));
    } catch (Exception e) {
      return null;
    }
  }

  public Promise<HttpResponse> handleResponse(final Tuple2<Integer, String> result) {
    if (result == null) return HttpUtils.handle422();

    return switch (result.value1()) {
      case 1 -> HttpUtils.handle404();
      case 2 -> HttpUtils.handle422();
      default -> HttpResponse.ok200().withBody(result.value2()).toPromise();
    };
  }
}
