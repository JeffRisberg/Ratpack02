package com.incra.ratpack.config;

/**
 * The <i>DatabaseConfig</i> class is the external version of the database configuration information.
 * This is instantiated and loaded by the Ratpack configuration tools.
 *
 * The 'encrypted' field is an indicator that the username and password are provided in encrypted form.
 *
 * @author Jeff Risberg
 * @since 12/01/16
 */
public class DatabaseConfig {
    protected Boolean encrypted = false;
    protected String server = "localhost";
    protected Integer portNumber = 3000;
    protected String databaseName = "myDB";
    protected String username = "root";
    protected String password;
    protected String persistanceUnitName = "ratpack-jpa";

    public DatabaseConfig() {
    }

    public DatabaseConfig(Boolean encrypted, String server, Integer portNumber,
                          String databaseName, String username, String password,
                          String persistanceUnitName) {
        this.encrypted = encrypted;
        this.server = server;
        this.portNumber = portNumber;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.persistanceUnitName = persistanceUnitName;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public String getServer() {
        return server;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPersistanceUnitName() {
        return persistanceUnitName;
    }

    public String toString() {
        return "DatabaseConfig(server=" + this.server +
                ", portNumber=" + this.portNumber +
                ", databaseName=" + this.databaseName +
                ", puName=" + this.persistanceUnitName +
                ", username=" + this.username +
                ")";
    }
}