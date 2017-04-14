package com.incra.ratpack.models;

import com.incra.ratpack.database.DatedDatabaseItem;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jeff Risberg
 * @since 12/30/16
 */
@Entity
public class Event extends DatedDatabaseItem {

    @Column()
    String type; // master key

    @Column()
    String detail; // second key

    public Event() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
