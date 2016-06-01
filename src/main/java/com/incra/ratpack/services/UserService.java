package com.incra.ratpack.services;

import com.incra.ratpack.models.User;

import java.util.List;

/**
 * @author Jeff Risberg
 * @since 6/1/16
 */
public interface UserService {

    public List<User> list();

}
