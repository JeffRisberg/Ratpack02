package com.incra.ratpack.services;

import com.google.inject.Inject;
import com.incra.ratpack.models.User;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jeff Risberg
 * @since 6/1/16
 */
public class DefaultUserService implements UserService {
    public Promise<List<User>> list() {
        //Blocking.get {
        //    dao.listUsers();
        //}
        return null;
    }
}
