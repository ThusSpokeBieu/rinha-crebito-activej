package github.mess;

import github.mess.handlers.ExtratoHandler;
import github.mess.handlers.TransacaoHandler;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import io.activej.reactor.nio.NioReactor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.postgresql.ds.PGSimpleDataSource;
import org.postgresql.jdbc.PreferQueryMode;

public class App extends HttpServerLauncher {
  private static final Pattern PATH_REGEX =
      Pattern.compile("/clientes/([1-5])/(extrato|transacoes)");

  public static final String PATH_EXTRATO = "extrato";
  public static final String PATH_TRANSACAO = "transacoes";

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
  AsyncServlet servlet(
      NioReactor reactor, ExtratoHandler extratoHandler, TransacaoHandler transacaoHandler) {
    return request -> {
      Matcher matcher = PATH_REGEX.matcher(request.getPath());
      boolean isValidPath = matcher.matches();
      if (!isValidPath) return HttpResponse.notFound404().toPromise();

      int id = Integer.parseInt(matcher.group(1));

      return switch (matcher.group(2)) {
        case PATH_EXTRATO -> extratoHandler.handleExtrato(id);
        case PATH_TRANSACAO ->
            request.loadBody().then(buffer -> transacaoHandler.handleTransacao(buffer, id));
        default -> HttpResponse.notFound404().toPromise();
      };
    };
  }

  @Override
  protected void run() throws Exception {
    System.out.println("🚀🚀 agr tô rodano filé 😎 🔥🔥 🚀🚀");
    awaitShutdown();
  }

  public static void main(String[] args) throws Exception {
    System.out.println("calmae meu cumpadi q eu to aqueceno 🥵🥵🥵💦💦");
    Launcher launcher = new App();
    launcher.launch(args);
  }
}
