package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jeff Risberg
 * @since late 2016
 */
public class DBSessionFactory {
    private static Logger jgLog = LoggerFactory.getLogger(DBSessionFactory.class);

    // Map of all the existing sessionFactory instances
    private static Map<DBSessionFactoryId, DBSessionFactory> sessionFactoryMap =
            new HashMap<DBSessionFactoryId, DBSessionFactory>();

    protected DataSource dataSource;

    protected String persistanceUnitName;

    private EntityManagerFactory emf;

    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();

    private static final ThreadLocal<DBTransaction> transaction = new ThreadLocal<>();

    protected DBSessionFactory() {
        throw new IllegalArgumentException("DBSessionFactory: no argument constructor not allowed");
    }

    public DBSessionFactory(DataSource dataSource, String persistanceUnitName) throws DBException {
        this.dataSource = dataSource;
        this.persistanceUnitName = persistanceUnitName;

        try {
            Properties jpaProperties = new Properties();
            jpaProperties.put("hibernate.connection.datasource", dataSource);

            emf = Persistence.createEntityManagerFactory(persistanceUnitName, jpaProperties);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    /**
     * Obtains the DBSessionFactory instance for the given datasource name.  Check the map for the datasource.
     * Instantiate a new one, if necessary
     *
     * @param persistanceUnitName The persistanceUnit name
     * @return DBSessionFactory
     */
    public static synchronized DBSessionFactory getInstance(DataSource dataSource, String persistanceUnitName) throws DBException {

        DBSessionFactoryId sessionFactoryId = new DBSessionFactoryId(dataSource, persistanceUnitName);
        DBSessionFactory sessionFactory = sessionFactoryMap.get(sessionFactoryId);

        if (sessionFactory == null) {
            try {
                sessionFactory = new DBSessionFactory(dataSource, persistanceUnitName);
                sessionFactoryMap.put(sessionFactoryId, sessionFactory);
            } catch (Exception e) {
                throw new DBException(e);
            }
        }

        return sessionFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getPersistanceUnitName() {
        return persistanceUnitName;
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
        DBTransaction dbTransaction = new DBTransaction(getEntityManager(), this);
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
