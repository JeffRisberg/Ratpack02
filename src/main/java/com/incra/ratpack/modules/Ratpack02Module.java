package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.incra.ratpack.binding.annotation.DB1;
import com.incra.ratpack.binding.annotation.DB2;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBService;
import ratpack.guice.ConfigurableModule;
import ratpack.server.ServerConfig;

/**
 * @author Jeff Risberg
 * @since 05/04/17
 */
public class Ratpack02Module extends ConfigurableModule {

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
        System.out.println("binding DBservices in Ratpack02Module");

        bind(DBService.class).annotatedWith(DB1.class).toProvider(new DBServiceProvider(databaseConfig1));
        bind(DBService.class).annotatedWith(DB2.class).toProvider(new DBServiceProvider(databaseConfig2));
    }
}

