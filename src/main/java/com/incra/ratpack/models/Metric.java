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
 * @since 12/30/16
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Metric extends DatedDatabaseItem {

  @Column(name = "name")
  protected String name;

  @Column(name = "value")
  protected Integer value;
}
