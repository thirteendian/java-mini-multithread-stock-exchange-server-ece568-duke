package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.HashMap;

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

        // if empty, test creation
        // test creation when existing
        for(int i = 0; i < 2; i++){
            assertDoesNotThrow(()->jdbc.createTablesIfNotExist());
        } 
    }

    @Test
    public void test_tryCreateAccount() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();

        int newAccountNum = 0;
        double newAmount = 12.01;
        String queryCleanAccount = "DELETE FROM ACCOUNT WHERE ACCOUNT_NUMBER=" + Integer.toString(newAccountNum); 
        jdbc.executeStatement(queryCleanAccount);

        // sucess: new record
        assertTrue(jdbc.tryCreateAccount(newAccountNum, newAmount));
        // fail: duplicated account_number
        assertFalse(jdbc.tryCreateAccount(newAccountNum, newAmount));
        // fail: negative account_id
        assertFalse(jdbc.tryCreateAccount(-1, newAmount));
        // fail: negative balance
        assertFalse(jdbc.tryCreateAccount(120, -123.01));
    }
}
