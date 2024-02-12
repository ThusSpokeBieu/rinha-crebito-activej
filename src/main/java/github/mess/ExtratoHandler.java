package github.mess;

import github.mess.utils.HttpUtils;
import io.activej.http.HttpRequest;
import io.activej.http.HttpResponse;
import io.activej.promise.Promise;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExtratoHandler {
  private static final String EXTRATO_SQL = "SELECT * FROM extrato(?)";
  private final PreparedStatement stmt;

  public ExtratoHandler(final Connection connection) throws SQLException {
    this.stmt =
        connection.prepareCall(
            EXTRATO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
  }

  public Promise<HttpResponse> handleRequest(final HttpRequest request) {
    String idStr = request.getPathParameter(HttpUtils.ID);
    try {
      final int id = Integer.parseInt(idStr);
      if (id < 1 || id > 5) return HttpUtils.handle404();
      return handleExtrato(id);
    } catch (NumberFormatException e) {
      return HttpUtils.handle404();
    } catch (SQLException e) {
      e.printStackTrace();
      return HttpUtils.handleError(e);
    }
  }

  public Promise<HttpResponse> handleExtrato(final int id) throws SQLException {
    try {
      stmt.setInt(1, id);

      ResultSet rs = stmt.executeQuery();
      rs.next();
      return HttpResponse.ok200().withBody(rs.getString(1)).toPromise();
    } catch (Exception e) {
      return HttpUtils.handleError(e);
    }
  }
}