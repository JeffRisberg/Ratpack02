package com.incra.ratpack.modules;

import com.google.inject.AbstractModule;
import com.incra.ratpack.handlers.UserHandler;

/**
 * @author Jeff Risberg
 * @since 02/12/17
 */
public class UserModule extends AbstractModule {
    protected void configure() {
        bind(UserHandler.class);
    }
}
