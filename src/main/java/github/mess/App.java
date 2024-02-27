package github.mess;

import github.mess.handlers.ExtratoHandler;
import github.mess.handlers.TransacaoHandler;
import github.mess.handlers.WarmUp;
import github.mess.utils.HttpUtils;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.nio.NioReactor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PreferQueryMode;

public class App extends HttpServerLauncher {

  public static final String PATH_EXTRATO = "/clientes/:id/extrato";
  public static final String PATH_TRANSACAO = "/clientes/:id/transacoes";

  @Provides
  Connection connection() throws SQLException, IOException {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setDatabaseName("crebito");
    dataSource.setUser("rinha");
    dataSource.setPassword("rinha");
    dataSource.setBinaryTransfer(true);
    dataSource.setPreparedStatementCacheQueries(1024);
    dataSource.setPrepareThreshold(1);
    dataSource.setPreparedStatementCacheSizeMiB(240);
    dataSource.setPreferQueryMode(PreferQueryMode.EXTENDED_CACHE_EVERYTHING);
    dataSource.setTcpKeepAlive(true);
    dataSource.setSocketFactory("org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
    dataSource.setSocketFactoryArg("/var/run/postgresql/.s.PGSQL.5432");
    dataSource.setMaxResultBuffer("100000");

    return dataSource.getConnection();
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
  WarmUp warmUp(
      Connection connection, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler)
      throws Exception {
    var warmUp = new WarmUp(connection, transacaoHandler, extratoHandler);
    warmUp.warmUp();
    warmUp.prepare();
    return warmUp;
  }

  @Provides
  AsyncServlet servlet(
      NioReactor reactor, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler) {
    return RoutingServlet.builder(reactor)
        .with(PATH_EXTRATO, extratoHandler::handleRequest)
        .with(PATH_TRANSACAO, transacaoHandler::handleRequest)
        .with(
            "/health-check",
            request -> WarmUp.IS_WARMING ? HttpUtils.isWarming() : HttpUtils.isOK())
        .build();
  }

  @Override
  protected void run() throws Exception {
    System.out.println("🚀🚀 agr tô rodano filé 😎 🔥🔥 🚀🚀");
    WarmUp.IS_WARMING = false;
    awaitShutdown();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("calmae meu cumpadi q eu to aqueceno 🥵🥵🥵💦💦");
    Launcher launcher = new App();
    launcher.launch(args);
  }
}
