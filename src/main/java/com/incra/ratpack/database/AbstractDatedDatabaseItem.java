package com.incra.ratpack.database;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Calendar;
import java.util.Date;

/**
 * Abstract database item with dateCreated and lastUpdated fields.
 *
 * @author Jeff Risberg
 * @since late 2016
 */
@MappedSuperclass
public abstract class AbstractDatedDatabaseItem extends AbstractDatabaseItem {

  @Column(name = "date_created", columnDefinition = "timestamp not null default now()")
  protected Date dateCreated = new Date();

  @Column(name = "last_updated", columnDefinition = "timestamp default now()")
  protected Date lastUpdated = new Date();

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date createdDate) {
    if (createdDate == null) {
      createdDate = Calendar.getInstance().getTime();
    }

    this.dateCreated = createdDate;
  }

  public void setDateCreated() {
    this.dateCreated = Calendar.getInstance().getTime();
    this.lastUpdated = Calendar.getInstance().getTime();
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date modifiedDate) {
    if (modifiedDate == null) {
      modifiedDate = Calendar.getInstance().getTime();
    }

    this.lastUpdated = modifiedDate;
  }

  public void setLastUpdated() {
    this.lastUpdated = Calendar.getInstance().getTime();
  }
}
