package github.mess;

import github.mess.utils.HttpUtils;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
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

public class ExtratoHandler extends AbstractReactive {
  private static final String EXTRATO_SQL = "SELECT * FROM extrato(?)";

  private final DataSource ds;
  private final Executor executor;

  public ExtratoHandler(final NioReactor reactor, final DataSource ds, final Executor executor)
      throws SQLException {
    super(reactor);
    this.ds = ds;
    this.executor = executor;
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    final String idStr = request.getPathParameter(HttpUtils.ID);
    try {
      final int id = Integer.parseInt(idStr);
      if (id < 1 || id > 5) return HttpUtils.handle404();
      return handleExtrato(id);
    } catch (NumberFormatException e) {
      return HttpUtils.handle404();
    } catch (SQLException e) {
      return HttpUtils.handleError(e);
    }
  }

  public Promise<HttpResponse> handleExtrato(final int id) throws SQLException {
    Reactive.checkInReactorThread(this);

    return Promise.ofBlocking(
        executor,
        () -> {
          try (Connection connection = ds.getConnection();
              PreparedStatement stmt =
                  connection.prepareStatement(
                      EXTRATO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            rs.next();
            return HttpResponse.ok200().withBody(rs.getString(1)).build();
          } catch (Exception e) {
            throw e;
          }
        });
  }
}
