package com.incra.ratpack.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.database.DatabaseItemManager;
import com.incra.ratpack.models.User;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

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

    private void handleGet(Context ctx) throws Exception {
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
}

