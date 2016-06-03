package com.incra.ratpack.services;

import com.google.inject.Inject;
import com.incra.ratpack.models.User;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.util.ArrayList;
import java.util.List;

import com.incra.ratpack.models.User;
import org.hibernate.Session;

/**
 * @author Jeff Risberg
 * @since 6/1/16
 */
public class DefaultUserService {

    //private final ExecControl execControl;
    private final Session session;

    @Inject
    DefaultUserService(Session session) {
        //this.execControl = execControl;
        this.session = session;
    }

     /*
    Observable<Long> save(User user) {
        block {
            session.save(user);
        }
    }

    Observable<User> all() {
        block {
            session.createCriteria(User.class).list();
        }
    }

    Observable<User> get(Long id) {
        block {
            session.get(User.class, id);
        }
    }

    private Observable<User> block(Closure clos) {
        observe(
                execControl.blocking(clos));
        .doOnCompleted({
                session.transaction.commit();
        }).doOnError({
                session.transaction.rollback();
        })
    }
    */
}