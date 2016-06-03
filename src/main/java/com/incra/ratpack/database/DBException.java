package com.incra.ratpack.database;

/**
 */
public class DBException extends Exception {
    public DBException(String message){
        super(message);
    }

    public DBException(Exception e){
        super(e);
    }
}
