package com.incra.ratpack.services;

import com.incra.ratpack.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff Risberg
 * @since 6/1/16
 */
public class DefaultUserService implements UserService {

    @Override
    public List<User> list() {
        List<User> result = new ArrayList<User>();

        return result;
    }
}
