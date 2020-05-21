package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.incra.ratpack.binding.annotation.DB1;
import com.incra.ratpack.binding.annotation.DB2;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBService;
import ratpack.server.ServerConfig;

/**
 * @author Jeff Risberg
 * @since 05/04/17
 */
public class Ratpack02Module extends AbstractModule {

  private ServerConfig serverConfig;
  private DatabaseConfig databaseConfig1;
  private DatabaseConfig databaseConfig2;

  public Ratpack02Module(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;

    this.databaseConfig1 = serverConfig.get("/database1", DatabaseConfig.class);
    this.databaseConfig2 = serverConfig.get("/database2", DatabaseConfig.class);
  }

  @Override
  protected void configure() {
    bind(DBService.class)
        .annotatedWith(DB1.class)
        .toProvider(new DBServiceProvider(databaseConfig1))
        .in(Scopes.SINGLETON);
    bind(DBService.class)
        .annotatedWith(DB2.class)
        .toProvider(new DBServiceProvider(databaseConfig2))
        .in(Scopes.SINGLETON);
  }
}
