package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.incra.ratpack.handlers.MetricHandler;

/**
 * @author Jeff Risberg
 * @since 02/12/17
 */
public class MetricModule extends AbstractModule {
    protected void configure() {
        bind(MetricHandler.class);
    }
}
