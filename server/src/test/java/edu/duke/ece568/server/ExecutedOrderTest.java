package edu.duke.ece568.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ExecutedOrderTest {
    @Test
    public void test_commitToDb() throws ClassNotFoundException, SQLException, IllegalAccessException{
        PostgreJDBC jdbc = Shared.helper_generateValidJdbc();
        Shared.cleanAllTables(jdbc);

        // error: constructor invalid arguments
        Timestamp time = Timestamp.from(Instant.now());
        assertThrows(IllegalArgumentException.class, ()->new ExecutedOrder(jdbc, 1, -1, "NYK", 100, 100, time));
        assertThrows(IllegalArgumentException.class, ()->new ExecutedOrder(jdbc, 1, 1, "", 100, 100, time));
        assertThrows(IllegalArgumentException.class, ()->new ExecutedOrder(jdbc, 1, 1, null, 100, 100, time));
        assertThrows(IllegalArgumentException.class, ()->new ExecutedOrder(jdbc, 1, 1, null, -1, 100, time));
        assertThrows(IllegalArgumentException.class, ()->new ExecutedOrder(jdbc, 1, 1, null, 100, -1, time));

        // success: create executed order and commit
        ExecutedOrder executedOrder1 = new ExecutedOrder(jdbc, 101, "NYK", 100, 10, time);
        ExecutedOrder executedOrder2 = new ExecutedOrder(jdbc, 101, "AMAZ", 90, 20, time);
        assertDoesNotThrow(()->executedOrder1.commitToDb());
        assertDoesNotThrow(()->executedOrder2.commitToDb());

        // success: query by order_id
        ArrayList<ExecutedOrder> executedOrdersExpected = new ArrayList<>();
        executedOrdersExpected.add(executedOrder1);
        executedOrdersExpected.add(executedOrder2);
        ArrayList<ExecutedOrder> executedOrdersActual = ExecutedOrder.getAllExecutedOrdersByOrderId(jdbc, 101);
     
        assertEquals(executedOrdersExpected.size(), executedOrdersActual.size());
        for(int i = 0; i < executedOrdersExpected.size(); i++){
            assertEquals(executedOrdersExpected.get(i), executedOrdersActual.get(i));
        }
    }
}
