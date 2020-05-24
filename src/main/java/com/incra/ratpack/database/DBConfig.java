package com.incra.ratpack.database;

import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <i>DBConfig</i> represents the internal configuration of the service, which extends the
 * HikariConfig to also have a JPA persistance unit specified.
 *
 * @author Jeff Risberg
 * @since 05/02/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DBConfig extends HikariConfig {

  protected String persistanceUnitName;

  public DBConfig() {
    super();
  }
}
