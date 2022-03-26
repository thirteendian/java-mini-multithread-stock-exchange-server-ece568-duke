package edu.duke.ece568.server;

import java.security.InvalidAlgorithmParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Account {
    private PostgreJDBC jdbc;
    private int accountNumber;
    private double balance;

    private void updateAccountFromDatabase(PostgreJDBC jdbc, int accountNumber) throws SQLException{
        String query = "SELECT * FROM ACCOUNT WHERE ACCOUNT_NUMBER=" + accountNumber + ";";
        ResultSet resultSet = jdbc.executeQueryStatement(query);

        if(!resultSet.next()){
            throw new IllegalArgumentException("account not found according to account number");
        }

        this.accountNumber = resultSet.getInt("ACCOUNT_NUMBER");
        this.balance = resultSet.getDouble("BALANCE");
    }

    /**
     * creation constructor, create a new account object in db
     * @param jdbc
     * @param accountNumber
     * @param balance
     */
    public Account(PostgreJDBC jdbc, int accountNumber, double balance){
        this.jdbc = jdbc;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    /**
     * query constructor, fetch an existing account object from db
     * @param jdbc
     * @param accountNumber
     * @throws SQLException
     */
    public Account(PostgreJDBC jdbc, int accountNumber) throws SQLException{
        this.jdbc = jdbc;
        this.updateAccountFromDatabase(jdbc, accountNumber);
    }

    public void commitToDb() throws InvalidAlgorithmParameterException{
        String query = 
            "INSERT INTO ACCOUNT(ACCOUNT_NUMBER, BALANCE) " + 
            "VALUES (" + accountNumber + ", " + balance + ");";
        
        if(!jdbc.executeUpdateStatement(query)){
            throw new InvalidAlgorithmParameterException("cannot create account, the account number may be occupied");
        }
    }

    /**
     * update balance
     * @param offset
     * @return true if successful, false otherwise
     * @throws SQLException
     */
    public boolean tryAddOrRemoveFromBalance(double offset) throws SQLException{
        this.updateAccountFromDatabase(jdbc, accountNumber);
        if(this.balance + offset < 0){
            return false;
        }
        this.balance += offset;
        String query = 
            "UPDATE ACCOUNT " + 
            "SET BALANCE=" + this.balance + " " + 
            "WHERE ACCOUNT_NUMBER=" + this.accountNumber + ";";
        
        return jdbc.executeUpdateStatement(query);
    }
    
    /**
     * verify if can afford to buy a position, given amount & limit price
     * @param amount
     * @param limitPrice
     * @return
     * @throws SQLException
     */
    public boolean canAffordToBuy(double amount, double limitPrice) throws SQLException{
        this.updateAccountFromDatabase(this.jdbc, this.accountNumber);
        if(amount <= 0 || limitPrice <= 0){
            return false;
        }
        String query = 
            "SELECT BALANCE " + 
            "FROM ACCOUNT " + 
            "WHERE ACCOUNT_NUMBER=" + accountNumber + 
            ";";

        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(resultSet == null || !resultSet.next()){
            return false;
        }
        double actualBalance = resultSet.getDouble("BALANCE");
        double cost = amount * limitPrice;
        return actualBalance >= cost;
    }

    /**
     * verify if has the position to sell
     * @param symbol
     * @param amount
     * @return
     * @throws SQLException
     */
    public boolean hasStockToSell(String symbol, double amount) throws SQLException{
        this.updateAccountFromDatabase(this.jdbc, this.accountNumber);
        if(symbol == null || symbol == ""){
            return false;
        }
        if(amount <= 0){
            throw new IllegalArgumentException("cannot attempt to sell symbol with negative amount");
        }
        String query = 
            "SELECT AMOUNT FROM POSITION " + 
            "WHERE ACCOUNT_NUMBER=" + accountNumber +" " + 
            "AND SYMBOL=\'" + symbol + "\';";
            
        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(resultSet == null || !resultSet.next()){
            return false;
        }
        double actualAmount = resultSet.getDouble("AMOUNT");
        return actualAmount >= amount;
    }

    /**
     * equal operator
     */
    @Override
    public boolean equals(Object o){
        if(o.getClass().equals(this.getClass())){
            Account rhsAccount = (Account)o;
            return this.accountNumber == rhsAccount.accountNumber &&
                this.balance == rhsAccount.balance;
        }
        return false;
    }
}
