package com.incra.ratpack.config;

/**
 * @author Jeff Risberg
 * @since 12/01/16
 */
public class DatabaseConfig {
    protected String server = "localhost";
    protected Integer portNumber = 3000;
    protected String databaseName = "myDB";
    protected String username = "root";
    protected String password;

    public DatabaseConfig() {
    }

    public DatabaseConfig(String server, Integer portNumber, String databaseName, String username, String password) {
        this.server = server;
        this.portNumber = portNumber;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
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

    public String toString() {
        return "DatabaseConfig(server=" + this.server + ", username=" + this.username +
                ", databaseName=" + this.databaseName + ", portNumber=" + this.portNumber + ")";
    }
}