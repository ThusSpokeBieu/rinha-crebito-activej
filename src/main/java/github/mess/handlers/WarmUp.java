package github.mess.handlers;

import github.mess.utils.RandomUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class WarmUp {

  public static boolean IS_WARMING = true;
  public long WARM_UP_TIME = TimeUnit.SECONDS.toMillis(5);

  private Connection connection;
  private TransacaoHandler transacaoHandler;
  private ExtratoHandler extratoHandler;

  public WarmUp(
      Connection connection, TransacaoHandler transacaoHandler, ExtratoHandler extratoHandler) {
    this.connection = connection;
    this.transacaoHandler = transacaoHandler;
    this.extratoHandler = extratoHandler;
  }

  public void prepare() throws Exception {
    Thread.sleep(2000);
    connection.prepareStatement("SELECT 1").execute();
    connection.prepareStatement("DELETE FROM nome_da_tabela").execute();
    connection.prepareStatement("VACUUM nome_da_tabela").execute();
    connection.prepareStatement("UPDATE clientes SET saldo = 0").execute();

    for (int i = 1; i < 6; i++) {
      extratoHandler.handleExtrato(i);
    }
    connection.prepareStatement(" SELECT pg_prewarm('clientes')").execute();
    connection.prepareStatement(" SELECT pg_prewarm('transacoes')").execute();
    connection.prepareStatement(" SELECT pg_prewarm('idx_clientes')").execute();
    connection.prepareStatement(" SELECT pg_prewarm('idx_transacoes_cliente_id')").execute();
    Thread.sleep(2000);
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
