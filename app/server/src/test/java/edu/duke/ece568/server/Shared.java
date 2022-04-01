package edu.duke.ece568.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.ds.PGConnectionPoolDataSource;

public class Shared {
    public static void cleanAllTables(PostgreJDBC jdbc) throws SQLException, ClassNotFoundException{
        String queryCleanAllTables = "DELETE FROM ARCHIVE; DELETE FROM STOCK_ORDER; DELETE FROM POSITION; DELETE FROM ACCOUNT;";
        jdbc.executeUpdateStatement(queryCleanAllTables);
    }

    public static PostgreJDBC helper_generateValidJdbc() throws ClassNotFoundException, SQLException{
        // return new PostgreJDBC("localhost", "5432", "ece568_hw4", "postgres", "passw0rd");
        PGConnectionPoolDataSource connectionPool = generateConnectionPool();
        Connection conn = connectionPool.getConnection();
        return new PostgreJDBC(conn);
    }

    public static PGConnectionPoolDataSource generateConnectionPool(){
        PGConnectionPoolDataSource connectionPool = new PGConnectionPoolDataSource();
        connectionPool.setUrl("jdbc:postgresql://localhost:5432/ece568_hw4");
        connectionPool.setUser("postgres");
        connectionPool.setPassword("passw0rd");
        return connectionPool;
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
