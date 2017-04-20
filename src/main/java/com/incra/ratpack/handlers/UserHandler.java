package com.incra.ratpack.handlers;

import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.database.DatabaseItemManager;
import com.incra.ratpack.models.Event;
import com.incra.ratpack.models.User;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ratpack.jackson.Jackson.json;

/**
 * @author Jeff Risberg
 * @since 12/13/16
 */
@Singleton
public class UserHandler extends BaseHandler implements Handler {

    @Inject
    public UserHandler() {
    }

    @Override
    public void handle(Context ctx) throws Exception {
        ctx.byMethod(metricSpec ->
                metricSpec
                        .post(() -> this.handlePost(ctx))
                        .get(() -> this.handleGet(ctx)));
    }

    private void handlePost(Context ctx) throws Exception {
        DatabaseItemManager dbManager = DatabaseItemManager.getInstance(ctx);
        String username = ctx.getRequest().getQueryParams()
                .getOrDefault("username", "Han Solo");
        String email = ctx.getRequest().getQueryParams()
                .getOrDefault("email", "han@rebels.org");

        Blocking.get(() -> {
            DataSource dataSource = ctx.get(DataSource.class);
            DBTransaction dbTransaction = dbManager.getTransaction(dataSource, persistanceUnitName);

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
        DatabaseItemManager dbManager = DatabaseItemManager.getInstance(ctx);

        Blocking.get(() -> {
            DataSource dataSource = ctx.get(DataSource.class);
            DBTransaction dbTransaction = dbManager.getTransaction(dataSource, persistanceUnitName);

            List<User> listUsers = dbTransaction.getObjects(User.class, "Select u from User u", null);

            List<Map<String, Object>> userList = listUsers.stream()
                    .map(m -> m.asMap())
                    .collect(Collectors.toList());

            Event event = new Event("FETCH", "USERS");
            dbTransaction.create(event);

            dbTransaction.commit();

            return userList;
        }).then(personList ->
                ctx.render(json(personList)));
    }
}

