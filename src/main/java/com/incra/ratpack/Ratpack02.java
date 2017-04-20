package com.incra.ratpack;

import com.incra.ratpack.handlers.MetricHandler;
import com.incra.ratpack.handlers.UserHandler;
import com.incra.ratpack.modules.MetricModule;
import com.incra.ratpack.modules.UserModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.Guice;
import ratpack.hikari.HikariModule;
import ratpack.server.RatpackServer;
import ratpack.server.Service;
import ratpack.server.StartEvent;

import javax.sql.DataSource;
import java.sql.Connection;

public class Ratpack02 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ratpack02.class);

    public static void main(String[] args) throws Exception {
        RatpackServer.start(spec -> spec
                .registry(Guice.registry(bindingsSpec ->
                        bindingsSpec
                                .module(HikariModule.class, c -> {
                                    c.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
                                    c.addDataSourceProperty("URL", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
                                    c.setUsername("sa");
                                    c.setPassword("");
                                })
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
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Luke Daley','luke@gmail.com')");
                                            connection.createStatement()
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Rob Fletch','rob@gmail.com')");
                                            connection.createStatement()
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Dan Woods','dan@gmail.com')");

                                            connection.createStatement()
                                                    .execute("CREATE TABLE `EVENT` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                            "`TYPE` VARCHAR(255), " +
                                                            "`DETAIL` VARCHAR(255), " +
                                                            "DATE_CREATED DATE, " +
                                                            "LAST_UPDATED DATE);");

                                            connection.createStatement()
                                                    .execute("CREATE TABLE `METRIC` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                            "`NAME` VARCHAR(255), " +
                                                            "DATE_CREATED DATE, " +
                                                            "LAST_UPDATED DATE);");
                                            LOGGER.debug("Database schema and sample content set up");
                                        }
                                    }
                                })))
                .handlers(chain -> chain
                        .path("events", ctx -> ctx.byMethod(eventSpec ->
                                eventSpec
                                        .get(() -> ctx.render("events get handler"))
                                        .put(() -> ctx.render("events put handler"))
                                        .post(() -> ctx.render("events post handler"))))

                        .path("users", UserHandler.class)
                        .path("metrics", MetricHandler.class)
                )
        );
    }
}