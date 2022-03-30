package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidAlgorithmParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class StockOrderTest {
    @Test
    public void test_commitToDb() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        Account account = new Account(jdbc, 0, 100);
        assertDoesNotThrow(()->account.commitToDb());
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 100, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());

        ResultSet resultSet = jdbc.executeQueryStatement("SELECT MAX(ORDER_ID) AS ORDER_ID FROM STOCK_ORDER;");
        assertTrue(resultSet.next());
        assertEquals(resultSet.getInt("ORDER_ID"), buyOrder.getOrderId());
    }

    @Test
    public void test_updateOrderAmount() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // success: create account and order
        Account account = new Account(jdbc, 0, 100);
        assertDoesNotThrow(()->account.commitToDb());
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 100, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());

        // success: add amount 
        assertDoesNotThrow(()->buyOrder.updateOrderAmount(10));
        assertEquals(110, buyOrder.getAmount());

        // success: decrease amount
        assertDoesNotThrow(()->buyOrder.updateOrderAmount(-30));
        assertEquals(80, buyOrder.getAmount());
    }

    @Test
    public void test_getTop1SaleOrdersForBuyOrder() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // success: create account and order
        Account account1 = new Account(jdbc, 0, 100);
        Account account2 = new Account(jdbc, 1, 100);
        Account account3 = new Account(jdbc, 2, 100);
        Account account4 = new Account(jdbc, 3, 100);

        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());
        assertDoesNotThrow(()->account4.commitToDb());

        StockOrder buyOrder1 = new StockOrder(jdbc, 0, "NYK", 100, 100);
        StockOrder buyOrder2 = new StockOrder(jdbc, 0, "NYK", 100, 59);

        assertDoesNotThrow(()->buyOrder1.commitToDb());
        StockOrder saleOrder1 = new StockOrder(jdbc, 0, "NYK", -90, 90);
        StockOrder saleOrder2 = new StockOrder(jdbc, 1, "NYK", -90, 60);
        StockOrder saleOrder3 = new StockOrder(jdbc, 2, "NYK", -90, 80);
        StockOrder saleOrder4 = new StockOrder(jdbc, 3, "NYK", -90, 70);
        saleOrder1.commitToDb();
        saleOrder2.commitToDb();
        saleOrder3.commitToDb();
        saleOrder4.commitToDb();

        assertEquals(saleOrder2, buyOrder1.getTop1SaleOrdersForBuyOrder());
        assertNull(buyOrder2.getTop1SaleOrdersForBuyOrder());
    }

    @Test
    public void test_getTop1BuyOrderForSaleOrder() throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
        
        // success: create account and order
        Account account1 = new Account(jdbc, 0, 100);
        Account account2 = new Account(jdbc, 1, 100);
        Account account3 = new Account(jdbc, 2, 100);
        Account account4 = new Account(jdbc, 3, 100);

        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());
        assertDoesNotThrow(()->account4.commitToDb());

        // success: get the order with the highest price, and not from self
        StockOrder saleOrder1 = new StockOrder(jdbc, 0, "NYK", -100, 100);
        StockOrder saleOrder2 = new StockOrder(jdbc, 0, "NYK", -100, 125);

        assertDoesNotThrow(()->saleOrder1.commitToDb());
        assertDoesNotThrow(()->saleOrder2.commitToDb());

        StockOrder buyOrder1 = new StockOrder(jdbc, 0, "NYK", 90, 90);
        StockOrder buyOrder2 = new StockOrder(jdbc, 1, "NYK", 90, 100);
        StockOrder buyOrder3 = new StockOrder(jdbc, 2, "NYK", 90, 120);
        StockOrder buyOrder4 = new StockOrder(jdbc, 3, "NYK", 90, 110);

        buyOrder1.commitToDb();
        buyOrder2.commitToDb();
        buyOrder3.commitToDb();
        buyOrder4.commitToDb();

        assertEquals(buyOrder3, saleOrder1.getTop1BuyOrderForSaleOrder());
        assertNull(saleOrder2.getTop1BuyOrderForSaleOrder());
    }

    @Test
    public void test_matchBuyOrder_amountMoreThanMatched() 
        throws ClassNotFoundException, SQLException, InvalidAlgorithmParameterException{

        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        Account account3 = new Account(jdbc, 2, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());

        // success: buy order amount > sale order amount
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 10, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        StockOrder saleOrder1 = new StockOrder(jdbc, 1, "NYK", -2, 90);
        StockOrder saleOrder2 = new StockOrder(jdbc, 2, "NYK", -3, 80);
        assertDoesNotThrow(()->saleOrder1.commitToDb());
        assertDoesNotThrow(()->saleOrder2.commitToDb());
        assertDoesNotThrow(()->buyOrder.matchBuyOrder());
    }

    @Test
    public void test_matchBuyOrder_amountEqualToMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());

        // success: buy order amount == sale order amount
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 10, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        StockOrder saleOrder = new StockOrder(jdbc, 1, "NYK", -10, 90);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        assertDoesNotThrow(()->buyOrder.matchBuyOrder());
    }

    @Test
    public void test_matchBuyOrder_amountLessThanMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());

        // success: buy order amount < sale order amount
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 2, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        StockOrder saleOrder = new StockOrder(jdbc, 1, "NYK", -10, 90);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        assertDoesNotThrow(()->buyOrder.matchBuyOrder());
    }

    @Test
    public void teset_matchSaleOrder_amountMoreThanMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        Account account3 = new Account(jdbc, 2, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());

        // success: sale order amount > buy order amount
        StockOrder saleOrder = new StockOrder(jdbc, 0, "NYK", -10, 100);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        StockOrder buyOrder1 = new StockOrder(jdbc, 1, "NYK", 2, 110);
        StockOrder buyOrder2 = new StockOrder(jdbc, 2, "NYK", 3, 120);
        assertDoesNotThrow(()->buyOrder1.commitToDb());
        assertDoesNotThrow(()->buyOrder2.commitToDb());
        assertDoesNotThrow(()->saleOrder.matchSaleOrder());
    }

    @Test
    public void teset_matchSaleOrder_amountEqualToMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());

        // success: sale order amount = buy order amount
        StockOrder saleOrder = new StockOrder(jdbc, 0, "NYK", -2, 100);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        StockOrder buyOrder = new StockOrder(jdbc, 1, "NYK", 2, 110);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        assertDoesNotThrow(()->saleOrder.matchSaleOrder());
    }

    @Test
    public void test_matchSaleOrder_amountLessThanMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());

        // success: sale order amount < buy order amount
        StockOrder saleOrder = new StockOrder(jdbc, 0, "NYK", -2, 100);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        StockOrder buyOrder = new StockOrder(jdbc, 1, "NYK", 10, 110);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        assertDoesNotThrow(()->saleOrder.matchSaleOrder());
    }

    @Test
    public void test_cancelOrder() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        assertDoesNotThrow(()->account1.commitToDb());

        // success: cancel a sale order
        StockOrder saleOrder = new StockOrder(jdbc, 0, "NYK", -2, 100);
        assertThrows(InvalidAlgorithmParameterException.class, ()->saleOrder.cancelOrder());
        assertDoesNotThrow(()->saleOrder.commitToDb());
        assertEquals("OPEN", saleOrder.getOrderStatus());
        assertDoesNotThrow(()->saleOrder.cancelOrder());
        assertEquals("CANCELLED", saleOrder.getOrderStatus());
        assertThrows(InvalidAlgorithmParameterException.class, ()->saleOrder.cancelOrder());

        // success: cancel a buy order
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 2, 90);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        assertEquals("OPEN", buyOrder.getOrderStatus());
        assertDoesNotThrow(()->buyOrder.cancelOrder());
        assertEquals("CANCELLED", buyOrder.getOrderStatus());
        assertThrows(InvalidAlgorithmParameterException.class, ()->buyOrder.cancelOrder());
    }

    @Test
    public void teset_matchOrder_buyAmountMoreThanMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        Account account3 = new Account(jdbc, 2, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());

        // success: buy order amount > sale order amount
        StockOrder buyOrder = new StockOrder(jdbc, 0, "NYK", 10, 100);
        assertDoesNotThrow(()->buyOrder.commitToDb());
        StockOrder saleOrder1 = new StockOrder(jdbc, 1, "NYK", -2, 90);
        StockOrder saleOrder2 = new StockOrder(jdbc, 2, "NYK", -3, 80);
        assertDoesNotThrow(()->saleOrder1.commitToDb());
        assertDoesNotThrow(()->saleOrder2.commitToDb());
        assertDoesNotThrow(()->buyOrder.matchOrder());
    }

    @Test
    public void test_matchOrder_saleAmountMoreThanMatched() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);
    
        // success: create accounts
        Account account1 = new Account(jdbc, 0, 1000);
        Account account2 = new Account(jdbc, 1, 1000);
        Account account3 = new Account(jdbc, 2, 1000);
        assertDoesNotThrow(()->account1.commitToDb());
        assertDoesNotThrow(()->account2.commitToDb());
        assertDoesNotThrow(()->account3.commitToDb());

        // success: sale order amount > buy order amount
        StockOrder saleOrder = new StockOrder(jdbc, 0, "NYK", -10, 100);
        assertDoesNotThrow(()->saleOrder.commitToDb());
        StockOrder buyOrder1 = new StockOrder(jdbc, 1, "NYK", 2, 110);
        StockOrder buyOrder2 = new StockOrder(jdbc, 2, "NYK", 3, 120);
        assertDoesNotThrow(()->buyOrder1.commitToDb());
        assertDoesNotThrow(()->buyOrder2.commitToDb());
        assertDoesNotThrow(()->saleOrder.matchOrder());
    }
}
