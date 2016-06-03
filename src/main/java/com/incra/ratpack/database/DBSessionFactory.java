package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Database access handling class.
 * <p/>
 * Requires that the properties database.name and database.type be set to the
 * name of the database and the database type (i.e., justgive or amex) being used.
 * <p/>
 * The DBSessionFactory is obtained by calling the getInstance() method.  Database
 * interaction itself is handled by getting a DBSession from the DBSessionFactory.
 *
 * @author Curtis
 * @since 2007
 */
public class DBSessionFactory {
    private static Logger jgLog = LoggerFactory.getLogger(DBSessionFactory.class);

    // List of all the existing ones
    private static Map<String, DBSessionFactory> datasources = new HashMap<String, DBSessionFactory>();

    // The name given to the datasource by the application
    // In jpa, this is the persistence unit name
    protected String datasourceName;

    // The actual database name
    protected String databaseName;

    private EntityManagerFactory emf;

    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();

    // Holds the JpaTransaction for the thread
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
            //Properties jpaProperties = new Properties();

            emf = Persistence.createEntityManagerFactory(datasourceName);
       } catch (Exception e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    /**
     * Returns the default DBSessionFactory instance.
     * <p/>
     * Requires that the property database.default.name is set.
     *
     * @return DBSessionFactory
     */
    public static synchronized DBSessionFactory getInstance() throws DBException {
        // Get the default instance
        try {
            return getInstance("ratpack-jpa");
        } catch (Exception e) {
            throw new DBException(e);
        }
    }

    /**
     * Obtains the DBSessionFactory instance for the given datasource name.
     *
     * @param datasourceName The datasource name
     * @return DBSessionFactory
     */
    public static synchronized DBSessionFactory getInstance(String datasourceName) throws DBException {
        // Check the map for the datasource
        // Instantiate a new one, if necessary
        DBSessionFactory sessionFactory = datasources.get(datasourceName);

        if (sessionFactory == null) {
            sessionFactory = setupSessionFactory(datasourceName);
        }

        return sessionFactory;
    }

    private static DBSessionFactory setupSessionFactory(String datasourceName) throws DBException {
        DBSessionFactory datasource;

        // Get the database properties.
        // These properties must be set separately in the calling application.
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

    // JpaSessionFactory methods
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
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
