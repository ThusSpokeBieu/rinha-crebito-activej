package github.mess.handlers;

import github.mess.utils.HttpUtils;
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
            EXTRATO_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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
