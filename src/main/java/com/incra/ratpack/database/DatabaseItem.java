package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Abstract database item, which has an id field.
 *
 * @author Jeff Risberg
 * @since late 2016
 */
@MappedSuperclass
public abstract class DatabaseItem implements Serializable {
    private static Logger jgLog = LoggerFactory.getLogger(DatabaseItem.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public DatabaseItem() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isPersisted() {
        return id != null;
    }

    public String toString() {
        return "(" + this.getClass().getSimpleName() + " (" + id + "))";
    }
}
