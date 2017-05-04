package com.incra.ratpack.handlers;

import com.incra.ratpack.database.DBService;
import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.models.Event;
import com.incra.ratpack.models.User;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ratpack.jackson.Jackson.json;

/**
 * @author Jeff Risberg
 * @since 12/13/16
 */
@Singleton
public class UserHandler extends BaseHandler implements Handler {

    protected DBService dbService;

    @Inject
    public UserHandler(DBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.byMethod(metricSpec ->
                metricSpec
                        .post(() -> this.handlePost(ctx))
                        .get(() -> this.handleGet(ctx)));
    }

    private void handlePost(Context ctx) throws Exception {
        String username = ctx.getRequest().getQueryParams()
                .getOrDefault("username", "Han Solo");
        String email = ctx.getRequest().getQueryParams()
                .getOrDefault("email", "han@rebels.org");

        Blocking.get(() -> {
            DBTransaction dbTransaction = dbService.getTransaction();

            dbTransaction.create(new User(username, email));
            dbTransaction.commit();
            return true;
        }).onError(t -> {
            ctx.getResponse().status(400);
            ctx.render(json(getResponseMap(false, t.getMessage())));
        }).then(r ->
                ctx.render(json(getResponseMap(true, null))));
    }

    private void handleGet(Context ctx) throws Exception {

        Blocking.get(() -> {
            DBTransaction dbTransaction = dbService.getTransaction();

            List<User> userList = dbTransaction.getObjects(User.class, "Select u from User u", null);

            Event event = new Event("FETCH", "USERS");
            dbTransaction.create(event);

            dbTransaction.commit();

            return userList;
        }).then(userList -> {
            Map response = new HashMap();
            response.put("data", userList);
            ctx.render(json(response));
        });
    }
}

