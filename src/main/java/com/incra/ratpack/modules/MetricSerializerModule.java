package com.incra.ratpack.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incra.ratpack.models.Metric;

import java.io.IOException;

/**
 * @author Jeff Risberg
 * @since 02/12/17
 */
public class MetricSerializerModule extends SimpleModule {
  private static final String NAME = "MetricSerializerModule";

  public MetricSerializerModule() {
    super(NAME);

    addSerializer(
        Metric.class,
        new JsonSerializer<Metric>() {
          @Override
          public void serialize(
              Metric metric, JsonGenerator jGen, SerializerProvider serializerProvider)
              throws IOException {

            jGen.writeStartObject();
            jGen.writeNumberField("id", metric.getId());
            jGen.writeStringField("name", metric.getName());
            jGen.writeNumberField("value", metric.getValue());
            if (metric.getDateCreated() != null) {
              jGen.writeNumberField("dateCreated", metric.getDateCreated().getTime());
            }
            if (metric.getLastUpdated() != null) {
              jGen.writeNumberField("lastUpdated", metric.getLastUpdated().getTime());
            }
            jGen.writeEndObject();
          }
        });
  }
}
