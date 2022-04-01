package edu.duke.ece568.server;

import java.sql.SQLException;

public class Shared {
    public static void cleanAllTables(PostgreJDBC jdbc) throws SQLException, ClassNotFoundException{
        String queryCleanAllTables = "DELETE FROM ARCHIVE; DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        jdbc.executeUpdateStatement(queryCleanAllTables);
    }

    public static PostgreJDBC helper_generateValidJdbc() throws ClassNotFoundException, SQLException{
        return new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
    }

    public static void dropAndCreateAllTables() throws ClassNotFoundException, SQLException{
        PostgreJDBC jdbc = helper_generateValidJdbc();

        String queryDropAllTables = "DROP TABLE IF EXISTS ACCOUNT, POSITION, STOCK_ORDER, ARCHIVE;";
        String queryDropAllTypes = "DROP TYPE IF EXISTS STATUS;";

        jdbc.executeUpdateStatement(queryDropAllTables);
        jdbc.executeUpdateStatement(queryDropAllTypes);

        jdbc.createTablesIfNotExist();
    }
}
