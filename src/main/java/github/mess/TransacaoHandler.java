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
  private static ThreadLocal<String> ID_STR = ThreadLocal.withInitial(String::new);
  private static ThreadLocal<Integer> ID = ThreadLocal.withInitial(() -> 0);
  private static ThreadLocal<Transacao> REQUEST_BODY = ThreadLocal.withInitial(Transacao::new);
  private static ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[1]);
  private static final JsonCodec<Transacao> JSON_CODEC = TransacaoCodec.create();
  private static final String TRANSACAO_SQL = "SELECT * FROM transacao(?, ?, ?, ?)";

  private final PreparedStatement stmt;

  public TransacaoHandler(final Connection connection) throws SQLException {
    this.stmt =
        connection.prepareStatement(
            TRANSACAO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    ID_STR.set(request.getPathParameter(HttpUtils.ID));
    try {
      ID.set(Integer.parseInt(ID_STR.get()));
      final int id = ID.get();
      if (id < 1 || id > 5) return HttpUtils.handle404();
      return handlePayload(request, id);
    } catch (NumberFormatException e) {
      return HttpUtils.handle404();
    } catch (Exception e) {
      return HttpUtils.handleError(e);
    } finally {
      ID_STR.remove();
      ID.remove();
    }
  }

  public Promise<HttpResponse> handlePayload(final HttpRequest request, final int id) {
    return request
        .loadBody()
        .then(
            $ -> {
              BUFFER.set(request.getBody().getArray());
              try {
                REQUEST_BODY.set(JsonUtils.fromJsonBytes(JSON_CODEC, BUFFER.get()));
                Transacao transacao = REQUEST_BODY.get();
                return handleTransacao(transacao, id);
              } catch (Exception e) {
                return HttpUtils.handleError(e);
              } finally {
                BUFFER.remove();
                REQUEST_BODY.remove();
              }
            },
            e -> HttpUtils.handleError(e));
  }

  public Promise<HttpResponse> handleTransacao(final Transacao transacao, final int id)
      throws SQLException {
    stmt.setInt(1, id);
    stmt.setInt(2, transacao.getValor());
    stmt.setString(3, String.valueOf(transacao.getTipo()));
    stmt.setString(4, transacao.getDescricao());

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
