package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeff Risberg
 * @since late 2016
 */
public class DatabaseItemManager {
    private static Logger jgLog = LoggerFactory.getLogger(DatabaseItemManager.class);

    private static DatabaseItemManager instance;

    private DBTransaction dbTransaction;

    protected DatabaseItemManager() {
    }

    /**
     * Defaults to using the standard DBTransaction from
     * DBSessionFactory.getTransaction()
     *
     * @return instance of DatabaseItemManager
     * @throws DBException if DBSessionFactory cannot be accessed
     */
    public static synchronized DatabaseItemManager getInstance() throws DBException {
        if (instance == null) {
            instance = new DatabaseItemManager();
        }
        return instance;
    }

    public DBTransaction getTransaction() throws DBException {
        // start a transaction here
        return DBSessionFactory.getInstance().getTransaction();
    }
}
