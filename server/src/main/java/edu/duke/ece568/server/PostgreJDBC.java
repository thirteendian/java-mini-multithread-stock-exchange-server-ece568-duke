package edu.duke.ece568.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

    protected void createTablesIfNotExist() throws SQLException{
        this.createAccountTable();
        this.createPositionTable();
        this.createOrderTable();
    }

    protected void executeStatement(String query) throws SQLException{
        Statement statement = this.conn.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    protected void createAccountTable() throws SQLException{
        String query = 
            "CREATE TABLE IF NOT EXISTS ACCOUNT(" +
                "ACCOUNT_ID SERIAL PRIMARY KEY," + 
                "ACCOUNT_NUMBER INT UNIQUE NOT NULL CHECK (ACCOUNT_NUMBER >= 0)," +
                "BALANCE FLOAT NOT NULL CHECK (BALANCE >= 0)" + 
            ");";

        this.executeStatement(query);
    }

    protected void createPositionTable() throws SQLException{
        String query = 
            "CREATE TABLE IF NOT EXISTS POSITION("+
                "POSITION_ID SERIAL PRIMARY KEY," + 
                "ACCOUNT_NUMBER INT NOT NULL CHECK (ACCOUNT_NUMBER >= 0)," + 
                "SYMBOL VARCHAR (255) NOT NULL," + 
                "AMOUNT FLOAT NOT NULL CHECK (AMOUNT > 0)," + 

                "CONSTRAINT FK_ACCOUNT " + 
                    "FOREIGN KEY (ACCOUNT_NUMBER) " + 
                    "REFERENCES ACCOUNT(ACCOUNT_NUMBER) " + 
                    "ON UPDATE CASCADE " + 
                    "ON DELETE SET NULL " + 
            ");";
        
        this.executeStatement(query);
    }

    protected void createOrderTable() throws SQLException{
        String enumQuery = 
            "DO $$ BEGIN "+
                "CREATE TYPE STATUS AS ENUM ('OPEN', 'EXECUTED', 'CANCELLED');"+
                "EXCEPTION WHEN duplicate_object THEN null;" + 
            "END $$"; 

        String tableQuery = 
            "CREATE TABLE IF NOT EXISTS STOCK_ORDER(" + 
                "ORDER_ID SERIAL PRIMARY KEY," + 
                "ACCOUNT_NUMBER INT NOT NULL CHECK (ACCOUNT_NUMBER >= 0)," + 
                "SYMBOL VARCHAR (255) NOT NULL," + 
                "AMOUNT FLOAT NOT NULL," + 
                "ISSUE_TIME TIMESTAMP NOT NULL," + 
                "ORDER_STATUS STATUS NOT NULL," + 

                "CONSTRAINT FK_ACCOUNT " + 
                    "FOREIGN KEY (ACCOUNT_NUMBER) " + 
                    "REFERENCES ACCOUNT(ACCOUNT_NUMBER) " + 
                    "ON UPDATE CASCADE " + 
                    "ON DELETE SET NULL " + 
            ");";

        this.executeStatement(enumQuery);
        this.executeStatement(tableQuery);
    }
}
