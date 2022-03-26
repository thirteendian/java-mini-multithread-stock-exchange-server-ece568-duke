package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidAlgorithmParameterException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class AccountTest {
    private void cleanAllTables(PostgreJDBC jdbc){
        String queryCleanAllTables = "DELETE FROM ARCHIVE; DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        assertTrue(jdbc.executeUpdateStatement(queryCleanAllTables));
    }

    private PostgreJDBC helper_generateValidJdbc() throws ClassNotFoundException, SQLException{
        return new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
    }

    @Test
    public void test_constructors() throws ClassNotFoundException, SQLException, IllegalAccessException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        this.cleanAllTables(jdbc);

        assertThrows(IllegalArgumentException.class, ()->new Account(jdbc, 0));
        Account account1 = new Account(jdbc, 0, 100);
        assertDoesNotThrow(()->account1.commitToDb());
    }

    @Test
    public void tryAddOrRemoveFromBalance() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        this.cleanAllTables(jdbc);

        Account account1 = new Account(jdbc, 0, 100);
        account1.commitToDb();

        // success: balance = 110
        assertTrue(account1.tryAddOrRemoveFromBalance(10));

        // success: balance = 90
        assertTrue(account1.tryAddOrRemoveFromBalance(-20));

        // error: balance = -0.1
        assertFalse(account1.tryAddOrRemoveFromBalance(-90.1));
    }

    @Test
    public void test_canAffordToBuy() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        this.cleanAllTables(jdbc);

        Account account = new Account(jdbc, 0, 100);
        account.commitToDb();

        // success: cost <= balance
        assertTrue(account.canAffordToBuy(5, 19));
        assertTrue(account.canAffordToBuy(5, 20));

        // error: cost > balance
        assertFalse(account.canAffordToBuy(1, 101));

        // error: amount <= 0
        assertFalse(account.canAffordToBuy(0, 20));
        assertFalse(account.canAffordToBuy(-1, 20));

        // error: limit price <= 0
        assertFalse(account.canAffordToBuy(20, 0));
        assertFalse(account.canAffordToBuy(20, -1));
    }

    @Test
    public void test_hasStockToSell() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        this.cleanAllTables(jdbc);

        Account account = new Account(jdbc, 0, 100);
        account.commitToDb();

        Position position = new Position(jdbc, 0, "NYK", 100);
        position.commitToDb();

        assertTrue(account.hasStockToSell("NYK", 80.5));
        assertTrue(account.hasStockToSell("NYK", 100));
        assertFalse(account.hasStockToSell("NYK", 100.1));
        assertFalse(account.hasStockToSell("AMAZ", 80));
        assertFalse(account.hasStockToSell("AMAZ", 80));
        assertFalse(account.hasStockToSell("AMAZ", 80));
        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell("AMAZ", 0));
        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell("AMAZ", -1));
    }

    @Test
    public void test_equals() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = this.helper_generateValidJdbc();
        this.cleanAllTables(jdbc);

        Account account1 = new Account(jdbc, 0, 100);
        account1.commitToDb();
        Account account2 = new Account(jdbc, 0);
        Account account3 = new Account(jdbc, 1, 100);
        account3.commitToDb();
        String account4 = "100";

        assertEquals(account1, account2);
        assertEquals(account2, account1);
        assertNotEquals(account1, account3);
        assertNotEquals(account1, account4);
    }
}
