package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.incra.ratpack.database.DBModule;

/**
 * @author Jeff Risberg
 * @since 05/04/17
 */
public class AlternateDatabaseModule extends DBModule {

    @Override
    protected void configure() {
        // Bind Groovy Sql to registry but annotated as DB2,
        // so we can have two Sql instances in the registry.

        //bind(Sql)
        //        .annotatedWith(DB2)
        //        .toProvider(LocationSqlProvider)
        //        .in(Scopes.SINGLETON);
        String url = "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1";

        //config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        //config.addDataSourceProperty("URL", url);
        //config.setUsername(databaseConfig2.getUsername());
        //config.setPassword(databaseConfig2.getPassword());
        //config.setPersistanceUnitName(databaseConfig2.getPersistanceUnitName());
    }
}

