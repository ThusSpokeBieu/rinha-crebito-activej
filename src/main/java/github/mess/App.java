package github.mess;

import github.mess.utils.HttpUtils;
import github.mess.utils.RandomUtils;
import io.activej.config.Config;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpMethod;
import io.activej.http.HttpRequest;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class App extends HttpServerLauncher {

  private static final String PATH_EXTRATO = "/clientes/:id/extrato";
  private static final String PATH_TRANSACAO = "/clientes/:id/transacoes";

  private Connection connection;
  private ExtratoHandler extratoHandler;
  private TransacaoHandler transacaoHandler;

  private boolean IS_WARMING = true;
  private int WARM_UP_TIME = 10;

  @Provides
  Connection dataSourcePg(Config config) throws SQLException, IOException {
    String connectionStr =
        "jdbc:postgresql://localhost/crebito?user=rinha&password=rinha?binaryTransfer=true?preparedStatementCacheQueries=1024?prepareThreshold=1?preparedStatementCacheSizeMiB=20?preferQueryMode=extendedCacheEverything?tcpKeepAlive=true?socketFactory=org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg&socketFactoryArg=/var/run/postgresql/.s.PGSQL.5431";
    connection = DriverManager.getConnection(connectionStr);
    return connection;
  }

  @Provides
  ExtratoHandler extratoHandler(Connection connection) throws SQLException {
    extratoHandler = new ExtratoHandler(connection);
    return extratoHandler;
  }

  @Provides
  TransacaoHandler transacaoHandler(Connection connection) throws SQLException {
    transacaoHandler = new TransacaoHandler(connection);
    return transacaoHandler;
  }

  @Override
  protected void run() throws Exception {
    warmup();
    Thread.sleep(2000);
    connection.prepareStatement("SELECT 1").execute();
    connection.prepareStatement("TRUNCATE transacoes").execute();
    connection.prepareStatement("UPDATE clientes SET saldo = 0").execute();
    for (int i = 1; i < 6; i++) {
      extratoHandler.handleExtrato(i);
    }
    System.out.println("🚀🚀 agr tô rodano filé 😎 🔥🔥 🚀🚀");
    IS_WARMING = false;
    awaitShutdown();
  }

  @Provides
  AsyncServlet servlet(
      Reactor reactor, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler) {
    return RoutingServlet.builder(reactor)
        .with(PATH_EXTRATO, extratoHandler::handleRequest)
        .with(PATH_TRANSACAO, transacaoHandler::handleRequest)
        .with("/healthcheck", request -> IS_WARMING ? HttpUtils.isWarming() : HttpUtils.isOK())
        .build();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("calmae meu cumpradi q eu to aqueceno 🥵🥵🥵💦💦");
    Thread.sleep(2000);
    Launcher launcher = new App();
    launcher.launch(args);
  }

  void warmup() throws Exception {

    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(WARM_UP_TIME)) {
      int id = ThreadLocalRandom.current().nextInt(6);
      final var request =
          HttpRequest.builder(HttpMethod.POST, "http://localhost:8080" + PATH_TRANSACAO)
              .withBody(RandomUtils.generateRandomJsonBytes())
              .build();

      transacaoHandler.handlePayload(request, id);

      try {
        extratoHandler.handleExtrato(id);
      } catch (SQLException e) {
        e.printStackTrace();
      } catch (Exception e) {
      }
    }
  }
}
