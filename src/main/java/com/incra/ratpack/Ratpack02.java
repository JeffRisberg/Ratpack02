package com.incra.ratpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBModule;
import com.incra.ratpack.handlers.MetricHandler;
import com.incra.ratpack.handlers.UserHandler;
import com.incra.ratpack.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;
import ratpack.service.Service;
import ratpack.service.StartEvent;

import javax.sql.DataSource;
import java.sql.Connection;

public class Ratpack02 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ratpack02.class);

    public static void main(String[] args) throws Exception {
        RatpackServer.start(spec -> spec
                .serverConfig(ctx -> {
                            ctx.baseDir(BaseDir.find());
                            ctx.json("databaseConfig.json");
                            ctx.require("/database", DatabaseConfig.class);
                        }
                )
                .registry(Guice.registry(bindingsSpec -> {
                    ServerConfig serverConfig = bindingsSpec.getServerConfig();
                    DatabaseConfig databaseConfig1 = serverConfig.get("/database1", DatabaseConfig.class);
                    DatabaseConfig databaseConfig2 = serverConfig.get("/database2", DatabaseConfig.class);

                    bindingsSpec
                            .add(ObjectMapper.class, new ObjectMapper()
                                    .registerModule(new UserSerializerModule())
                                    .registerModule(new MetricSerializerModule()));

                    bindingsSpec
                            .module(PrimaryDatabaseModule.class, config -> {
                                String url = "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1";

                                config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
                                config.addDataSourceProperty("URL", url);
                                config.setUsername(databaseConfig1.getUsername());
                                config.setPassword(databaseConfig1.getPassword());
                                config.setPersistanceUnitName(databaseConfig1.getPersistanceUnitName());
                            })
                            .module(AlternateDatabaseModule.class, config -> {
                                String url = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";

                                config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
                                config.addDataSourceProperty("URL", url);
                                config.setUsername(databaseConfig2.getUsername());
                                config.setPassword(databaseConfig2.getPassword());
                                config.setPersistanceUnitName(databaseConfig2.getPersistanceUnitName());
                            })
                            .module(UserModule.class)
                            .module(UserModule.class)
                            .module(MetricModule.class)

                            .bindInstance(new Service() {
                                @Override
                                public void onStart(StartEvent event) throws Exception {
                                    DataSource dataSource = event.getRegistry().get(DataSource.class);
                                    try (Connection connection = dataSource.getConnection()) {

                                        connection.createStatement()
                                                .execute("CREATE TABLE `USER` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                        "`USERNAME` VARCHAR(255), " +
                                                        "`EMAIL` VARCHAR(255), " +
                                                        "DATE_CREATED DATE, " +
                                                        "LAST_UPDATED DATE);");
                                        connection.createStatement()
                                                .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Luke Daley','luke@gmail.com')");
                                        connection.createStatement()
                                                .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Rob Fletch','rob@gmail.com')");
                                        connection.createStatement()
                                                .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Dan Woods','dan@gmail.com')");

                                        connection.createStatement()
                                                .execute("CREATE TABLE `EVENT` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                        "`TYPE` VARCHAR(255), " +
                                                        "`DETAIL` VARCHAR(255), " +
                                                        "DATE_CREATED DATE, " +
                                                        "LAST_UPDATED DATE);");

                                        connection.createStatement()
                                                .execute("CREATE TABLE `METRIC` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                        "`NAME` VARCHAR(255), " +
                                                        "`VALUE` INTEGER, " +
                                                        "DATE_CREATED DATE, " +
                                                        "LAST_UPDATED DATE);");

                                        connection.createStatement()
                                                .execute("INSERT INTO `METRIC` (NAME, VALUE) VALUES('Clicks', 0);");

                                        LOGGER.debug("Database schema and sample content set up");
                                    }
                                }
                            });
                }))

                .handlers(chain -> chain
                        .path("events", ctx -> ctx.byMethod(eventSpec ->
                                eventSpec
                                        .get(() -> ctx.render("events get handler"))
                                        .put(() -> ctx.render("events put handler"))
                                        .post(() -> ctx.render("events post handler"))))

                        .path("users", UserHandler.class)
                        .path("metrics", MetricHandler.class)

                        .all(ctx -> ctx.render("root handler"))
                )
        );
    }
}