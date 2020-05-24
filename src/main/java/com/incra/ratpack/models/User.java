package com.incra.ratpack.models;

import com.incra.ratpack.database.DatedDatabaseItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jeff Risberg
 * @since 05/30/16
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends DatedDatabaseItem {
  @Column(name = "username")
  protected String username;

  @Column(name = "email")
  protected String email;
}
