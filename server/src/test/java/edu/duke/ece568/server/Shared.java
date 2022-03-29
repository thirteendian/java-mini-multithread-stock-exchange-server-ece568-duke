package edu.duke.ece568.server;

import java.sql.SQLException;

public class Shared {
    public static void cleanAllTables(PostgreJDBC jdbc) throws SQLException{
        String queryCleanAllTables = "DELETE FROM ARCHIVE; DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        jdbc.executeUpdateStatement(queryCleanAllTables);
    }

    public static PostgreJDBC helper_generateValidJdbc() throws ClassNotFoundException, SQLException{
        return new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
    }
}
