package com.incra.ratpack.models;

import com.incra.ratpack.database.AbstractDatedDatabaseItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Jeff Risberg
 * @since 12/30/16
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Event extends AbstractDatedDatabaseItem {

  @Column(name = "type")
  protected String type; // master key

  @Column(name = "detail")
  protected String detail; // second key
}
