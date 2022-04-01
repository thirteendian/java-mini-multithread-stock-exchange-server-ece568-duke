package edu.duke.ece568.server;

import java.security.InvalidAlgorithmParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ExecutedOrder {
    private PostgreJDBC jdbc;
    private int archiveId;
    private int orderId;
    private String symbol;
    private double limitPrice;
    private double amount;
    private Timestamp issueTime;

    /**
     * create constructor 
     * @param jdbc
     * @param orderId
     * @param symbol
     * @param limitPrice
     * @param amount
     * @param issueTime
     */
    public ExecutedOrder(PostgreJDBC jdbc, int orderId, String symbol, double limitPrice, double amount, Timestamp issueTime){
        this(jdbc, -1, orderId, symbol, limitPrice, amount, issueTime);
    }

    /**
     * default constructor
     * @param jdbc
     * @param archiveId
     * @param orderId
     * @param symbol
     * @param limitPrice
     * @param amount
     * @param issueTime
     */
    public ExecutedOrder(PostgreJDBC jdbc, int archiveId, int orderId, String symbol, double limitPrice, double amount, Timestamp issueTime){
        if(orderId < 0){
            throw new IllegalArgumentException("archived order cannot have ORDER_ID < 0");
        }
        if(symbol == null || symbol == ""){
            throw new IllegalArgumentException("archived order cannot have empty SYMBOL");
        }
        if(limitPrice <= 0){
            throw new IllegalArgumentException("archived order cannot have LIMIT_PRICE <= 0");
        }

        this.jdbc = jdbc;
        this.archiveId = archiveId;
        this.orderId = orderId;
        this.symbol = symbol;
        this.limitPrice = limitPrice;
        this.amount = amount;
        this.issueTime = issueTime;
    }

    public int getArchiveId(){
        return this.archiveId;
    }

    public double getAmount(){
        return this.amount;
    }

    public double getLimitPrice(){
        return this.limitPrice;
    }

    public Timestamp getIssueTime(){
        return this.issueTime;
    }

    /**
     * commit the newly created archived order to database
     * @throws InvalidAlgorithmParameterException
     * @throws SQLException
     */
    public void commitToDb() throws InvalidAlgorithmParameterException, SQLException{
        if(this.archiveId >= 1){
            throw new InvalidAlgorithmParameterException("the archived order already exists in database, cannot commit again"); 
        }
        String query = 
            "WITH TEMP AS ( " + 
                "INSERT INTO ARCHIVE (ORDER_ID, SYMBOL, AMOUNT, LIMIT_PRICE, ISSUE_TIME)" + 
                "VALUES (" + this.orderId + ", \'" + this.symbol + "\', " + this.amount + ", " + 
                    this.limitPrice + ", \'" + this.issueTime + "\')" + 
                "RETURNING ARCHIVE_ID " + 
            ")" + 
            "SELECT ARCHIVE_ID FROM TEMP";

        ResultSet resultSet = jdbc.executeQueryStatement(query);
        if(!resultSet.next()){
            throw new InvalidAlgorithmParameterException("cannot create an archived order");
        }
        this.archiveId = resultSet.getInt("ARCHIVE_ID");
    }

    /**
     * get all executed orders by ORDER_ID
     * @param jdbc
     * @param orderId
     * @return
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public static ArrayList<ExecutedOrder> getAllExecutedOrdersByOrderId(PostgreJDBC jdbc, int orderId) throws IllegalAccessException, SQLException{
        String query = "SELECT * FROM ARCHIVE WHERE ORDER_ID="+ orderId + ";";
        ArrayList<ExecutedOrder> executedOrders = new ArrayList<>();
        ResultSet resultSet = jdbc.executeQueryStatement(query);
        while(resultSet.next()){
            ExecutedOrder executedOrder = new ExecutedOrder(jdbc, resultSet.getInt("ARCHIVE_ID"), resultSet.getInt("ORDER_ID"), 
                resultSet.getString("SYMBOL"), resultSet.getDouble("LIMIT_PRICE"), 
                resultSet.getDouble("AMOUNT"), resultSet.getTimestamp("ISSUE_TIME"));
            executedOrders.add(executedOrder);
        }
        return executedOrders;
    }

    @Override
    public boolean equals(Object o){
        if(o.getClass().equals(this.getClass())){
            ExecutedOrder rhs = (ExecutedOrder)o;
            return this.archiveId == rhs.archiveId &&
                this.orderId == rhs.orderId &&
                this.symbol.equals(rhs.symbol) &&
                this.limitPrice == rhs.limitPrice &&
                this.amount == rhs.amount &&
                this.issueTime.getTime() == rhs.issueTime.getTime();
        }
        return false;
    }
}
