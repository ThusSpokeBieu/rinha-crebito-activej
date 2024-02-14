package github.mess;

import com.zaxxer.hikari.HikariDataSource;
import github.mess.utils.HikariConfigConverter;
import github.mess.utils.HttpUtils;
import github.mess.utils.RandomUtils;
import io.activej.config.Config;
import io.activej.http.AsyncServlet;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.Reactor;
import io.activej.reactor.nio.NioReactor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

public class App extends HttpServerLauncher {

  public static final String PATH_EXTRATO = "/clientes/:id/extrato";
  public static final String PATH_TRANSACAO = "/clientes/:id/transacoes";

  public boolean IS_WARMING = true;
  public long WARM_UP_TIME = TimeUnit.SECONDS.toMillis(5);

  private DataSource ds;
  private TransacaoHandler transacaoHandler;
  private ExtratoHandler extratoHandler;

  @Provides
  Executor executor() {
    return Executors.newCachedThreadPool((task) -> Thread.ofVirtual().unstarted(task));
  }

  @Provides
  DataSource dataSource(Config config) {
    HikariConfigConverter converter =
        HikariConfigConverter.builder().withAllowMultiQueries().build();

    ds = new HikariDataSource(config.get(converter, "hikari"));
    return ds;
  }

  @Provides
  ExtratoHandler extratoHandler(NioReactor nioReactor, DataSource ds, Executor executor)
      throws SQLException {
    extratoHandler = new ExtratoHandler(nioReactor, ds, executor);
    return extratoHandler;
  }

  @Provides
  TransacaoHandler transacaoHandler(NioReactor nioReactor, DataSource ds, Executor executor)
      throws SQLException {
    transacaoHandler = new TransacaoHandler(nioReactor, ds, executor);
    return transacaoHandler;
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
    try (Connection connection = ds.getConnection()) {
      connection.prepareStatement("SELECT 1").execute();
      connection.prepareStatement("TRUNCATE transacoes").execute();
      connection.prepareStatement("UPDATE clientes SET saldo = 0").execute();
      for (int i = 1; i < 6; i++) {
        extratoHandler.handleExtrato(i);
      }
    } catch (Exception e) {
      e.printStackTrace();
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
