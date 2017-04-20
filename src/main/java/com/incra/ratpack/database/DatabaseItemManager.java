package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

import javax.sql.DataSource;

/**
 * @author Jeff Risberg
 * @since late 2016
 */
public class DatabaseItemManager {
    private static Logger jgLog = LoggerFactory.getLogger(DatabaseItemManager.class);

    private static DatabaseItemManager instance;

    private Context ctx;

    protected DatabaseItemManager(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Defaults to using the standard DBTransaction from
     * DBSessionFactory.getTransaction()
     *
     * @return instance of DatabaseItemManager
     * @throws DBException if DBSessionFactory cannot be accessed
     */
    public static synchronized DatabaseItemManager getInstance(Context ctx) throws DBException {
        if (instance == null) {
            instance = new DatabaseItemManager(ctx);
            jgLog.debug("Created a DatabaseItemManager");
        }
        return instance;
    }

    /**
     * Set up a DBSessionFactory for this pair of (dataSource, persistanceUnitName) if not already
     * created, and then get a transaction from it.
     */
    public DBTransaction getTransaction(DataSource dataSource, String persistanceUnitName) throws DBException {
        DBSessionFactory sessionFactory = DBSessionFactory.getInstance(dataSource, persistanceUnitName);

        return sessionFactory.getTransaction();
    }
}
