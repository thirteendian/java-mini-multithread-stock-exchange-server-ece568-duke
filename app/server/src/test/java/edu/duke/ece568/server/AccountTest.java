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
    private void cleanAllTables(PostgreJDBC jdbc) throws SQLException{
        String queryCleanAllTables = "DELETE FROM ARCHIVE; DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        jdbc.executeUpdateStatement(queryCleanAllTables);
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
        assertThrows(SQLException.class, ()->account1.commitToDb());
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

        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell("", 10));
        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell(null, 10));
        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell("AMAZ", 0));
        assertThrows(IllegalArgumentException.class, ()->account.hasStockToSell("AMAZ", -1));
    }

    @Test
    public void test_placeOrder_error() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // success: create account and some positions
        Account account = new Account(jdbc, 0, 1000);
        assertDoesNotThrow(()->account.commitToDb());
        Position position = new Position(jdbc, 0, "NYK", 10);
        assertDoesNotThrow(()->position.commitToDb());

        // error: place an order with symbol = null or ""
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder("", 10, 10));
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder(null, 10, 10));

        // error: place an order with amount = 0
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder("NYK", 0, 10));

        // error: place an order with limit price <= 0
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder("NYK", 10, 0));
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder("NYK", 10, -10));
        assertThrows(IllegalArgumentException.class, ()->account.placeOrder("NYK", -10, -10));

        // error: place buy order without enough balance
        assertThrows(InvalidAlgorithmParameterException.class, ()->account.placeOrder("NYK", 10, 101));

        // error: place sale order without enough stock
        assertThrows(InvalidAlgorithmParameterException.class, ()->account.placeOrder("NYK", -11, 90));
        assertThrows(InvalidAlgorithmParameterException.class, ()->account.placeOrder("AMAZ", -1, 90));
    }

    @Test
    public void test_placeOrder_success() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // success: create account and some positions
        Account account = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        assertDoesNotThrow(()->account.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());

        Position position = new Position(jdbc, 0, "NYK", 10);
        assertDoesNotThrow(()->position.commitToDb());

        assertDoesNotThrow(()->account2.placeOrder("NYK", 5, 30));

        // success: place a buy order
        assertDoesNotThrow(()->account.placeOrder("NYK", 10, 12));

        // success: place a sale order
        assertDoesNotThrow(()->account.placeOrder("NYK", -2, 20));
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
