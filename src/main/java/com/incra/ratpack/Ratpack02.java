package com.incra.ratpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBModule;
import com.incra.ratpack.handlers.MetricHandler;
import com.incra.ratpack.handlers.UserHandler;
import com.incra.ratpack.modules.MetricModule;
import com.incra.ratpack.modules.MetricSerializerModule;
import com.incra.ratpack.modules.UserModule;
import com.incra.ratpack.modules.UserSerializerModule;
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
                    DatabaseConfig databaseConfig = serverConfig.get("/database", DatabaseConfig.class);

                    bindingsSpec
                            .add(ObjectMapper.class, new ObjectMapper()
                                    .registerModule(new UserSerializerModule())
                                    .registerModule(new MetricSerializerModule()));

                    bindingsSpec
                            .module(DBModule.class, config -> {
                                config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
                                config.addDataSourceProperty("URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
                                config.setUsername(databaseConfig.getUsername());
                                config.setPassword(databaseConfig.getPassword());
                                config.setPersistanceUnitName(databaseConfig.getPersistanceUnitName());
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