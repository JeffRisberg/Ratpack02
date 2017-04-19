package com.incra.ratpack.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * @author Jeff Risberg
 * @since late 2016
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

    public DBSessionFactory(String datasourceName, Context ctx) throws DBException {
        this.datasourceName = datasourceName;

        instantiateSessionFactory(ctx);
    }

    protected void instantiateSessionFactory(Context ctx) throws DBException {
        try {
            DataSource dataSource = ctx.get(DataSource.class);
            Properties jpaProperties = new Properties();

            jpaProperties.put("hibernate.connection.datasource", dataSource);

            emf = Persistence.createEntityManagerFactory(datasourceName, jpaProperties);
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
    public static synchronized DBSessionFactory getInstance(Context ctx) throws DBException {
        try {
            return getInstance("ratpack-jpa", ctx);
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
    public static synchronized DBSessionFactory getInstance(String datasourceName, Context ctx) throws DBException {
        DBSessionFactory sessionFactory = datasources.get(datasourceName);

        if (sessionFactory == null) {
            sessionFactory = setupSessionFactory(datasourceName, ctx);
        }

        return sessionFactory;
    }

    private static DBSessionFactory setupSessionFactory(String datasourceName, Context ctx) throws DBException {
        DBSessionFactory datasource;

        try {
            datasource = new DBSessionFactory(datasourceName, ctx);
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
    public synchronized DBTransaction getTransaction(Context ctx) throws DBException {
        DBTransaction dbTransaction = transaction.get();

        jgLog.trace("Getting JpaTransaction");
        if (dbTransaction == null) {
            dbTransaction = createTransaction(ctx);
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
        DBTransaction dbTransaction = getTransaction(null);

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
    private DBTransaction createTransaction(Context ctx) throws DBException {
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
