package com.incra.ratpack.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.incra.ratpack.models.Metric;
import com.incra.ratpack.models.User;

import java.io.IOException;

/**
 * @author Jeff Risberg
 * @since 02/12/17
 */
public class MetricSerializerModule extends SimpleModule {
    private static final String NAME = "MetricSerializerModule";
    private static final VersionUtil VERSION_UTIL = new VersionUtil() {
    };

    public MetricSerializerModule() {
        super(NAME, VERSION_UTIL.version());

        addSerializer(Metric.class, new JsonSerializer<Metric>() {
            @Override
            public void serialize(Metric metric, JsonGenerator jGen, SerializerProvider serializerProvider)
                    throws IOException {

                jGen.writeStartObject();
                jGen.writeNumberField("id", metric.getId());
                jGen.writeStringField("name", metric.getName());
                jGen.writeNumberField("value", metric.getValue());
                jGen.writeNumberField("dateCreated", metric.getDateCreated().getTime());
                jGen.writeNumberField("lastUpdated", metric.getLastUpdated().getTime());
                jGen.writeEndObject();
            }
        });
    }
}