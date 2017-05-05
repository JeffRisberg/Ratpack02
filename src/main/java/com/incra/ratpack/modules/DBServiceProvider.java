package com.incra.ratpack.modules;

import com.google.inject.Provider;
import com.incra.ratpack.config.DatabaseConfig;
import com.incra.ratpack.database.DBConfig;
import com.incra.ratpack.database.DBException;
import com.incra.ratpack.database.DBService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Jeff Risberg
 * @since 05/04/17
 */
public class DBServiceProvider implements Provider<DBService> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBServiceProvider.class);

    private DatabaseConfig databaseConfig;

    public DBServiceProvider(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public DBService get() {
        String persistanceUnitName = databaseConfig.getPersistanceUnitName();

        DBConfig config = new DBConfig();

        String databaseName = databaseConfig.getDatabaseName();
        String url = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1";

        LOGGER.debug("Setting up database at " + url);
        config.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource");
        config.addDataSourceProperty("URL", url);
        config.setUsername(databaseConfig.getUsername());
        config.setPassword(databaseConfig.getPassword());

        HikariDataSource dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {

            connection.createStatement()
                    .execute("CREATE TABLE `USER` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                            "`USERNAME` VARCHAR(255), " +
                            "`EMAIL` VARCHAR(255), " +
                            "DATE_CREATED DATE, " +
                            "LAST_UPDATED DATE);");
            connection.createStatement()
                    .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Luke Daley','luke@gmail.com')");
            connection.createStatement()
                    .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Rob Fletch','rob@gmail.com')");
            connection.createStatement()
                    .execute("INSERT INTO `USER` (USERNAME, EMAIL) VALUES('Dan Woods','dan@gmail.com')");

            connection.createStatement()
                    .execute("CREATE TABLE `EVENT` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                            "`TYPE` VARCHAR(255), " +
                            "`DETAIL` VARCHAR(255), " +
                            "DATE_CREATED DATE, " +
                            "LAST_UPDATED DATE);");

            connection.createStatement()
                    .execute("CREATE TABLE `METRIC` (ID INT PRIMARY KEY AUTO_INCREMENT, " +
                            "`NAME` VARCHAR(255), " +
                            "`VALUE` INTEGER, " +
                            "DATE_CREATED DATE, " +
                            "LAST_UPDATED DATE);");

            connection.createStatement()
                    .execute("INSERT INTO `METRIC` (NAME, VALUE) VALUES('Clicks', 0);");

            LOGGER.debug("Database schema and sample content set up for database " + databaseName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            DBService dbService = new DBService(dataSource, persistanceUnitName);
            return dbService;
        } catch (DBException e) {
            e.printStackTrace();
            return null;
        }
    }
}