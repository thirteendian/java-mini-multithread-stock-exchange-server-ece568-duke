package edu.duke.ece568.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreJDBC {
    private Connection conn;

    /**
     * connect to a postgresql server
     * @param psqlServerUrl
     * @param psqlServerPort
     * @param databaseName
     * @param userName
     * @param password
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public PostgreJDBC(String psqlServerUrl, String psqlServerPort, String databaseName, String userName, String password)
        throws SQLException, ClassNotFoundException{
        Class.forName("org.postgresql.Driver");
        this.conn = DriverManager.getConnection(
            "jdbc:postgresql://" + psqlServerUrl + ":" + psqlServerPort + "/" + databaseName,
            userName, 
            password
        );
    }
}
