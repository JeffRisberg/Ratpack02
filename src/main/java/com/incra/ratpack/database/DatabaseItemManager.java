package com.incra.ratpack.database;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DatabaseItemManager {
    private static Logger jgLog = LoggerFactory.getLogger(DatabaseItemManager.class);

    static int TRANSACTION = 1;

    private static DatabaseItemManager instance;

    private DBTransaction dbTransaction;
    private int dbConnectionType = TRANSACTION;

    protected DatabaseItemManager(int connectionType) {
        this.dbConnectionType = connectionType;
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
            instance = new DatabaseItemManager(TRANSACTION);
        }
        return instance;
    }

    /**
     * Load a single Object instance of Class T from persistence by its unique id.
     *
     * @param classType Class type to find
     * @param id        unique integer identity
     * @return instance of class type T
     * @throws DBException if id can't be found
     */
    public <T extends DatabaseItem> T find(Class<T> classType, int id) throws DBException {
        return getDatabase().load(classType, id);
    }

    public DBTransaction getDatabase() throws DBException {
        if (dbConnectionType == TRANSACTION) {
            // start a transaction here
            return DBSessionFactory.getInstance().getTransaction();
        } else {
            return dbTransaction;
        }
    }
}
