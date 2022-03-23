package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class PostgreJDBCTest {

    @Test
    public void test_constructor() throws ClassNotFoundException, SQLException{
        // correct
        assertDoesNotThrow(()->new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd"));
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
}
