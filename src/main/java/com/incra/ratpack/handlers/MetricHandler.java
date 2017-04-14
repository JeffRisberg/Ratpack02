package com.incra.ratpack.handlers;

import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.database.DatabaseItemManager;
import com.incra.ratpack.models.Event;
import com.incra.ratpack.models.Metric;
import ratpack.exec.Blocking;
import ratpack.handling.Context;
import ratpack.handling.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ratpack.jackson.Jackson.json;

/**
 * @author Jeff Risberg
 * @since 12/13/16
 */
@Singleton
public class MetricHandler extends BaseHandler implements Handler {

    @Inject
    public MetricHandler() {
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

    private void handleGet(Context ctx) throws Exception {
        DatabaseItemManager dbManager = DatabaseItemManager.getInstance();

        Blocking.get(() -> {
            DBTransaction dbTransaction = dbManager.getTransaction();

            List<Metric> listMetrics = dbTransaction.getObjects(Metric.class, "Select m from Metric m", null);

            List<Map<String, Object>> metricList = listMetrics.stream()
                    .map(m -> m.asMap())
                    .collect(Collectors.toList());

            Event event = new Event("FETCH", "METRICS");
            dbTransaction.create(event);

            dbTransaction.commit();

            return metricList;
        }).then(metricList ->
                ctx.render(json(metricList)));
    }
}