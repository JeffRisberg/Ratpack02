package com.incra.ratpack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.exec.Blocking;
import ratpack.form.Form;
import ratpack.guice.Guice;
import ratpack.hikari.HikariModule;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.Service;
import ratpack.server.StartEvent;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static ratpack.jackson.Jackson.json;

public class Ratpack02 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ratpack02.class);

    public static void main(String[] args) throws Exception {
        RatpackServer.start(spec -> spec
                        .serverConfig(config -> config
                                .baseDir(BaseDir.find())
                                .json("dbconfig.json")
                                .require("/database", DatabaseConfig.class))
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
                                                                    "EMAIL VARCHAR(255));");
                                                    connection.createStatement()
                                                            .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Luke Daley','luke@gmail.com')");
                                                    connection.createStatement()
                                                            .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Rob Fletch','Rob@gmail.com')");
                                                    connection.createStatement()
                                                            .execute("INSERT INTO USER (USERNAME, EMAIL) VALUES('Dan Woods','Dan@gmail.com')");
                                                }
                                            }
                                        })))
                        .handlers(chain -> chain
                                .get(ctx -> {
                                    Blocking.get(() -> {
                                        DataSource dataSource = ctx.get(DataSource.class);
                                        List<Map<String, String>> personList = Lists.newArrayList();
                                        try (Connection connection = dataSource.getConnection()) {
                                            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM USER");
                                            while (rs.next()) {
                                                long id = rs.getLong(1);
                                                String username = rs.getString(2);
                                                String email = rs.getString(3);
                                                Map<String, String> person = Maps.newHashMap();
                                                person.put("id", String.valueOf(id));
                                                person.put("username", username);
                                                person.put("email", email);
                                                personList.add(person);
                                            }
                                        }
                                        return personList;
                                    }).then(personList -> ctx.render(json(personList)));
                                })
                                .post("create", ctx ->
                                                ctx.parse(Form.class).then(f -> {
                                                    String name = f.get("name");
                                                    if (name != null) {
                                                        Blocking.get(() -> {
                                                            DataSource dataSource = ctx.get(DataSource.class);
                                                            try (Connection connection = dataSource.getConnection()) {
                                                                PreparedStatement pstmt = connection.prepareStatement(
                                                                        "INSERT INTO USER (USERNAME,EMAIL) VALUES(?1, 'email@gmail.com');");
                                                                pstmt.setString(1, name);
                                                                pstmt.execute();
                                                            }
                                                            return true;
                                                        }).onError(t -> {
                                                            ctx.getResponse().status(400);
                                                            ctx.render(json(getResponseMap(false, t.getMessage())));
                                                        }).then(r ->
                                                                ctx.render(json(getResponseMap(true, null))));
                                                    } else {
                                                        ctx.getResponse().status(400);
                                                        ctx.render(json(getResponseMap(false, "name not provided")));
                                                    }
                                                })
                                ))
        );
    }

    private static Map<String, Object> getResponseMap(Boolean status, String message) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("success", status);
        response.put("error", message);
        return response;
    }
}

