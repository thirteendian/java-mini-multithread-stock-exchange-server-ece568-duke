package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5432", "ece568_hw3", "postgresXXXX", "passw0rd"));
        // wrong password
        assertThrows(SQLException.class, () -> new PostgreJDBC("localhost", "5432", "ece568_hw3", "postgres", "asdf"));
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
}
