package github.mess.handlers;

import github.mess.Transacao;
import github.mess.codecs.TransacaoCodec;
import io.activej.bytebuf.ByteBuf;
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

  public Promise<HttpResponse> handleTransacao(final ByteBuf body, final int id) {
    try {
      Transacao transacao = JsonUtils.fromJsonBytes(TransacaoCodec.CODEC, body.getArray());
      if (!transacao.isValid()) return HttpResponse.ofCode(422).toPromise();

      stmt.setInt(1, id);
      stmt.setInt(2, transacao.valor());
      stmt.setString(3, transacao.tipo());
      stmt.setString(4, transacao.descricao());

      ResultSet rs = stmt.executeQuery();
      rs.next();

      return HttpResponse.ofCode(rs.getInt(1)).withBody(rs.getString(2)).toPromise();
    } catch (Exception e) {
      return HttpResponse.ofCode(422).toPromise();
    }
  }
}
