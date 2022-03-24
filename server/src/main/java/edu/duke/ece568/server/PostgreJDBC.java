package edu.duke.ece568.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

    protected boolean createTablesIfNotExist() throws SQLException{
        int errorCounter = 0;

        errorCounter += this.createAccountTable() ? 0:1;
        errorCounter += this.createPositionTable() ? 0:1;
        errorCounter += this.createOrderTable() ? 0:1;

        return errorCounter > 0 ? false: true;
    }

    protected boolean executeUpdateStatement(String query){
        try{
            Statement statement = this.conn.createStatement();
            statement.executeUpdate(query);
            statement.close();
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected ResultSet executeQueryStatement(String query){
        try{
            Statement statement = this.conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet;
        }
        catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    protected boolean createAccountTable(){
        String query = 
            "CREATE TABLE IF NOT EXISTS ACCOUNT(" +
                "ACCOUNT_ID SERIAL PRIMARY KEY," + 
                "ACCOUNT_NUMBER INT UNIQUE NOT NULL CHECK (ACCOUNT_NUMBER >= 0)," +
                "BALANCE FLOAT NOT NULL CHECK (BALANCE >= 0)" + 
            ");";

        return this.executeUpdateStatement(query);
    }

    protected boolean createPositionTable(){
        String query = 
            "CREATE TABLE IF NOT EXISTS POSITION("+
                "POSITION_ID SERIAL PRIMARY KEY," + 
                "ACCOUNT_NUMBER INT NOT NULL CHECK (ACCOUNT_NUMBER >= 0)," + 
                "SYMBOL VARCHAR (255) NOT NULL," + 
                "AMOUNT FLOAT NOT NULL CHECK (AMOUNT > 0)," + 

                "UNIQUE (ACCOUNT_NUMBER, SYMBOL)," + 

                "CONSTRAINT FK_ACCOUNT " + 
                    "FOREIGN KEY (ACCOUNT_NUMBER) " + 
                    "REFERENCES ACCOUNT(ACCOUNT_NUMBER) " + 
                    "ON UPDATE CASCADE " + 
                    "ON DELETE SET NULL " + 
            ");";
        
        return this.executeUpdateStatement(query);
    }

    protected boolean createOrderTable(){
        int errorCounter = 0;

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

        errorCounter += this.executeUpdateStatement(enumQuery) ? 0 : 1;
        errorCounter += this.executeUpdateStatement(tableQuery) ? 0 : 1;

        return errorCounter > 0 ? false: true;
    }

    /**
     * create an account
     * @param accountId new account ID
     * @param balance new balance 
     * @return true upon success, false otherwise
     */
    public boolean tryCreateAccount(int accountNumber, double balance){
        String query = 
            "INSERT INTO ACCOUNT(ACCOUNT_NUMBER, BALANCE) " + 
            "VALUES (" + accountNumber + ", " + Double.toString(balance) + ");";

        return this.executeUpdateStatement(query);
    }

    /**
     * for a given account, create a position if not exist
     * update the amount if exist
     * if the updated amount = 0, delete entry from table
     * @param accountNumber existing account number
     * @param symbol new symbol
     * @param amount new amount
     * @return true upon success, false otherwise
     * @throws SQLException
     */
    public boolean tryUpdateOrCreatePosition(int accountNumber, String symbol, double amount) throws SQLException{
        try{
            this.conn.setAutoCommit(false);
            boolean isTransactionSuccessful = this.tryUpdateOrCreatePositionTransaction(accountNumber, symbol, amount);
            this.conn.commit();
            return isTransactionSuccessful;
        }
        catch(SQLException e){
            this.conn.rollback();
            return false;
        }  
    }

    /**
     * transaction implementation for wrapper tryUpdateOrCreatePosition
     * @param accountNumber existing account number
     * @param symbol new symbol
     * @param amount new amount
     * @return true upon success, false otherwise
     * @throws SQLException
     */
    protected boolean tryUpdateOrCreatePositionTransaction(int accountNumber, String symbol, double amount) throws SQLException{
        try{
            if(amount == 0){ // cannot update with an offset of 0
                return false;
            }
            else if(amount < 0){ // if offset < 0
                // getting current amount
                ResultSet resultSet = this.executeQueryStatement(
                    "SELECT AMOUNT FROM POSITION " +
                    "WHERE ACCOUNT_NUMBER=" + accountNumber + " " + 
                    "AND SYMBOL=\'" + symbol + "\' FOR UPDATE;"
                );
                // if current amount exist when trying to decrease, error
                if(resultSet == null || !resultSet.next()){ 
                    return false;
                }
                else{
                    double amountFromDb = resultSet.getDouble("AMOUNT");
                    // if current amount = amount want to remove, delete row
                    if(-1*amount == amountFromDb){ 
                        return this.executeUpdateStatement(
                            "DELETE FROM POSITION " + 
                            "WHERE ACCOUNT_NUMBER=" + accountNumber + " " +
                            "AND SYMBOL=\'" + symbol + "\';"
                        );
                    }
                    else{ // if current amount != amount want to remove, update value
                        return this.executeUpdateStatement( 
                            "UPDATE POSITION SET AMOUNT=AMOUNT+" + amount + " " + 
                            "WHERE ACCOUNT_NUMBER=" + accountNumber + " " + 
                            "AND SYMBOL=\'" + symbol + "\';"
                        );
                    }
                }
            }
            else{ // if offset > 0, create or update amount
                String query = 
                "INSERT INTO POSITION(ACCOUNT_NUMBER, SYMBOL, AMOUNT) " + 
                "VALUES (" + accountNumber  + ", \'" + symbol + "\', " + amount + ") " +
                "ON CONFLICT (ACCOUNT_NUMBER, SYMBOL) DO " + 
                "UPDATE SET AMOUNT=POSITION.AMOUNT+" + amount + " " + 
                "WHERE EXCLUDED.ACCOUNT_NUMBER=" + accountNumber + " " + 
                "AND EXCLUDED.SYMBOL=\'" + symbol + "\';";

                return this.executeUpdateStatement(query);
            }
        }
        catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
