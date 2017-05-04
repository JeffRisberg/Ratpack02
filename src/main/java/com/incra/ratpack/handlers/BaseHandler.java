package com.incra.ratpack.handlers;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Jeff Risberg
 * @since 12/13/16
 */
public class BaseHandler {

    protected static Map<String, Object> getResponseMap(Boolean status, String message) {
        Map<String, Object> response = Maps.newHashMap();
        response.put("success", status);
        response.put("error", message);
        return response;
    }
}
