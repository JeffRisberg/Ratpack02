package com.incra.ratpack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.handlers.MetricHandler;
import com.incra.ratpack.handlers.UserHandler;
import com.incra.ratpack.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.guice.Guice;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

public class Ratpack02 {
  private static final Logger LOGGER = LoggerFactory.getLogger(Ratpack02.class);

  public static void main(String[] args) throws Exception {
    RatpackServer.start(
        spec ->
            spec.serverConfig(
                    ctx -> {
                      ctx.baseDir(BaseDir.find());
                      ctx.json("databaseConfig.json");
                      ctx.require("/database", DatabaseConfig.class);
                    })
                .registry(
                    Guice.registry(
                        bindingsSpec -> {
                          ServerConfig serverConfig = bindingsSpec.getServerConfig();

                          bindingsSpec.add(
                              ObjectMapper.class,
                              new ObjectMapper()
                                  .registerModule(new UserSerializerModule())
                                  .registerModule(new MetricSerializerModule()));

                          bindingsSpec.module(new Ratpack02Module(serverConfig));

                          bindingsSpec.module(UserModule.class).module(MetricModule.class);
                        }))
                .handlers(
                    chain ->
                        chain
                            .path(
                                "events",
                                ctx ->
                                    ctx.byMethod(
                                        eventSpec ->
                                            eventSpec
                                                .get(() -> ctx.render("events get handler"))
                                                .put(() -> ctx.render("events put handler"))
                                                .post(() -> ctx.render("events post handler"))))
                            .path("users", UserHandler.class)
                            .path("metrics", MetricHandler.class)
                            .all(ctx -> ctx.render("root handler"))));
  }
}
