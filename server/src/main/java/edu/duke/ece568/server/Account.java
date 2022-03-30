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

    public void commitToDb() throws InvalidAlgorithmParameterException, SQLException{
        String query = 
            "INSERT INTO ACCOUNT(ACCOUNT_NUMBER, BALANCE) " + 
            "VALUES (" + accountNumber + ", " + balance + ");";
        
        this.jdbc.executeUpdateStatement(query);
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
        
        jdbc.executeUpdateStatement(query);
        return true;
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
            "WHERE ACCOUNT_NUMBER=" + accountNumber + " " + 
            "AND BALANCE >= " + amount * limitPrice;

        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(resultSet == null || !resultSet.next()){
            return false;
        }
        return true;
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
            throw new IllegalArgumentException("cannot attempt to sell symbol == null or empty");
        }
        if(amount <= 0){
            throw new IllegalArgumentException("cannot attempt to sell symbol with negative amount");
        }
        String query = 
            "SELECT AMOUNT FROM POSITION " + 
            "WHERE ACCOUNT_NUMBER=" + accountNumber +" " + 
            "AND SYMBOL=\'" + symbol + "\' " + 
            "AND AMOUNT >= " + amount + ";";
            
        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(resultSet == null || !resultSet.next()){
            return false;
        }
        return true;
    }

    /**
     * place a buy or sale order
     * to create a buy order:
     *  1. check balance
     *  2. deduct from balance
     * to create a sale order:
     *  1. check stock
     *  2. deduct position
     * 
     * @param symbol
     * @param amount
     * @param limitPrice
     * @throws InvalidAlgorithmParameterException
     * @throws SQLException
     */
    public int placeOrder(String symbol, double amount, double limitPrice) throws InvalidAlgorithmParameterException, SQLException{
        if(symbol == null || symbol == ""){
            throw new IllegalArgumentException("cannot create order with symbol == null or empty");
        }
        if(amount == 0){
            throw new IllegalArgumentException("cannot create order with amount = 0");
        }
        if(limitPrice <= 0){
            throw new IllegalArgumentException("cannot create order with limit price <= 0");
        }
        else if(amount > 0){ // buy order
            if(!this.canAffordToBuy(amount, limitPrice)){
                throw new InvalidAlgorithmParameterException(
                    "this account does not have enough balance to place this buy order");
            }
            this.tryAddOrRemoveFromBalance(-1*(amount*limitPrice));
            StockOrder newOrder = new StockOrder(this.jdbc, this.accountNumber, symbol, amount, limitPrice);
            newOrder.commitToDb();
            // newOrder.matchOrder();
            return newOrder.getOrderId();
        }
        else{
            if(!this.hasStockToSell(symbol, Math.abs(amount))){
                throw new InvalidAlgorithmParameterException(
                    "this account does not have enough "  + symbol + " stock to place this sale order");
            }
            Position newPosition = new Position(this.jdbc, this.accountNumber, symbol, amount);
            newPosition.commitToDb();
            StockOrder newOrder = new StockOrder(this.jdbc, this.accountNumber, symbol, amount, limitPrice);
            newOrder.commitToDb();
            // newOrder.matchOrder();
            return newOrder.getOrderId();
        }
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
