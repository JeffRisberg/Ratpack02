package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class DBSessionFactory {
    private static Logger jgLog = LoggerFactory.getLogger(DBSessionFactory.class);

    // List of all the existing instances
    private static Map<String, DBSessionFactory> datasources = new HashMap<String, DBSessionFactory>();

    // Persistence unit name
    protected String datasourceName;

    // The actual database name
    protected String databaseName;

    private EntityManagerFactory emf;

    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();

    private static final ThreadLocal<DBTransaction> transaction = new ThreadLocal<>();

    protected DBSessionFactory() {
        throw new IllegalArgumentException("DBSessionFactory: no argument constructor not allowed");
    }

    public DBSessionFactory(String datasourceName) throws DBException {
        this.datasourceName = datasourceName;

        instantiateSessionFactory();
    }

    protected void instantiateSessionFactory() throws DBException {
        try {
            emf = Persistence.createEntityManagerFactory(datasourceName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    /**
     * Returns the default DBSessionFactory instance.
     *
     * @return DBSessionFactory
     */
    public static synchronized DBSessionFactory getInstance() throws DBException {
        try {
            return getInstance("ratpack-jpa");
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * Obtains the DBSessionFactory instance for the given datasource name.  Check the map for the datasource.
     * Instantiate a new one, if necessary
     *
     * @param datasourceName The datasource name
     * @return DBSessionFactory
     */
    public static synchronized DBSessionFactory getInstance(String datasourceName) throws DBException {
        DBSessionFactory sessionFactory = datasources.get(datasourceName);

        if (sessionFactory == null) {
            sessionFactory = setupSessionFactory(datasourceName);
        }

        return sessionFactory;
    }

    private static DBSessionFactory setupSessionFactory(String datasourceName) throws DBException {
        DBSessionFactory datasource;

        try {
            datasource = new DBSessionFactory(datasourceName);
            datasources.put(datasourceName, datasource);
        } catch (Exception e) {
            throw new DBException(e);
        }

        return datasource;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Returns the thread's DBTransaction. If it is null,
     * create a new one, if it is closed; open.
     *
     * @return DBTransaction
     * @throws DBException on database exception.
     */
    public synchronized DBTransaction getTransaction() throws DBException {
        DBTransaction dbTransaction = transaction.get();

        jgLog.trace("Getting JpaTransaction");
        if (dbTransaction == null) {
            dbTransaction = createTransaction();
            transaction.set(dbTransaction);
        }
        return dbTransaction;
    }

    /**
     * Closes the transaction.  Forces a new transaction on the next call to getTransaction()
     * by closing and setting it to null.
     *
     * @throws DBException when an exception is encountered.
     */
    public synchronized void closeTransaction() throws DBException {
        EntityManager em = entityManager.get();

        jgLog.trace("Closing EntityManager");
        if (em != null) {
            em.close();
            jgLog.trace("Closed EntityManager");
        }

        transaction.set(null);
        entityManager.set(null);
    }

    /**
     * Commits the current transaction.
     *
     * @throws DBException when an exception is encountered.
     */
    public void commitTransaction() throws DBException {
        DBTransaction dbTransaction = getTransaction();

        jgLog.trace("Committing JpaTransaction");
        //TODO - consider checking isActive() and isOpen(), for now minimize differences
        if (dbTransaction != null && dbTransaction.isActive()) {
            jgLog.trace("JpaTransaction isClosed? " + entityManager.get().isOpen());
            jgLog.trace("JpaTransaction isActive? " + dbTransaction.isActive());
            dbTransaction.commit();
        }
    }

    /**
     * Obtains a new JpaTransaction and opens it.
     * <p/>
     * Clients are responsible for closing transactions.
     *
     * @return DBTransaction
     * @throws DBException
     */
    private DBTransaction createTransaction() throws DBException {
        DBTransaction dbTransaction = new DBTransaction(getEntityManager());
        dbTransaction.begin();
        return dbTransaction;
    }

    public EntityManager getEntityManager() throws DBException {
        if (entityManager.get() == null) {
            entityManager.set(createEntityManager());
        }
        return entityManager.get();
    }

    private EntityManager createEntityManager() throws DBException {
        try {
            return emf.createEntityManager();
        } catch (Exception e) {
            jgLog.error(e.getMessage());
            throw new DBException(e);
        }
    }
}
