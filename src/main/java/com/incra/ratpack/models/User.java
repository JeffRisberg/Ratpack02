package com.incra.ratpack.models;

import com.incra.ratpack.database.DatedDatabaseItem;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jeff Risberg
 * @since 05/30/16
 */
@Entity
public class User extends DatedDatabaseItem {
  @Column(name = "username")
  protected String username;

  @Column(name = "email")
  protected String email;

  public User() {}

  public User(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
