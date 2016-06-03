package com.incra.ratpack.services;

import com.incra.ratpack.models.User;
import ratpack.exec.Promise;

import java.util.List;

/**
 * @author Jeff Risberg
 * @since 6/1/16
 */
public interface UserService {

    public Promise<List<User>> list();

}
