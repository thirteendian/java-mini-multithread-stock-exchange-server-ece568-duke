package edu.duke.ece568.server;

import java.security.InvalidAlgorithmParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Position {
    private PostgreJDBC jdbc;
    private int accountNumber;
    private String symbol;
    private double amount;

    private void updatePositionFromDB(PostgreJDBC jdbc, int accountNumber, String symbol) throws SQLException{
        String query = 
            "SELECT * FROM POSITION " + 
            "WHERE ACCOUNT_NUMBER=" + accountNumber + " " + 
            "AND SYMBOL=\'" + symbol + "\';";

        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(!resultSet.next()){
            throw new IllegalArgumentException("position not found according to account number and symbol");
        }
        this.accountNumber = resultSet.getInt("ACCOUNT_NUMBER");
        this.symbol = resultSet.getString("SYMBOL");
        this.amount = resultSet.getDouble("AMOUNT");
    }

    /**
     * creation constructor, create a new position in db
     * @param jdbc
     * @param accountNumber
     * @param symbol
     * @param amount
     */
    public Position(PostgreJDBC jdbc, int accountNumber, String symbol, double amount){
        if(amount == 0){
            throw new IllegalArgumentException("cannot create position with amount 0");
        }
        this.jdbc = jdbc;
        this.accountNumber = accountNumber;
        this.symbol = symbol;
        this.amount = amount;
    }

    /**
     * query constructor, fetch an existing position object from db
     * @param jdbc
     * @param accountNumber
     * @param symbol
     * @throws SQLException
     */
    public Position(PostgreJDBC jdbc, int accountNumber, String symbol) throws SQLException{
        this.jdbc = jdbc;
        this.updatePositionFromDB(jdbc, accountNumber, symbol);
    }

    public int getAccountNumber() throws SQLException{
        this.updatePositionFromDB(jdbc, accountNumber, symbol);
        return this.accountNumber;
    }

    public String getSymbol() throws SQLException{
        this.updatePositionFromDB(jdbc, accountNumber, symbol);
        return this.symbol;
    }

    public double getAmount() throws SQLException{
        this.updatePositionFromDB(jdbc, accountNumber, symbol);
        return this.amount;
    }

    public void commitToDb() throws InvalidAlgorithmParameterException, SQLException{
        try{
            Position existingPosition = new Position(this.jdbc, this.accountNumber, this.symbol);
            double newAmount = existingPosition.amount + this.amount;
            if(newAmount < 0){
                throw new InvalidAlgorithmParameterException("cannot update position amount, would be negative");
            }
            else if(newAmount == 0){
                String query = 
                    "DELETE FROM POSITION " + 
                    "WHERE ACCOUNT_NUMBER=" + this.accountNumber + " " + 
                    "AND SYMBOL=\'" + this.symbol + "\';";

                this.jdbc.executeUpdateStatement(query);
            }
            else{
                String query = 
                    "UPDATE POSITION " + 
                    "SET AMOUNT=" + newAmount + " " +
                    "WHERE ACCOUNT_NUMBER=" + this.accountNumber + " " + 
                    "AND SYMBOL=\'" + this.symbol + "\';";

                this.jdbc.executeUpdateStatement(query);
            }        
        }
        catch(IllegalArgumentException e){
            String query = 
                "INSERT INTO POSITION(ACCOUNT_NUMBER, SYMBOL, AMOUNT) " + 
                "VALUES (" + this.accountNumber  + ", \'" + this.symbol + "\', " + this.amount + ");";

            this.jdbc.executeUpdateStatement(query);
        }
    }

    @Override
    public boolean equals(Object o){
        if(o.getClass().equals(this.getClass())){
            Position rhs = (Position)o;
            return this.accountNumber == rhs.accountNumber &&
                this.symbol.equals(rhs.symbol) &&
                this.amount == rhs.amount;
        }
        return false;
    }

    @Override
    public String toString(){
        String output = this.accountNumber + ", " + this.symbol + ", " + this.amount;
        return output;
    }

    @Override
    public int hashCode(){
        return this.toString().hashCode();
    }
}
