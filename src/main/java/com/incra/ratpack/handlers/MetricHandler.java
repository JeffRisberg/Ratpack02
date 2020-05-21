package com.incra.ratpack.handlers;

import com.incra.ratpack.binding.annotation.DB2;
import com.incra.ratpack.database.DBService;
import com.incra.ratpack.database.DBTransaction;
import com.incra.ratpack.models.Event;
import com.incra.ratpack.models.Metric;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
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
public class MetricHandler extends BaseHandler implements Handler {

  protected DBService dbService;

  @Inject
  public MetricHandler(@DB2 DBService dbService) {
    this.dbService = dbService;
  }

  @Override
  public void handle(Context ctx) throws Exception {
    ctx.byMethod(
        metricSpec -> metricSpec.post(() -> this.handlePost(ctx)).get(() -> this.handleGet(ctx)));
  }

  private void handlePost(Context ctx) throws Exception {
    String name = ctx.getRequest().getQueryParams().getOrDefault("name", "ClickCount");
    String valueStr = ctx.getRequest().getQueryParams().getOrDefault("value", "0");
    Integer value = Integer.parseInt(valueStr);

    Blocking.get(
            () -> {
              DBTransaction dbTransaction = dbService.getTransaction();

              dbTransaction.create(new Metric(name, value));

              dbTransaction.commit();
              dbTransaction.close();

              return true;
            })
        .onError(
            t -> {
              ctx.getResponse().status(400);
              ctx.render(json(getResponseMap(false, t.getMessage())));
            })
        .then(r -> ctx.render(json(getResponseMap(true, null))));
  }

  private void handleGet(Context ctx) throws Exception {
    Blocking.get(
            () -> {
              DBTransaction dbTransaction = dbService.getTransaction();

              List<Metric> metricList =
                  dbTransaction.getObjects(Metric.class, "Select m from Metric m", null);

              Event event = new Event("FETCH", "METRICS");
              dbTransaction.create(event);

              dbTransaction.commit();
              dbTransaction.close();

              return metricList;
            })
        .then(
            metricList -> {
              HikariDataSource hds = dbService.getDataSource();
              HikariPoolMXBean poolMXBean = hds.getHikariPoolMXBean();
              int idleConnections = poolMXBean.getIdleConnections();
              int activeConnections = poolMXBean.getActiveConnections();
              int threadsAwaitingConnections = poolMXBean.getThreadsAwaitingConnection();
              int totalConnections = poolMXBean.getTotalConnections();

              Map response = new HashMap();
              response.put("data", metricList);
              response.put("idleConnections", idleConnections);
              response.put("activeConnections", activeConnections);
              response.put("threadsAwaitingConnections", threadsAwaitingConnections);
              response.put("totalConnections", totalConnections);

              ctx.render(json(response));
            });
  }
}
