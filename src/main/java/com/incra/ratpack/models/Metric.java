package com.incra.ratpack.models;

import com.google.common.collect.Maps;
import com.incra.ratpack.database.DatedDatabaseItem;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Map;

/**
 * @author Jeff Risberg
 * @since 12/30/16
 */
@Entity
public class Metric extends DatedDatabaseItem {

    @Column()
    String name;

    @Column()
    Integer value;

    public Metric() {
    }

    public Metric(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
