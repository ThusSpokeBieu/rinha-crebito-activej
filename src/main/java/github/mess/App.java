package github.mess;

import com.dslplatform.json.DslJson;
import github.mess.utils.HttpUtils;
import github.mess.utils.RandomUtils;
import io.activej.config.Config;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Eager;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PreferQueryMode;

public class App extends HttpServerLauncher {

  public static final String PATH_EXTRATO = "/clientes/:id/extrato";
  public static final String PATH_TRANSACAO = "/clientes/:id/transacoes";

  public boolean IS_WARMING = true;
  public long WARM_UP_TIME = TimeUnit.SECONDS.toMillis(5);

  private Connection connection;
  private TransacaoHandler transacaoHandler;
  private ExtratoHandler extratoHandler;

  @Provides
  DslJson<Object> dslJson() {
    return new DslJson<>();
  }

  @Provides
  @Eager
  Connection dataSourcePg(Config config) throws SQLException, IOException {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setDatabaseName("crebito");
    dataSource.setUser(config.get("pg.user", "rinha"));
    dataSource.setPassword(config.get("pg.password", "rinha"));
    dataSource.setBinaryTransfer(true);
    dataSource.setPreparedStatementCacheQueries(1024);
    dataSource.setPrepareThreshold(1);
    dataSource.setPreparedStatementCacheSizeMiB(240);
    dataSource.setPreferQueryMode(PreferQueryMode.EXTENDED_CACHE_EVERYTHING);
    dataSource.setTcpKeepAlive(true);
    dataSource.setSocketFactory("org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
    dataSource.setSocketFactoryArg("/var/run/postgresql/.s.PGSQL.5432");
    dataSource.setMaxResultBuffer("100000");

    connection = dataSource.getConnection();
    return Objects.requireNonNull(connection, "conexão nula com o banco, pq?");
  }

  @Provides
  ExtratoHandler extratoHandler(Connection connection) throws SQLException {
    extratoHandler = new ExtratoHandler(connection);
    return Objects.requireNonNull(extratoHandler, "extrato handler n inicializou corretamente");
  }

  @Provides
  TransacaoHandler transacaoHandler(final DslJson<Object> dslJson, Connection connection)
      throws SQLException {
    transacaoHandler = new TransacaoHandler(dslJson, connection);
    return Objects.requireNonNull(transacaoHandler, "transacao handler n inicializou corretamente");
  }

  @Provides
  AsyncServlet servlet(
      Reactor reactor, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler) {
    return RoutingServlet.builder(reactor)
        .with(PATH_EXTRATO, extratoHandler::handleRequest)
        .with(PATH_TRANSACAO, transacaoHandler::handleRequest)
        .with("/health-check", request -> IS_WARMING ? HttpUtils.isWarming() : HttpUtils.isOK())
        .build();
  }

  @Override
  protected void run() throws Exception {
    warmUp();
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

  public static void main(String[] args) throws Exception {
    System.out.println("calmae meu cumpadi q eu to aqueceno 🥵🥵🥵💦💦");
    Launcher launcher = new App();
    launcher.launch(args);
  }

  public void warmUp() {

    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < WARM_UP_TIME) {
      int id = RandomUtils.generateRandomPositiveInt(5);
      transacaoHandler.handlePayload(RandomUtils.generateRandomRequest(), id);
      transacaoHandler.handlePayload(RandomUtils.GenerateNotRandomReqD(), id);
      transacaoHandler.handlePayload(RandomUtils.generateNotRandomReqC(), id);
      transacaoHandler.handlePayload(RandomUtils.GenerateNotRandomReqD(), 1);
      transacaoHandler.handlePayload(RandomUtils.generateNotRandomReqC(), 1);
      transacaoHandler.handlePayload(RandomUtils.generateOtherNotRandomReqC(), 1);
      transacaoHandler.handlePayload(RandomUtils.generateOtherNotRandomReqD(), 1);

      try {
        extratoHandler.handleExtrato(id);
        extratoHandler.handleExtrato(id);
      } catch (SQLException e) {
        e.printStackTrace();
      } catch (Exception e) {
      }
    }
  }
}
