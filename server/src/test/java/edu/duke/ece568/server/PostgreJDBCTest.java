package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class PostgreJDBCTest {

    private PostgreJDBC helper_generateValidJdbc() throws ClassNotFoundException, SQLException{
        return new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
    }

    @Test
    public void test_constructor() throws ClassNotFoundException, SQLException{
        // correct
        assertDoesNotThrow(()->this.helper_generateValidJdbc());
        // wrong host
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhostxxx", "5432", "ece568_hw4", "postgres", "passw0rd"));
        // wrong port
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5433", "ece568_hw4", "postgres", "passw0rd"));
        // wrong database
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5432", "ece568_hw3", "postgres", "passw0rd"));
        // wrong username
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgresXXXX", "passw0rd"));
        // wrong password
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "asdf"));
    }

    @Test
    public void test_createTablesIfNotExist() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        
        String queryDropAllTables = "DROP TABLE IF EXISTS ACCOUNT, POSITION, STOCK_ORDER;";
        String queryDropAllTypes = "DROP TYPE IF EXISTS STATUS;";

        assertTrue(jdbc.executeUpdateStatement(queryDropAllTables));
        assertTrue(jdbc.executeUpdateStatement(queryDropAllTypes));

        // if empty, test creation
        // test creation when existing
        for(int i = 0; i < 2; i++){
            assertTrue(jdbc.createTablesIfNotExist());
        } 
    }

    @Test
    public void test_tryCreateAccount() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();

        int newAccountNum = 0;
        double newAmount = 12.01;
        String queryCleanAccount = "DELETE FROM ACCOUNT;";
        assertTrue(jdbc.executeUpdateStatement(queryCleanAccount));

        // sucess: new record
        assertTrue(jdbc.tryCreateAccount(newAccountNum, newAmount));
        // fail: duplicated account_number
        assertFalse(jdbc.tryCreateAccount(newAccountNum, newAmount));
        // fail: negative account_id
        assertFalse(jdbc.tryCreateAccount(-1, newAmount));
        // fail: negative balance
        assertFalse(jdbc.tryCreateAccount(120, -123.01));
    }

    @Test
    public void test_tryAddPosition() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();

        String queryCleanAllTables = "DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        assertTrue(jdbc.executeUpdateStatement(queryCleanAllTables));

        // error: create symbol when account does not exist
        assertFalse(jdbc.tryUpdateOrCreatePosition(0, "symbol", 12.2));
        
        // success: create new account
        assertTrue(jdbc.tryCreateAccount(0, 10.05));

        // error: create new symbol with amount 0
        assertFalse(jdbc.tryUpdateOrCreatePosition(0, "NYK", 0));

        // error: create new symbol with negative
        assertFalse(jdbc.tryUpdateOrCreatePosition(0, "NYK", -2));

        // success: create new symbol 
        assertTrue(jdbc.tryUpdateOrCreatePosition(0, "NYK", 12.2));
        String queryCheckPositionAmount = "SELECT AMOUNT FROM POSITION WHERE ACCOUNT_NUMBER=0 AND SYMBOL=\'NYK\';";
        ResultSet resultSet = jdbc.executeQueryStatement(queryCheckPositionAmount);
        assertNotNull(resultSet);
        resultSet.next();
        assertEquals(12.2, resultSet.getDouble("AMOUNT"));
        
        // success: add amount for existing symbol
        double expectedAmount = 12.2;
        for(int i = 0; i < 10; i++){
            double increment = (i+1)*12;
            expectedAmount += increment;
            assertTrue(jdbc.tryUpdateOrCreatePosition(0, "NYK", increment));
            queryCheckPositionAmount = "SELECT AMOUNT FROM POSITION WHERE ACCOUNT_NUMBER=0 AND SYMBOL=\'NYK\';";
            resultSet = jdbc.executeQueryStatement(queryCheckPositionAmount);
            assertNotNull(resultSet);
            resultSet.next();
            double actualAmount = resultSet.getDouble("AMOUNT");
            assertEquals(expectedAmount, actualAmount);
        }

        // success: decrease amount
        expectedAmount -= 50;
        assertTrue(jdbc.tryUpdateOrCreatePosition(0, "NYK", -50));
        resultSet = jdbc.executeQueryStatement(queryCheckPositionAmount);
        assertNotNull(resultSet);
        resultSet.next();
        double actualAmount = resultSet.getDouble("AMOUNT");
        assertEquals(expectedAmount, actualAmount);

        // error: decrease below 0
        assertFalse(jdbc.tryUpdateOrCreatePosition(0, "NYK", -622.3));

        // success: decrease to resulting in removal
        assertTrue(jdbc.tryUpdateOrCreatePosition(0, "NYK", -622.2));
        queryCheckPositionAmount = "SELECT AMOUNT FROM POSITION WHERE ACCOUNT_NUMBER=0 AND SYMBOL=\'NYK\';";
        resultSet = jdbc.executeQueryStatement(queryCheckPositionAmount);
        assertFalse(resultSet.next());
    }
}
