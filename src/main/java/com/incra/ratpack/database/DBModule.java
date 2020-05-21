package com.incra.ratpack.database;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.ConfigurableModule;

import javax.sql.DataSource;

/**
 * The <i>DBModule</i> is similar to the HikariModule, except that it builds instances of the
 * DBService, which wraps JPA around Hikari.
 *
 * @author Jeff Risberg
 * @since 05/02/17
 */
public class DBModule extends ConfigurableModule<DBConfig> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DBModule.class);

  @Override
  protected void configure() {}

  @Provides
  @Singleton
  public DBService hikariService(DBConfig config) throws DBException {
    return new DBService(new HikariDataSource(config), config.getPersistanceUnitName());
  }

  @Provides
  @Singleton
  public DataSource dataSource(DBService service) {
    return getDataSource(service);
  }

  // separate from above to allow decoration of the datasource by extending the module
  // Guice does not allow overriding @Provides methods
  protected DataSource getDataSource(DBService service) {
    return service.getDataSource();
  }
}
