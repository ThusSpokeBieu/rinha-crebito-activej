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
import java.sql.DriverManager;
import java.sql.SQLException;

public class App extends HttpServerLauncher {

  private static final String PATH_EXTRATO = "/clientes/:id/extrato";
  private static final String PATH_TRANSACAO = "/clientes/:id/transacoes";
  private Connection connection;

  @Provides
  Connection dataSourcePg(Config config) throws SQLException, IOException {
    String connectionStr =
        "jdbc:postgresql://localhost/crebito?user=rinha&password=rinha?binaryTransfer=true?preparedStatementCacheQueries=1024?prepareThreshold=1?preparedStatementCacheSizeMiB=20?preferQueryMode=extendedCacheEverything?tcpKeepAlive=true?socketFactory=org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg&socketFactoryArg=/var/run/postgresql/.s.PGSQL.5431";
    connection = DriverManager.getConnection(connectionStr);
    return connection;
  }

  @Provides
  ExtratoHandler extratoHandler(Connection connection) throws SQLException {
    return new ExtratoHandler(connection);
  }

  @Provides
  TransacaoHandler transacaoHandler(Connection connection) throws SQLException {
    return new TransacaoHandler(connection);
  }

  @Override
  protected void run() throws Exception {
    connection.prepareStatement("SELECT 1").execute();
    connection.prepareStatement("TRUNCATE transacoes").execute();
    connection.prepareStatement("UPDATE clientes SET saldo = 0").execute();
    System.out.println("tô rodano filé 😎");
    awaitShutdown();
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
