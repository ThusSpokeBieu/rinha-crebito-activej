package github.mess;

import github.mess.utils.HttpUtils;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.json.JsonCodec;
import io.activej.json.JsonUtils;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractReactive;
import io.activej.reactor.Reactive;
import io.activej.reactor.nio.NioReactor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import javax.sql.DataSource;

public class TransacaoHandler extends AbstractReactive {

  private static ThreadLocal<String> ID_STR = ThreadLocal.withInitial(String::new);
  private static ThreadLocal<Transacao> REQUEST_BODY =
      ThreadLocal.withInitial(() -> new Transacao(0, "", ""));
  private static ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[1]);
  private static final JsonCodec<Transacao> JSON_CODEC = TransacaoCodec.create();
  private static final String TRANSACAO_SQL = "SELECT * FROM transacao(?, ?, ?::tipo_transacao, ?)";

  private final DataSource ds;
  private final Executor executor;

  public TransacaoHandler(final NioReactor reactor, final DataSource ds, final Executor executor)
      throws SQLException {
    super(reactor);
    this.ds = ds;
    this.executor = executor;
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    ID_STR.set(request.getPathParameter(HttpUtils.ID));
    try {
      final int id = Integer.parseInt(ID_STR.get());
      if (id < 1 || id > 5) return HttpUtils.handle404();
      return handlePayload(request, id);
    } catch (NumberFormatException e) {
      return HttpUtils.handle404();
    } finally {
      ID_STR.remove();
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
    Reactive.checkInReactorThread(this);
    return Promise.ofBlocking(
        executor,
        () -> {
          try (Connection connection = ds.getConnection();
              PreparedStatement stmt =
                  connection.prepareStatement(
                      TRANSACAO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setInt(1, id);
            stmt.setInt(2, transacao.valor());
            stmt.setString(3, transacao.tipo());
            stmt.setString(4, transacao.descricao());

            ResultSet rs = stmt.executeQuery();

            rs.next();
            int error = rs.getInt(1);

            return switch (error) {
              case 2 -> HttpResponse.builder().withCode(422).build();
              default -> HttpResponse.ok200().withBody(rs.getString(2)).build();
            };
          } catch (Exception e) {
            e.printStackTrace();
            throw e;
          }
        });
  }
}
