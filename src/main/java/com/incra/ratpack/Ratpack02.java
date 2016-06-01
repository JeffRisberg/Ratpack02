package com.incra.ratpack;

import com.incra.ratpack.models.User;
import com.incra.ratpack.services.DefaultUserService;
import com.incra.ratpack.services.UserService;
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
                .registryOf(r -> r
                                .add(UserService.class, new DefaultUserService())
                )
                .handlers(chain -> chain
                                .get("config", ctx -> {
                                    ctx.render(json(ctx.get(DatabaseConfig.class)));
                                })
                                .get(ctx -> {
                                    UserService userService = ctx.get(UserService.class);
                                    userService.list().then(users -> {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append('[');
                                        for (User user : users) {
                                            sb.append(jsonify(user));
                                        }
                                        sb.append(']');
                                        ctx.getResponse().contentType("application/json");
                                        ctx.render(sb.toString());
                                    });
                                })
                );
    }

    private static String jsonify(User user) { return "{ \"username\": \""
            +user.getUsername()+"\", \"email\": \""
            +user.getEmail()+"\" }";
    }
}
