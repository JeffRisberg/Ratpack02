package com.incra.ratpack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.database.DatabaseItemManager;
import com.incra.ratpack.models.Metric;
import com.incra.ratpack.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.guice.Guice;
import ratpack.hikari.HikariModule;
import ratpack.server.RatpackServer;
import ratpack.server.Service;
import ratpack.server.StartEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static ratpack.jackson.Jackson.json;

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
                                .bindInstance(new Service() {
                                    @Override
                                    public void onStart(StartEvent event) throws Exception {
                                        DataSource dataSource = event.getRegistry().get(DataSource.class);
                                        try (Connection connection = dataSource.getConnection()) {

                                            connection.createStatement()
                                                    .execute("CREATE TABLE USER (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                            "USERNAME VARCHAR(255), " +
                                                            "EMAIL VARCHAR(255)," +
                                                            "DATE_CREATED DATE," +
                                                            "LAST_UPDATED DATE);");
                                            connection.createStatement()
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Luke Daley','luke@gmail.com')");
                                            connection.createStatement()
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Rob Fletch','rob@gmail.com')");
                                            connection.createStatement()
                                                    .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Dan Woods','dan@gmail.com')");

                                            connection.createStatement()
                                                    .execute("CREATE TABLE EVENT (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                            "TYPE VARCHAR(255), " +
                                                            "DETAIL VARCHAR(255)," +
                                                            "DATE_CREATED DATE," +
                                                            "LAST_UPDATED DATE);");

                                            connection.createStatement()
                                                    .execute("CREATE TABLE METRIC (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                                                            "NAME VARCHAR(255), " +
                                                            "DATE_CREATED DATE," +
                                                            "LAST_UPDATED DATE);");
                                        }
                                    }
                                })))
                .handlers(chain -> chain
                        .path("events", ctx -> ctx.byMethod(eventSpec ->
                                eventSpec
                                        .get(() -> ctx.render("events get handler"))
                                        .put(() -> ctx.render("events put handler"))
                                        .post(() -> ctx.render("events post handler"))))

                        .path("users", ctx -> ctx.byMethod(userSpec ->
                                userSpec
                                        .post(() ->
                                                {
                                                    DatabaseItemManager dbManager = DatabaseItemManager.getInstance();
                                                    String username = ctx.getRequest().getQueryParams()
                                                            .getOrDefault("username", "Han Solo");
                                                    String email = ctx.getRequest().getQueryParams()
                                                            .getOrDefault("username", "han@rebels.org");

                                                    Blocking.get(() -> {
                                                        DBTransaction dbTransaction = dbManager.getTransaction();

                                                        dbTransaction.create(new User(username, email));
                                                        dbTransaction.commit();
                                                        return true;
                                                    }).onError(t -> {
                                                        ctx.getResponse().status(400);
                                                        ctx.render(json(getResponseMap(false, t.getMessage())));
                                                    }).then(r ->
                                                            ctx.render(json(getResponseMap(true, null))));
                                                }
                                        )
                                        .get(() ->
                                                {
                                                    DatabaseItemManager dbManager = DatabaseItemManager.getInstance();

                                                    Blocking.get(() -> {
                                                        DBTransaction dbTransaction = dbManager.getTransaction();

                                                        List<User> listUsers = dbTransaction.getObjects(User.class, "Select u from User u", null);

                                                        List<Map<String, String>> userList = Lists.newArrayList();
                                                        for (User user : listUsers) {
                                                            Map<String, String> userAsMap = Maps.newHashMap();
                                                            userAsMap.put("id", "" + user.getId());
                                                            userAsMap.put("username", user.getUsername());
                                                            userAsMap.put("email", user.getEmail());
                                                            userList.add(userAsMap);
                                                        }
                                                        dbTransaction.commit();

                                                        return userList;
                                                    }).then(personList -> ctx.render(json(personList)));
                                                }
                                        )
                        ))

                        .path("metrics", ctx -> ctx.byMethod(metricsSpec ->
                                        metricsSpec
                                                .post(() ->
                                                        {
                                                            DatabaseItemManager dbManager = DatabaseItemManager.getInstance();
                                                            String name = ctx.getRequest().getQueryParams()
                                                                    .getOrDefault("name", "ClickCount");

                                                            Blocking.get(() -> {
                                                                DBTransaction dbTransaction = dbManager.getTransaction();

                                                                dbTransaction.create(new Metric(name));
                                                                dbTransaction.commit();
                                                                return true;
                                                            }).onError(t -> {
                                                                ctx.getResponse().status(400);
                                                                ctx.render(json(getResponseMap(false, t.getMessage())));
                                                            }).then(r ->
                                                                    ctx.render(json(getResponseMap(true, null))));
                                                        }
                                                )
                                                .get(() ->
                                                {
                                                    DatabaseItemManager dbManager = DatabaseItemManager.getInstance();

                                                    Blocking.get(() -> {
                                                        DBTransaction dbTransaction = dbManager.getTransaction();

                                                        List<Metric> listMetrics = dbTransaction.getObjects(Metric.class, "Select m from Metric m", null);

                                                        List<Map<String, String>> metricList = Lists.newArrayList();
                                                        for (Metric metric : listMetrics) {
                                                            Map<String, String> metricAsMap = Maps.newHashMap();
                                                            metricAsMap.put("id", "" + metric.getId());
                                                            metricAsMap.put("name", metric.getName());
                                                            metricList.add(metricAsMap);
                                                        }
                                                        dbTransaction.commit();

                                                        return metricList;
                                                    }).then(metricList -> ctx.render(json(metricList)));
                                                })
                                )
                        )
                )
        );
    }

    private static Map<String, Object> getResponseMap(Boolean status, String message) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("success", status);
        response.put("error", message);
        return response;
    }
}

