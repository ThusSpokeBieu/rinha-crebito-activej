package github.mess;

import github.mess.utils.HttpUtils;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.json.JsonCodec;
import io.activej.json.JsonUtils;
import io.activej.promise.Promise;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransacaoHandler {
  private static final JsonCodec<Transacao> JSON_CODEC = TransacaoCodec.create();
  private static final String TRANSACAO_SQL = "SELECT * FROM transacao(?, ?, ?::tipo_transacao, ?)";

  private final PreparedStatement stmt;

  public TransacaoHandler(final Connection connection) throws SQLException {
    this.stmt =
        connection.prepareStatement(
            TRANSACAO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    try {
      final int id = Integer.parseInt(request.getPathParameter(HttpUtils.ID));
      return handlePayload(request, id);
    } catch (NumberFormatException e) {
      return HttpUtils.handle404();
    }
  }

  public Promise<HttpResponse> handlePayload(final HttpRequest request, final int id) {
    return request
        .loadBody()
        .then(
            $ -> {
              try {
                return handleTransacao(
                    JsonUtils.fromJsonBytes(JSON_CODEC, request.getBody().getArray()), id);
              } catch (Exception e) {
                return HttpUtils.handleError(e);
              }
            },
            e -> HttpUtils.handleError(e));
  }

  public Promise<HttpResponse> handleTransacao(final Transacao transacao, final int id)
      throws SQLException {
    stmt.setInt(1, id);
    stmt.setInt(2, transacao.valor());
    stmt.setString(3, transacao.tipo());
    stmt.setString(4, transacao.descricao());

    ResultSet rs = stmt.executeQuery();

    rs.next();
    int error = rs.getInt(1);

    return switch (error) {
      case 1 -> HttpUtils.handle404();
      case 2 -> HttpUtils.handle422();
      default -> HttpResponse.ok200().withBody(rs.getString(2)).toPromise();
    };
  }
}
