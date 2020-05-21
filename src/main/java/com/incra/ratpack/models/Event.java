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
public class Event extends DatedDatabaseItem {

  @Column() String type; // master key

  @Column() String detail; // second key

  public Event() {}

  public Event(String type, String detail) {
    this.type = type;
    this.detail = detail;
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

  public Map<String, Object> asMap() {
    Map<String, Object> result = Maps.newHashMap();

    result.put("id", getId());
    result.put("type", getType());
    result.put("detail", getDetail());

    return result;
  }
}
