package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class PositionTest {

    @Test
    public void test_commitToDb() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // error: account does not exist
        Position position1 = new Position(jdbc, 0, "NYK", 100);
        assertThrows(SQLException.class, ()->position1.commitToDb()); 

        // success: create account
        Account account = new Account(jdbc, 0, 100);
        assertDoesNotThrow(()->account.commitToDb());

        // error: create position with amount 0
        assertThrows(IllegalArgumentException.class, ()->new Position(jdbc, 0, "NYK", 0));

        // success: create position and commit
        assertDoesNotThrow(()->position1.commitToDb());
        Position position2 = new Position(jdbc, 0, "NYK");
        assertEquals(position1, position1);
        assertEquals(position1, position2);
        assertEquals(position2, position1);

        // success: update positive number
        assertDoesNotThrow(()->new Position(jdbc, 0, "NYK", 10).commitToDb());
        Position position = new Position(jdbc, 0, "NYK");
        assertEquals(110, position.getAmount());

        // success: update negative number
        assertDoesNotThrow(()->new Position(jdbc, 0, "NYK", -20).commitToDb());
        position = new Position(jdbc, 0, "NYK");
        assertEquals(90, position.getAmount());

        // error: update to new amount below 0
        assertThrows(InvalidAlgorithmParameterException.class, ()->new Position(jdbc, 0, "NYK", -90.1).commitToDb());

        // success: update to zero lead to delete
        assertDoesNotThrow(()->new Position(jdbc, 0, "NYK", -90).commitToDb());
        assertThrows(IllegalArgumentException.class, ()->new Position(jdbc, 0, "MYK"));
    }
}
