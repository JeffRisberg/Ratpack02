package com.incra.ratpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import static ratpack.jackson.Jackson.json;

public class Ratpack02 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ratpack02.class);

    public static void main(String[] args) throws Exception {
        RatpackServer.start(spec -> spec
                        .serverConfig(config -> config
                                .baseDir(BaseDir.find())
                                .json("dbconfig.json")
                                .require("/database", DatabaseConfig.class))
                        .handlers(chain -> chain
                                        .get("config", ctx -> {
                                            ctx.render(json(ctx.get(DatabaseConfig.class)));
                                        })
                        )
        );
    }
}
