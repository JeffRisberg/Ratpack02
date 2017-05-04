package com.incra.ratpack.database;

import com.google.inject.Inject;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.hikari.HikariService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

/**
 * The <i>DBService</i> is an extension of HikariService, and it provides JPA-based usage of Hikari.
 * It manages the EntityManagerFactory, and the threadlocal entityManager and transaction.
 *
 * @author Jeff Risberg
 * @since 05/02/17
 */
public class DBService extends HikariService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBService.class);

    protected String persistanceUnitName;

    private EntityManagerFactory emf;

    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<>();

    private static final ThreadLocal<DBTransaction> transaction = new ThreadLocal<>();

    /**
     * Constructor
     */
    @Inject
    public DBService(HikariDataSource dataSource, String persistanceUnitName) throws DBException {
        super(dataSource);

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

        LOGGER.info("Getting JpaTransaction");
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

        LOGGER.trace("Closing EntityManager");
        if (em != null) {
            em.close();
            LOGGER.trace("Closed EntityManager");
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

        LOGGER.trace("Committing JpaTransaction");
        //TODO - consider checking isActive() and isOpen(), for now minimize differences
        if (dbTransaction != null && dbTransaction.isActive()) {
            LOGGER.trace("JpaTransaction isClosed? " + entityManager.get().isOpen());
            LOGGER.trace("JpaTransaction isActive? " + dbTransaction.isActive());
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
            LOGGER.error(e.getMessage());
            throw new DBException(e);
        }
    }
}
