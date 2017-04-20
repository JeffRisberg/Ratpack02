package com.incra.ratpack.database;

import javax.sql.DataSource;

/**
 * Tuple which is used to identify a specific dbSession, which is important when there are
 * multiple databases being used.
 *
 * @author Jeff Risberg
 * @since 02/12/17
 */
public class DBSessionFactoryId {
    private DataSource dataSource;
    private String persistanceUnitName;

    public DBSessionFactoryId(DataSource dataSource, String persistanceUnitName) {
        this.dataSource = dataSource;
        this.persistanceUnitName = persistanceUnitName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getPersistanceUnitName() {
        return persistanceUnitName;
    }
}
