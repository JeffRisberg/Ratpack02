package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.incra.ratpack.binding.annotation.DB;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBService;
import ratpack.server.ServerConfig;

/**
 * @author Jeff Risberg
 * @since 05/04/17
 */
public class Ratpack02Module extends AbstractModule {

  private ServerConfig serverConfig;
  private DatabaseConfig databaseConfig;

  public Ratpack02Module(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;

    this.databaseConfig = this.serverConfig.get("/database", DatabaseConfig.class);
  }

  @Override
  protected void configure() {
    bind(DBService.class)
        .annotatedWith(DB.class)
        .toProvider(new DBServiceProvider(databaseConfig))
        .in(Scopes.SINGLETON);
  }
}
