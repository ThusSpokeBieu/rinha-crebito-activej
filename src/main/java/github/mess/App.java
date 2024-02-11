package github.mess;

import io.activej.config.Config;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;

public class App extends HttpServerLauncher {

  private static final String PATH_EXTRATO = "/clientes/:id/extrato";
  private static final String PATH_TRANSACAO = "/clientes/:id/transacoes";

  @Provides
  Connection dataSourcePg(Config config) throws SQLException, IOException {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setServerName(config.get("pg.server", "0.0.0.0"));
    ds.setDatabaseName(config.get("pg.db", "crebito"));
    ds.setPassword(config.get("pg.pass", "rinha"));
    ds.setUser(config.get("pg.user", "rinha"));
    ds.setBinaryTransfer(true);

    return ds.getConnection();
  }

  @Provides
  ExtratoHandler extratoHandler(Connection connection) throws SQLException {
    return new ExtratoHandler(connection);
  }

  @Provides
  TransacaoHandler transacaoHandler(Connection connection) throws SQLException {
    return new TransacaoHandler(connection);
  }

  @Provides
  AsyncServlet servlet(
      Reactor reactor, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler) {
    return RoutingServlet.builder(reactor)
        .with(PATH_EXTRATO, extratoHandler::handleRequest)
        .with(PATH_TRANSACAO, transacaoHandler::handleRequest)
        .build();
  }

  public static void main(String[] args) throws Exception {
    Launcher launcher = new App();
    launcher.launch(args);
  }
}
