package com.incra.ratpack.models;

import com.incra.ratpack.database.DatedDatabaseItem;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jeff Risberg
 * @since 12/30/16
 */
@Entity
public class Metric extends DatedDatabaseItem {

    @Column()
    String name;

    public Metric() {
    }

    public Metric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
