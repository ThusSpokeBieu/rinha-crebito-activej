package github.mess.utils;

import static io.activej.config.Config.ifNotDefault;
import static io.activej.config.converter.ConfigConverters.ofBoolean;
import static io.activej.config.converter.ConfigConverters.ofDurationAsMillis;
import static io.activej.config.converter.ConfigConverters.ofInteger;
import static io.activej.config.converter.ConfigConverters.ofLong;
import static io.activej.config.converter.ConfigConverters.ofString;

import com.zaxxer.hikari.HikariConfig;
import io.activej.common.builder.AbstractBuilder;
import io.activej.config.Config;
import io.activej.config.converter.ComplexConfigConverter;
import java.util.Map;

public final class HikariConfigConverter extends ComplexConfigConverter<HikariConfig> {
  private String poolName;
  private boolean allowMultiQueries;

  private HikariConfigConverter() {
    super(new HikariConfig());
  }

  public static HikariConfigConverter create() {
    return builder().build();
  }

  public static Builder builder() {
    return new HikariConfigConverter().new Builder();
  }

  public final class Builder extends AbstractBuilder<Builder, HikariConfigConverter> {
    private Builder() {}

    public Builder withPoolName(String poolName) {
      checkNotBuilt(this);
      HikariConfigConverter.this.poolName = poolName;
      return this;
    }

    public Builder withAllowMultiQueries() {
      checkNotBuilt(this);
      HikariConfigConverter.this.allowMultiQueries = true;
      return this;
    }

    @Override
    protected HikariConfigConverter doBuild() {
      return HikariConfigConverter.this;
    }
  }

  @Override
  protected HikariConfig provide(Config config, HikariConfig defaultValue) {
    defaultValue.setRegisterMbeans(true);
    defaultValue.setPoolName(poolName);

    HikariConfig hikariConfig = new HikariConfig();
    if (allowMultiQueries) {
      hikariConfig.addDataSourceProperty("allowMultiQueries", "true");
    }

    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
    hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
    hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
    hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
    hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
    hikariConfig.addDataSourceProperty("binaryTransfer", true);
    hikariConfig.addDataSourceProperty(
        "socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
    hikariConfig.addDataSourceProperty("socketFactoryArg", "/var/run/postgresql/.s.PGSQL.5432");

    config.apply(
        ofBoolean(), "autoCommit", defaultValue.isAutoCommit(), hikariConfig::setAutoCommit);
    config.apply(ofString(), "catalog", defaultValue.getCatalog(), hikariConfig::setCatalog);
    config.apply(
        ofString(),
        "connectionInitSql",
        defaultValue.getConnectionInitSql(),
        hikariConfig::setConnectionInitSql);
    config.apply(
        ofString(),
        "connectionTestQuery",
        defaultValue.getConnectionTestQuery(),
        hikariConfig::setConnectionTestQuery);
    config.apply(
        ofDurationAsMillis(),
        "connectionTimeout",
        defaultValue.getConnectionTimeout(),
        hikariConfig::setConnectionTimeout);
    config.apply(
        ofDurationAsMillis(),
        "validationTimeout",
        defaultValue.getValidationTimeout(),
        hikariConfig::setValidationTimeout);
    config.apply(
        ofString(),
        "dataSourceClassName",
        defaultValue.getDataSourceClassName(),
        hikariConfig::setDataSourceClassName);
    config.apply(
        ofString(),
        "driverClassName",
        defaultValue.getDriverClassName(),
        ifNotDefault(hikariConfig::setDriverClassName));
    config.apply(
        ofDurationAsMillis(),
        "idleTimeout",
        defaultValue.getIdleTimeout(),
        hikariConfig::setIdleTimeout);
    config.apply(
        ofLong(),
        "initializationFailTimeout",
        defaultValue.getInitializationFailTimeout(),
        hikariConfig::setInitializationFailTimeout);
    config.apply(
        ofBoolean(),
        "isolateInternalQueries",
        defaultValue.isIsolateInternalQueries(),
        hikariConfig::setIsolateInternalQueries);
    config.apply(ofString(), "jdbcUrl", defaultValue.getJdbcUrl(), hikariConfig::setJdbcUrl);
    config.apply(
        ofDurationAsMillis(),
        "leakDetectionThreshold",
        defaultValue.getLeakDetectionThreshold(),
        hikariConfig::setLeakDetectionThreshold);
    config.apply(
        ofInteger(),
        "maximumPoolSize",
        defaultValue.getMaximumPoolSize(),
        ifNotDefault(hikariConfig::setMaximumPoolSize));
    config.apply(
        ofDurationAsMillis(),
        "maxLifetime",
        defaultValue.getMaxLifetime(),
        hikariConfig::setMaxLifetime);
    config.apply(
        ofInteger(),
        "minimumIdle",
        defaultValue.getMinimumIdle(),
        ifNotDefault(hikariConfig::setMinimumIdle));
    config.apply(ofString(), "password", defaultValue.getPassword(), hikariConfig::setPassword);
    config.apply(ofString(), "poolName", defaultValue.getPoolName(), hikariConfig::setPoolName);
    config.apply(ofBoolean(), "readOnly", defaultValue.isReadOnly(), hikariConfig::setReadOnly);
    config.apply(
        ofBoolean(),
        "registerMbeans",
        defaultValue.isRegisterMbeans(),
        hikariConfig::setRegisterMbeans);
    config.apply(
        ofString(),
        "transactionIsolation",
        defaultValue.getTransactionIsolation(),
        hikariConfig::setTransactionIsolation);
    config.apply(ofString(), "username", defaultValue.getUsername(), hikariConfig::setUsername);
    Config propertiesConfig = config.getChild("extra");
    for (Map.Entry<String, Config> entry : propertiesConfig.getChildren().entrySet()) {
      hikariConfig.addDataSourceProperty(entry.getKey(), entry.getValue().getValue());
    }
    return hikariConfig;
  }
}
